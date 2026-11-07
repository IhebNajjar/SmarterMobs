package com.smartermobs;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Harder-world features:
 *  - zombies, wither skeletons, piglin brutes, and vindicators break blocks when they can't reach you
 *  - passive animals flee from you
 *  - iron golems hunt players who hit/kill villagers or steal from homes when caught
 *  - stealth thievery: you can steal if no villager or golem sees you
 *  - villagers repair damaged iron golems
 *  - iron golems can leap
 */
public class HarderWorld {

    private static final int STUCK_TICKS_BEFORE_DIGGING = 30; // 1.5 s of being unable to reach you
    private static final int DIG_COOLDOWN = 15;               // ticks between broken blocks
    private static final float MAX_BREAKABLE_HARDNESS = 10.0F; // stone/ores yes, obsidian no
    private static final double ANIMAL_FLEE_DISTANCE = 16.0;
    private static final double GOLEM_ALERT_RADIUS = 48.0;

    private static class DigState {
        int stuck = 0;
        int cooldown = 0;
    }

    private final Map<Mob, DigState> digStates = new WeakHashMap<>();

    // ------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------
    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide()) return;
        Entity en = e.getEntity();

        if (en instanceof Animal a
                && !(a instanceof TamableAnimal)
                && !(a instanceof AbstractHorse)
                && !(a instanceof NeutralMob)
                && !(a instanceof Enemy)) {
            // Run away from the player, unless holding this animal's food
            a.goalSelector.addGoal(1, new AvoidEntityGoal<>(a, Player.class,
                    (float) ANIMAL_FLEE_DISTANCE, 1.2D, 1.6D,
                    p -> !(p instanceof Player pl)
                            || !(a.isFood(pl.getMainHandItem()) || a.isFood(pl.getOffhandItem()))));
        } else if (en instanceof IronGolem g) {
            if (g.getAttribute(Attributes.FOLLOW_RANGE) != null) {
                g.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64);
            }
        } else if (en instanceof Villager v) {
            v.goalSelector.addGoal(2, new RepairGolemGoal(v));
        } else if (en instanceof PiglinBrute pb) {
            if (pb.getAttribute(Attributes.FOLLOW_RANGE) != null) {
                pb.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40);
            }
        } else if (en instanceof Vindicator vi) {
            if (vi.getAttribute(Attributes.FOLLOW_RANGE) != null) {
                vi.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40);
            }
        }
    }

    // ------------------------------------------------------------
    // Hitting villagers with strong attack -> Golems attack!
    // ------------------------------------------------------------
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent e) {
        LivingEntity victim = e.getEntity();
        if (victim.level().isClientSide()) return;
        Entity source = e.getSource().getEntity();
        if (!(source instanceof Player p) || p.isCreative() || p.isSpectator()) return;

        // If you hit a villager with a strong attack or weapon
        if (victim instanceof Villager v) {
            if (e.getAmount() >= 1.5F || !p.getMainHandItem().isEmpty()) {
                alertGolemsToCriminal(p, v.level(), v.blockPosition());
            }
        }
    }

    // ------------------------------------------------------------
    // Killing animals / villagers angers iron golems
    // ------------------------------------------------------------
    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.level().isClientSide()) return;
        if (!(e.getSource().getEntity() instanceof Player p)) return;
        if (p.isCreative() || p.isSpectator()) return;
        if (!(dead instanceof Villager || (dead instanceof Animal && !(dead instanceof Enemy)))) return;

        alertGolemsToCriminal(p, dead.level(), dead.blockPosition());
    }

    // ------------------------------------------------------------
    // Stealing from homes (Opening Chests, Barrels, Shulkers)
    // ------------------------------------------------------------
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock e) {
        if (e.getLevel().isClientSide()) return;
        Player p = e.getEntity();
        if (p == null || p.isCreative() || p.isSpectator()) return;

        BlockState state = e.getLevel().getBlockState(e.getPos());
        Block block = state.getBlock();

        if (block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock) {
            alertGolemsIfSeenStealing(p, e.getLevel(), e.getPos());
        }
    }

    // ------------------------------------------------------------
    // Stealing from homes (Breaking Beds, Doors, Chests, Barrels)
    // ------------------------------------------------------------
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent e) {
        if (e.getLevel().isClientSide()) return;
        Player p = e.getPlayer();
        if (p == null || p.isCreative() || p.isSpectator()) return;

        BlockState state = e.getState();
        Block block = state.getBlock();

        if (block instanceof BedBlock || block instanceof DoorBlock || block instanceof ChestBlock
                || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock) {
            if (e.getLevel() instanceof Level level) {
                alertGolemsIfSeenStealing(p, level, e.getPos());
            }
        }
    }

    // ------------------------------------------------------------
    // Stealth theft logic: only caught if a Villager or Golem sees you!
    // ------------------------------------------------------------
    private static void alertGolemsIfSeenStealing(Player p, Level level, BlockPos pos) {
        boolean witnessed = false;
        AABB witnessBox = new AABB(pos).inflate(18);

        // Check if any villager has line of sight
        for (Villager v : level.getEntitiesOfClass(Villager.class, witnessBox)) {
            if (v.isAlive() && (v.hasLineOfSight(p) || v.distanceTo(p) < 3.5)) {
                witnessed = true;
                v.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
                v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
                break;
            }
        }

        // Check if any Iron Golem has line of sight directly
        if (!witnessed) {
            for (IronGolem g : level.getEntitiesOfClass(IronGolem.class, witnessBox)) {
                if (g.isAlive() && g.hasLineOfSight(p)) {
                    witnessed = true;
                    break;
                }
            }
        }

        // If you were caught, all golems hunt you! If not seen, theft succeeds peacefully.
        if (witnessed) {
            alertGolemsToCriminal(p, level, pos);
        }
    }

    private static void alertGolemsToCriminal(Player p, Level level, BlockPos pos) {
        AABB golemBox = new AABB(pos).inflate(GOLEM_ALERT_RADIUS);
        for (IronGolem g : level.getEntitiesOfClass(IronGolem.class, golemBox)) {
            g.setTarget(p);
            g.setPersistentAngerTarget(p.getUUID());
            g.startPersistentAngerTimer();
            g.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1, false, false));
        }
    }

    // ------------------------------------------------------------
    // Tick
    // ------------------------------------------------------------
    @SubscribeEvent
    public void onTick(LivingEvent.LivingTickEvent e) {
        LivingEntity le = e.getEntity();
        if (le.level().isClientSide()) return;

        if (le instanceof IronGolem g) golemLeap(g);
        if (le instanceof Zombie || le instanceof WitherSkeleton || le instanceof PiglinBrute || le instanceof Vindicator) {
            digTick((Mob) le);
        }
    }

    // ---------------- Iron golem leap ----------------
    private void golemLeap(IronGolem g) {
        LivingEntity t = g.getTarget();
        if (t == null || !t.isAlive() || !g.onGround()) return;

        long now = g.level().getGameTime();
        if (now < g.getPersistentData().getLong("sm_leap")) return;

        double dx = t.getX() - g.getX();
        double dz = t.getZ() - g.getZ();
        double dy = t.getY() - g.getY();
        double dist = g.distanceTo(t);

        if (dist < 2.5 || dist > 14) return;
        // leap if a wall blocks it, if you are higher up, or to close a gap fast
        if (!(g.horizontalCollision || dy > 1.2 || dist > 6)) return;

        Vec3 h = new Vec3(dx, 0, dz).normalize().scale(0.5);
        double up = dy > 2.5 ? 0.72 : 0.58; // ~2.4 blocks normal, ~3.5 blocks to reach a high player
        g.setDeltaMovement(h.x, up, h.z);
        g.getPersistentData().putLong("sm_leap", now + 50);
    }

    // ---------------- Digging mobs ----------------
    private void digTick(Mob m) {
        DigState s = digStates.computeIfAbsent(m, k -> new DigState());
        if (s.cooldown > 0) s.cooldown--;

        LivingEntity t = m.getTarget();
        if (t == null || !t.isAlive() || m.distanceTo(t) > 20) {
            s.stuck = 0;
            return;
        }

        double dx = t.getX() - m.getX();
        double dz = t.getZ() - m.getZ();
        double hd = Math.sqrt(dx * dx + dz * dz);
        double dy = t.getY() - m.getY();

        Path path = m.getNavigation().getPath();
        boolean pathOk = path != null && path.canReach();
        boolean close = m.distanceTo(t) < 2.0;
        boolean stuckNow = !close && !pathOk;

        if (stuckNow) s.stuck++;
        else s.stuck = Math.max(0, s.stuck - 2);

        if (s.stuck >= STUCK_TICKS_BEFORE_DIGGING && s.cooldown == 0) {
            if (breakToward(m, t, hd, dy)) s.cooldown = DIG_COOLDOWN;
        }
    }

    private boolean breakToward(Mob m, LivingEntity t, double hd, double dy) {
        BlockPos feet = m.blockPosition();

        // Player is up on a pillar: knock out the block he stands on
        if (dy > 1.5 && hd < 3.0) {
            if (tryBreak(m, t.blockPosition().below())) return true;
        }
        // Player dug down: open the floor so the mob drops in after him
        if (dy < -1.5 && hd < 3.0) {
            if (tryBreak(m, feet.below())) return true;
        }

        // Otherwise tunnel straight toward the player (head height first, then feet)
        Vec3 dir = t.position().subtract(m.position());
        Direction d = Direction.getNearest(dir.x, 0, dir.z);
        BlockPos front = feet.relative(d);
        if (tryBreak(m, front.above())) return true;
        if (tryBreak(m, front)) return true;
        return dy > 1.0 && tryBreak(m, feet.above(2));
    }

    private boolean tryBreak(Mob m, BlockPos pos) {
        Level level = m.level();
        BlockState st = level.getBlockState(pos);
        if (st.isAir()) return false;
        float hardness = st.getDestroySpeed(level, pos);
        if (hardness < 0 || hardness > MAX_BREAKABLE_HARDNESS) return false;
        if (m.distanceToSqr(Vec3.atCenterOf(pos)) > 36) return false;
        level.destroyBlock(pos, true, m);
        return true;
    }

    // ------------------------------------------------------------
    // Villagers heal damaged iron golems
    // ------------------------------------------------------------
    static class RepairGolemGoal extends Goal {
        private final Villager villager;
        private IronGolem golem;
        private int cooldown = 0;

        RepairGolemGoal(Villager villager) {
            this.villager = villager;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            if (villager.isSleeping() || villager.isBaby()) return false;

            golem = villager.level().getEntitiesOfClass(IronGolem.class,
                            villager.getBoundingBox().inflate(16),
                            g -> g.isAlive() && g.getHealth() < g.getMaxHealth() - 1)
                    .stream()
                    .min(Comparator.comparingDouble((IronGolem g) -> villager.distanceToSqr(g)))
                    .orElse(null);
            return golem != null;
        }

        @Override
        public boolean canContinueToUse() {
            return golem != null && golem.isAlive()
                    && golem.getHealth() < golem.getMaxHealth()
                    && villager.distanceToSqr(golem) < 400;
        }

        @Override
        public void tick() {
            if (golem == null) return;
            villager.getLookControl().setLookAt(golem, 30.0F, 30.0F);
            if (villager.distanceToSqr(golem) < 6.25) {
                golem.heal(25.0F);
                golem.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0F, 1.0F);
                cooldown = 80;
                golem = null;
            } else {
                villager.getNavigation().moveTo(golem, 0.7D);
            }
        }

        @Override
        public void stop() {
            golem = null;
            villager.getNavigation().stop();
            cooldown = Math.max(cooldown, 40);
        }
    }
}
