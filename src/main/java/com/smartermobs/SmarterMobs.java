package com.smartermobs;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(SmarterMobs.MODID)
public class SmarterMobs {
    public static final String MODID = "smartermobsitscarderidea";

    public SmarterMobs() {
        MinecraftForge.EVENT_BUS.register(new MobEvents());
        MinecraftForge.EVENT_BUS.register(new HarderWorld());
    }

    public static class MobEvents {

        /** Per-creeper memory. */
        private static class CreeperState {
            Vec3 last;
            double runDist = 0;
            boolean hasAngle = false;
            double lastAngle = 0;
            double angleScore = 0;
        }

        private final Map<Creeper, CreeperState> creeperStates = new WeakHashMap<>();

        // ------------------------------------------------------------
        // Setup when a mob enters the world
        // ------------------------------------------------------------
        @SubscribeEvent
        public void onJoin(EntityJoinLevelEvent e) {
            if (e.getLevel().isClientSide()) return;
            Entity en = e.getEntity();

            if (en instanceof AbstractSkeleton sk) {
                setFollowRange(sk, 40);
                // Clear vanilla goals so our custom kiting, meat-shielding, and trapped attack logic take full effect
                sk.goalSelector.removeAllGoals(g -> true);
            } else if (en instanceof Creeper c) {
                setFollowRange(c, 40);
            } else if (en instanceof Phantom ph) {
                setFollowRange(ph, 64);
            } else if (en instanceof Monster m) {
                setFollowRange(m, 48); // All monsters notice player from farther away
            }
        }

        private static void setFollowRange(LivingEntity le, double range) {
            if (le.getAttribute(Attributes.FOLLOW_RANGE) != null) {
                le.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(range);
            }
        }

        // ------------------------------------------------------------
        // Per-tick logic
        // ------------------------------------------------------------
        @SubscribeEvent
        public void onTick(LivingEvent.LivingTickEvent e) {
            LivingEntity le = e.getEntity();
            if (le.level().isClientSide()) return;

            if (le instanceof Monster m) {
                packAndTrackingTick(m);
            }

            if (le instanceof Creeper c) creeperTick(c);
            else if (le instanceof EnderDragon d) dragonTick(d);
            else if (le instanceof Spider sp) spiderTick(sp);
            else if (le instanceof AbstractSkeleton sk) skeletonTick(sk);
            else if (le instanceof Piglin pig) piglinTick(pig);
            else if (le instanceof Pillager pil) pillagerTick(pil);
            else if (le instanceof Evoker ev) evokerTick(ev);
            else if (le instanceof Hoglin hog) hoglinTick(hog);
            else if (le instanceof Blaze bl) blazeTick(bl);
            else if (le instanceof Phantom ph) phantomTick(ph);
            else if (le instanceof Witch w) witchTick(w);
            else if (le instanceof Drowned dr) drownedTick(dr);
            else if (le instanceof Zombie z) zombieTick(z);
            else if (le instanceof Vindicator vi) vindicatorTick(vi);
        }

        // ---------------- Pack Aggro (24 blocks) & Extended Tracking (10s) ----------------
        private void packAndTrackingTick(Monster m) {
            LivingEntity target = m.getTarget();
            long now = m.level().getGameTime();

            if (target instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator()) {
                // Pack Aggro: when this mob targets a player, alert all nearby mobs of the same type within 24 blocks
                if (m.tickCount % 30 == 0) {
                    AABB box = m.getBoundingBox().inflate(24);
                    for (Monster mate : m.level().getEntitiesOfClass(m.getClass(), box)) {
                        if (mate != m && (mate.getTarget() == null || !mate.getTarget().isAlive())) {
                            mate.setTarget(p);
                        }
                    }
                }

                // 10-Second Tracking after losing sight:
                if (m.hasLineOfSight(p)) {
                    m.getPersistentData().putLong("sm_last_seen", now);
                    m.getPersistentData().putDouble("sm_ls_x", p.getX());
                    m.getPersistentData().putDouble("sm_ls_y", p.getY());
                    m.getPersistentData().putDouble("sm_ls_z", p.getZ());
                } else {
                    long lastSeen = m.getPersistentData().getLong("sm_last_seen");
                    // Keep tracking for 10 seconds (200 ticks) after losing line of sight
                    if (now - lastSeen < 200) {
                        m.setTarget(p);
                        if (m.getNavigation().isDone()) {
                            double lx = m.getPersistentData().getDouble("sm_ls_x");
                            double ly = m.getPersistentData().getDouble("sm_ls_y");
                            double lz = m.getPersistentData().getDouble("sm_ls_z");
                            m.getNavigation().moveTo(lx, ly, lz, 1.2D);
                        }
                    }
                }
            }
        }

        // ---------------- Creepers ----------------
        private void creeperTick(Creeper c) {
            CreeperState s = creeperStates.computeIfAbsent(c, k -> new CreeperState());
            Vec3 pos = c.position();

            if (s.last != null) {
                double moved = Math.hypot(pos.x - s.last.x, pos.z - s.last.z);
                if (moved > 0.02) s.runDist += moved;
                else s.runDist = 0;
            }
            s.last = pos;

            LivingEntity t = c.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) {
                s.hasAngle = false;
                s.angleScore = 0;
                return;
            }

            if (s.runDist >= 2.0) {
                c.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, false, false));
            }

            double d = c.distanceTo(p);
            if (d > 8) return;

            // Player placed a block / is behind a wall: still ignites
            if (d < 4.5 && !c.hasLineOfSight(p)) {
                c.ignite();
            }

            // Detect player circling around creeper
            double ang = Math.atan2(p.getZ() - c.getZ(), p.getX() - c.getX());
            if (s.hasAngle) {
                double diff = ang - s.lastAngle;
                while (diff > Math.PI) diff -= 2 * Math.PI;
                while (diff < -Math.PI) diff += 2 * Math.PI;
                s.angleScore = s.angleScore * 0.95 + Math.abs(diff);
            }
            s.lastAngle = ang;
            s.hasAngle = true;

            if (d < 6 && s.angleScore > 0.8) {
                c.ignite();
            }
        }

        // ---------------- Ender Dragon (With cooldown between fireballs and charge) ----------------
        private void dragonTick(EnderDragon d) {
            if (d.isDeadOrDying()) return;
            Player p = d.level().getNearestPlayer(d, 120);
            if (p == null || p.isCreative() || p.isSpectator()) return;

            long now = d.level().getGameTime();
            long nextAction = d.getPersistentData().getLong("sm_dragon_next_action");

            // Cooldown between attacks
            if (now < nextAction) return;

            int t = d.tickCount;

            // 1. Breath barrage: shoots 3 fireballs aimed straight at the player
            if (t % 110 == 20) {
                Vec3 head = d.head.position();
                Vec3 dir = p.getEyePosition().subtract(head);
                for (int i = -1; i <= 1; i++) {
                    Vec3 v = dir.add(i * 1.5, 0, i * 1.5);
                    DragonFireball fb = new DragonFireball(d.level(), d, v.x, v.y, v.z);
                    fb.moveTo(head.x, head.y, head.z, 0, 0);
                    d.level().addFreshEntity(fb);
                }
                // Cooldown: at least 70 ticks (3.5s) before it can charge
                d.getPersistentData().putLong("sm_dragon_next_action", now + 70);
                return;
            }

            // 2. Surprise Ambush Charge (No warning dive)
            Vec3 playerLook = p.getViewVector(1.0F).normalize();
            Vec3 toDragon = d.position().subtract(p.position()).normalize();
            boolean lookingAway = playerLook.dot(toDragon) < -0.15; // player not looking at dragon

            if ((lookingAway && t % 70 == 0) || (t % 140 == 0)) {
                d.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                d.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(p.position());

                // Instant speed boost towards player for a sudden no-warning dive
                Vec3 diveVec = p.position().subtract(d.position()).normalize().scale(1.35);
                d.setDeltaMovement(diveVec.x, diveVec.y, diveVec.z);

                // Cooldown: at least 80 ticks (4s) before it can fire breath attacks
                d.getPersistentData().putLong("sm_dragon_next_action", now + 80);
            }
        }

        // ---------------- Skeletons (10-block retreat, hide behind zombies, fight when cornered) ----------------
        private void skeletonTick(AbstractSkeleton sk) {
            LivingEntity t = sk.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            double dist = sk.distanceTo(p);
            long now = sk.level().getGameTime();

            // Check if skeleton is trapped in a tight corner or very close
            boolean isCornered = (sk.horizontalCollision || sk.getDeltaMovement().horizontalDistanceSqr() < 0.005) && dist < 8.0;
            boolean isTooClose = dist <= 3.5;

            if (isTooClose || isCornered) {
                // FIXED: When trapped or rushed, don't run against walls! Fight back aggressively!
                sk.getLookControl().setLookAt(p, 30.0F, 30.0F);

                if (now >= sk.getPersistentData().getLong("sm_skel_shot")) {
                    sk.performRangedAttack(p, 1.0F);
                    sk.getPersistentData().putLong("sm_skel_shot", now + 16);
                }

                if (dist <= 2.0) {
                    sk.doHurtTarget(p); // Melee strike if right in your face
                }
            } else if (dist <= 10.0) {
                // Retreat when within 10 blocks: check if there is a tanky zombie or monster to hide behind
                Monster meatShield = null;
                double minShieldDist = Double.MAX_VALUE;
                AABB searchBox = sk.getBoundingBox().inflate(10);
                for (Monster candidate : sk.level().getEntitiesOfClass(Monster.class, searchBox)) {
                    if (candidate != sk && !(candidate instanceof AbstractSkeleton) && candidate.isAlive()) {
                        double d = sk.distanceTo(candidate);
                        if (d < minShieldDist) {
                            minShieldDist = d;
                            meatShield = candidate;
                        }
                    }
                }

                if (meatShield != null) {
                    // Position behind the meat shield relative to the player
                    Vec3 playerToShield = meatShield.position().subtract(p.position()).normalize();
                    Vec3 hideSpot = meatShield.position().add(playerToShield.scale(2.5));
                    sk.getNavigation().moveTo(hideSpot.x, hideSpot.y, hideSpot.z, 1.2D);
                } else {
                    // Back away directly from player
                    Vec3 away = sk.position().subtract(p.position()).normalize().scale(0.35);
                    sk.setDeltaMovement(away.x, sk.getDeltaMovement().y, away.z);
                }

                sk.getLookControl().setLookAt(p, 30.0F, 30.0F);
                if (now >= sk.getPersistentData().getLong("sm_skel_shot")) {
                    sk.performRangedAttack(p, 1.0F);
                    sk.getPersistentData().putLong("sm_skel_shot", now + 22);
                }
            } else {
                // Outside 10 blocks: normal aim and shoot
                sk.getLookControl().setLookAt(p, 30.0F, 30.0F);
                if (now >= sk.getPersistentData().getLong("sm_skel_shot")) {
                    sk.performRangedAttack(p, 1.0F);
                    sk.getPersistentData().putLong("sm_skel_shot", now + 24);
                }
            }
        }

        // ---------------- Spiders (Leap from 4 blocks & Throw Web with 3s Cooldown) ----------------
        private void spiderTick(Spider sp) {
            LivingEntity t = sp.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            double dist = sp.distanceTo(p);
            long now = sp.level().getGameTime();

            // 1. Leap attack from 4 blocks away (3.5 - 7 blocks)
            if (sp.onGround() && dist >= 3.5 && dist <= 7.0 && sp.hasLineOfSight(p)) {
                if (now >= sp.getPersistentData().getLong("sm_leap")) {
                    Vec3 jumpDir = p.position().subtract(sp.position()).normalize().scale(0.65);
                    sp.setDeltaMovement(jumpDir.x, 0.45, jumpDir.z);
                    sp.getPersistentData().putLong("sm_leap", now + 40);
                }
            }

            // 2. Throw cobweb at player when within 3 blocks with a 3-second (60 tick) cooldown
            if (dist <= 3.2) {
                if (now >= sp.getPersistentData().getLong("sm_web")) {
                    BlockPos playerPos = p.blockPosition();
                    Level level = sp.level();
                    if (level.getBlockState(playerPos).isAir()) {
                        level.setBlockAndUpdate(playerPos, Blocks.COBWEB.defaultBlockState());
                        level.playSound(null, playerPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.HOSTILE, 1.0F, 1.0F);
                    }
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, false));
                    p.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, false, false));
                    sp.getPersistentData().putLong("sm_web", now + 60); // 3-second cooldown!
                }
            }
        }

        // ---------------- Crossbow Piglins (Kite & back off) ----------------
        private void piglinTick(Piglin pig) {
            LivingEntity t = pig.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            if (pig.isHolding(is -> is.is(Items.CROSSBOW))) {
                double dist = pig.distanceTo(p);
                long now = pig.level().getGameTime();

                if (dist <= 3.0) {
                    // Too close: shoot point-blank or hit!
                    pig.getLookControl().setLookAt(p, 30.0F, 30.0F);
                    if (now >= pig.getPersistentData().getLong("sm_pig_shot")) {
                        pig.performRangedAttack(p, 1.0F);
                        pig.getPersistentData().putLong("sm_pig_shot", now + 20);
                    }
                } else if (dist < 8.0) {
                    // Back off like a skeleton to keep distance
                    Vec3 away = pig.position().subtract(p.position()).normalize().scale(0.35);
                    pig.setDeltaMovement(away.x, pig.getDeltaMovement().y, away.z);
                    pig.getLookControl().setLookAt(p, 30.0F, 30.0F);
                }
            }
        }

        // ---------------- Crossbow Pillagers (Kite & back off) ----------------
        private void pillagerTick(Pillager pil) {
            LivingEntity t = pil.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            double dist = pil.distanceTo(p);
            long now = pil.level().getGameTime();

            if (dist <= 3.0) {
                pil.getLookControl().setLookAt(p, 30.0F, 30.0F);
                if (now >= pil.getPersistentData().getLong("sm_pil_shot")) {
                    pil.performRangedAttack(p, 1.0F);
                    pil.getPersistentData().putLong("sm_pil_shot", now + 20);
                }
            } else if (dist < 8.0) {
                // Back off to maintain crossbow range
                Vec3 away = pil.position().subtract(p.position()).normalize().scale(0.35);
                pil.setDeltaMovement(away.x, pil.getDeltaMovement().y, away.z);
                pil.getLookControl().setLookAt(p, 30.0F, 30.0F);
            }
        }

        // ---------------- Evokers (Summon vexes often, line of fangs, run away close) ----------------
        private void evokerTick(Evoker ev) {
            LivingEntity t = ev.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            double dist = ev.distanceTo(p);
            Level level = ev.level();
            long now = level.getGameTime();

            // 1. Run away when player gets close (within 8 blocks)
            if (dist < 8.0) {
                Vec3 away = ev.position().subtract(p.position()).normalize().scale(0.38);
                ev.setDeltaMovement(away.x, ev.getDeltaMovement().y, away.z);
                ev.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
            }

            // 2. Send fangs in a line towards player every 70 ticks (3.5s)
            if (now >= ev.getPersistentData().getLong("sm_evoker_fangs")) {
                Vec3 start = ev.position();
                Vec3 dir = p.position().subtract(start);
                double maxDist = Math.min(dir.length(), 16.0);
                Vec3 step = dir.normalize();

                for (int i = 2; i <= (int) maxDist; i += 2) {
                    double fx = start.x + step.x * i;
                    double fz = start.z + step.z * i;
                    int fy = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) fx, (int) fz);
                    EvokerFangs fangs = new EvokerFangs(level, fx, fy, fz, (float) Math.toDegrees(Math.atan2(step.z, step.x)), i, ev);
                    level.addFreshEntity(fangs);
                }
                ev.getPersistentData().putLong("sm_evoker_fangs", now + 70);
            }

            // 3. Summon vexes more often (every 140 ticks = 7s)
            if (now >= ev.getPersistentData().getLong("sm_evoker_vexes")) {
                AABB vexBox = ev.getBoundingBox().inflate(16);
                List<Vex> existingVexes = level.getEntitiesOfClass(Vex.class, vexBox);
                if (existingVexes.size() < 4) {
                    for (int i = 0; i < 2; i++) {
                        Vex vex = new Vex(EntityType.VEX, level);
                        vex.moveTo(ev.getX() + (Math.random() - 0.5) * 3, ev.getY() + 1.5, ev.getZ() + (Math.random() - 0.5) * 3, ev.getYRot(), 0.0F);
                        vex.setOwner(ev);
                        vex.setTarget(p);
                        level.addFreshEntity(vex);
                    }
                }
                ev.getPersistentData().putLong("sm_evoker_vexes", now + 140);
            }
        }

        // ---------------- Hoglins (Speed burst >= 6 blocks, call nearby hoglins) ----------------
        private void hoglinTick(Hoglin hog) {
            LivingEntity t = hog.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            double dist = hog.distanceTo(p);

            // Speed burst when 6 or more blocks away
            if (dist >= 6.0 && hog.hasLineOfSight(p)) {
                hog.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
            }

            // Call nearby hoglins to charge together
            if (hog.tickCount % 40 == 0) {
                AABB box = hog.getBoundingBox().inflate(32);
                for (Hoglin mate : hog.level().getEntitiesOfClass(Hoglin.class, box)) {
                    if (mate != hog && (mate.getTarget() == null || !mate.getTarget().isAlive())) {
                        mate.setTarget(p);
                        mate.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, false, false));
                    }
                }
            }
        }

        // ---------------- Blazes (Strafe sideways & shoot in bursts) ----------------
        private void blazeTick(Blaze bl) {
            LivingEntity t = bl.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            Vec3 toP = p.position().subtract(bl.position());
            Vec3 norm = toP.normalize();

            // 1. Strafe sideways
            Vec3 perp = new Vec3(-norm.z, 0, norm.x).scale(0.28);
            if ((bl.tickCount / 40) % 2 == 0) perp = perp.scale(-1);
            bl.setDeltaMovement(bl.getDeltaMovement().add(perp.x, 0, perp.z));

            // 2. Shoot in bursts of 3 fireballs every 70 ticks
            long now = bl.level().getGameTime();
            if (now >= bl.getPersistentData().getLong("sm_blaze_burst")) {
                for (int i = -1; i <= 1; i++) {
                    Vec3 fbDir = norm.add((Math.random() - 0.5) * 0.12, (Math.random() - 0.5) * 0.12, (Math.random() - 0.5) * 0.12);
                    SmallFireball fb = new SmallFireball(bl.level(), bl, fbDir.x, fbDir.y, fbDir.z);
                    fb.setPos(bl.getX(), bl.getY(0.5) + 0.5, bl.getZ());
                    bl.level().addFreshEntity(fb);
                }
                bl.getPersistentData().putLong("sm_blaze_burst", now + 70);
            }
        }

        // ---------------- Phantoms (Dive in groups of 3 or more) ----------------
        private void phantomTick(Phantom ph) {
            LivingEntity t = ph.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            // Group aggro coordination
            if (ph.tickCount % 40 == 0) {
                AABB pBox = ph.getBoundingBox().inflate(48);
                List<Phantom> flock = ph.level().getEntitiesOfClass(Phantom.class, pBox);
                for (Phantom other : flock) {
                    if (other.getTarget() == null || !other.getTarget().isAlive()) {
                        other.setTarget(p);
                    }
                }

                // If at least 3 phantoms are present, boost group dive speed
                if (flock.size() >= 3 && ph.getY() > p.getY() + 3) {
                    Vec3 dive = p.position().subtract(ph.position()).normalize().scale(1.15);
                    ph.setDeltaMovement(dive.x, dive.y, dive.z);
                }
            }
        }

        // ---------------- Witches (Long-range throw, retreat, faster self-heal) ----------------
        private void witchTick(Witch w) {
            LivingEntity t = w.getTarget();
            Level level = w.level();
            int tick = w.tickCount;

            // 1. Heal faster (every 25 ticks heal 3 HP and apply Regeneration)
            if (w.getHealth() < w.getMaxHealth() && tick % 25 == 0) {
                w.heal(3.0F);
                w.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 1, false, false));
            }

            // 2. Buff nearby monster allies
            if (tick % 60 == 0) {
                AABB box = w.getBoundingBox().inflate(12);
                for (Monster enemy : level.getEntitiesOfClass(Monster.class, box)) {
                    if (!(enemy instanceof Witch)) {
                        enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 0, false, false));
                        enemy.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 0, false, false));
                    }
                }
            }

            // 3. Retreat when player approaches
            if (t instanceof Player p) {
                double dist = w.distanceTo(p);
                if (dist < 9.0) {
                    Vec3 away = w.position().subtract(p.position()).normalize().scale(0.36);
                    w.setDeltaMovement(away.x, w.getDeltaMovement().y, away.z);
                    w.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
                }

                // 4. Throw potions from farther away (up to 22 blocks)
                if (dist <= 22.0 && w.hasLineOfSight(p) && tick % 35 == 0) {
                    w.performRangedAttack(p, 1.0F);
                }
            }
        }

        // ---------------- Drowned (Faster swimming, long trident throws, land daylight chasing) ----------------
        private void drownedTick(Drowned dr) {
            LivingEntity t = dr.getTarget();

            // Faster swimming
            if (dr.isInWater()) {
                dr.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 1, false, false));
                dr.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 2, false, false));

                // Whirlpool pull down
                if (t instanceof Player p && p.isInWater() && dr.distanceTo(p) < 4.0) {
                    p.setDeltaMovement(p.getDeltaMovement().add(0, -0.12, 0));
                }
            }

            // Chase on land even in daylight without burning
            if (t instanceof Player p && p.isAlive()) {
                dr.clearFire();
                dr.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, false, false));

                // Throw tridents from farther away (up to 30 blocks)
                double dist = dr.distanceTo(p);
                if (dist > 8.0 && dist <= 30.0 && dr.hasLineOfSight(p) && dr.tickCount % 45 == 0) {
                    if (dr.isHolding(is -> is.is(Items.TRIDENT))) {
                        dr.performRangedAttack(p, 1.0F);
                    }
                }
            }
        }

        // ---------------- Zombies (Frenzy speed on wounded prey) ----------------
        private void zombieTick(Zombie z) {
            LivingEntity t = z.getTarget();
            if (t instanceof Player p && p.getHealth() <= 10.0F) {
                z.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, false, false));
            }
        }

        // ---------------- Vindicators (Sprint) ----------------
        private void vindicatorTick(Vindicator vi) {
            LivingEntity t = vi.getTarget();
            if (t instanceof Player) {
                vi.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
            }
        }

        // ---------------- Damage & Hit Events ----------------
        @SubscribeEvent
        public void onHurt(LivingHurtEvent e) {
            LivingEntity victim = e.getEntity();
            Entity attacker = e.getSource().getEntity();

            // Enderman Flanking: teleports behind attacker when damaged and blinds them
            if (victim instanceof EnderMan em && attacker instanceof Player p) {
                Vec3 pLook = p.getViewVector(1.0F).normalize();
                Vec3 behind = p.position().subtract(pLook.scale(2.2));
                em.teleportTo(behind.x, p.getY(), behind.z);
                em.getLookControl().setLookAt(p, 180.0F, 180.0F);
                em.setTarget(p);
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false));
            }

            // Piglins & Piglin Brutes Pack Aggro: hitting one makes all piglins within 32 blocks attack
            if ((victim instanceof Piglin || victim instanceof PiglinBrute) && attacker instanceof Player p) {
                AABB box = victim.getBoundingBox().inflate(32);
                for (AbstractPiglin pig : victim.level().getEntitiesOfClass(AbstractPiglin.class, box)) {
                    pig.setTarget(p);
                    pig.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0, false, false));
                }
            }

            // Hoglin stronger knockback & group aggro
            if (attacker instanceof Hoglin && victim instanceof Player p) {
                p.knockback(1.8D, attacker.getX() - p.getX(), attacker.getZ() - p.getZ());
            }
            if (victim instanceof Hoglin && attacker instanceof Player p) {
                AABB box = victim.getBoundingBox().inflate(32);
                for (Hoglin mate : victim.level().getEntitiesOfClass(Hoglin.class, box)) {
                    mate.setTarget(p);
                    mate.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, false, false));
                }
            }

            // Zombie Horde Call: striking a zombie alerts all zombies within 35 blocks
            if (victim instanceof Zombie && attacker instanceof Player p) {
                AABB box = victim.getBoundingBox().inflate(35);
                for (Zombie nearby : victim.level().getEntitiesOfClass(Zombie.class, box)) {
                    if (nearby.getTarget() == null || !nearby.getTarget().isAlive()) {
                        nearby.setTarget(p);
                        nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0, false, false));
                    }
                }
            }
        }

        // ---------------- Explosion shield piercing ----------------
        @SubscribeEvent
        public void onExplosion(ExplosionEvent.Detonate e) {
            Explosion ex = e.getExplosion();
            if (!(ex.getDirectSourceEntity() instanceof Creeper)) return;

            Level level = e.getLevel();
            Vec3 center = ex.getPosition();
            AABB box = new AABB(center, center).inflate(7);

            for (Player p : level.getEntitiesOfClass(Player.class, box)) {
                if (p.isCreative() || p.isSpectator()) continue;
                double dist = p.position().distanceTo(center);
                if (dist > 6) continue;

                // Break the shield
                if (p.isBlocking()) {
                    p.getCooldowns().addCooldown(Items.SHIELD, 100);
                    p.stopUsingItem();
                }

                // Pierces walls
                float seen = Explosion.getSeenPercent(center, p);
                if (seen < 0.4F) {
                    float dmg = (float) (10.0 * (1.0 - dist / 6.0));
                    if (dmg > 0) p.hurt(level.damageSources().explosion(ex), dmg);
                }
            }
        }
    }
}
