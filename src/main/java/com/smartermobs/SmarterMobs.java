package com.smartermobs;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
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
                // Skeletons shoot from 28 blocks away, faster, and kite when approached
                sk.goalSelector.removeAllGoals(g -> g instanceof RangedBowAttackGoal<?>);
                sk.goalSelector.addGoal(2, new AvoidEntityGoal<>(sk, Player.class, 7.0F, 1.1D, 1.5D));
                sk.goalSelector.addGoal(3, new RangedBowAttackGoal<>(sk, 1.0D, 14, 28.0F));
            } else if (en instanceof Creeper c) {
                setFollowRange(c, 40);
            } else if (en instanceof Zombie || en instanceof Spider || en instanceof Witch || en instanceof Drowned) {
                setFollowRange((LivingEntity) en, 48); // notice player from farther away
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

            if (le instanceof Creeper c) creeperTick(c);
            else if (le instanceof EnderDragon d) dragonTick(d);
            else if (le instanceof Spider sp) spiderTick(sp);
            else if (le instanceof Witch w) witchTick(w);
            else if (le instanceof Drowned dr) drownedTick(dr);
            else if (le instanceof Zombie z) zombieTick(z);
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

        // ---------------- Ender Dragon ----------------
        private void dragonTick(EnderDragon d) {
            if (d.isDeadOrDying()) return;
            Player p = d.level().getNearestPlayer(d, 120);
            if (p == null || p.isCreative() || p.isSpectator()) return;

            int t = d.tickCount;

            // Breath barrage: shoots 3 fireballs aimed straight at the player every 4 seconds
            if (t % 80 == 20) {
                Vec3 head = d.head.position();
                Vec3 dir = p.getEyePosition().subtract(head);
                for (int i = -1; i <= 1; i++) {
                    Vec3 v = dir.add(i * 1.5, 0, i * 1.5);
                    DragonFireball fb = new DragonFireball(d.level(), d, v.x, v.y, v.z);
                    fb.moveTo(head.x, head.y, head.z, 0, 0);
                    d.level().addFreshEntity(fb);
                }
            }

            // --- SURPRISE AMBUSH CHARGE WITHOUT WARNING ---
            // Detect if player is looking away from the dragon (player's blind spot!)
            Vec3 playerLook = p.getViewVector(1.0F).normalize();
            Vec3 toDragon = d.position().subtract(p.position()).normalize();
            boolean lookingAway = playerLook.dot(toDragon) < -0.15; // dragon is behind/out of player's FOV

            // Ambush: when player looks away or periodically, dragon silently swoops at high speed
            if ((lookingAway && t % 60 == 0) || (t % 110 == 0)) {
                d.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                d.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(p.position());

                // Instant speed boost towards player for a sudden no-warning dive
                Vec3 diveVec = p.position().subtract(d.position()).normalize().scale(1.35);
                d.setDeltaMovement(diveVec.x, diveVec.y, diveVec.z);
            }
        }

        // ---------------- Spiders (Leap attack & Web slowing) ----------------
        private void spiderTick(Spider sp) {
            LivingEntity t = sp.getTarget();
            if (!(t instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator()) return;

            double dist = sp.distanceTo(p);

            // Spider leaping attack from 3.5 to 9 blocks away
            if (sp.onGround() && dist >= 3.5 && dist <= 9.0 && sp.hasLineOfSight(p)) {
                long now = sp.level().getGameTime();
                if (now >= sp.getPersistentData().getLong("sm_leap")) {
                    Vec3 jumpDir = p.position().subtract(sp.position()).normalize().scale(0.65);
                    sp.setDeltaMovement(jumpDir.x, 0.45, jumpDir.z);
                    sp.getPersistentData().putLong("sm_leap", now + 40); // 2 second cooldown
                }
            }

            // Sticky web venom when very close
            if (dist < 2.2) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0, false, false));
            }
        }

        // ---------------- Witches (Squad Buffer & Tactical retreat) ----------------
        private void witchTick(Witch w) {
            LivingEntity t = w.getTarget();
            Level level = w.level();
            int tick = w.tickCount;

            // Every 3 seconds, Witch buffs nearby allied monsters with Speed and Strength
            if (tick % 60 == 0) {
                AABB box = w.getBoundingBox().inflate(12);
                for (Monster enemy : level.getEntitiesOfClass(Monster.class, box)) {
                    if (!(enemy instanceof Witch)) {
                        enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 0, false, false));
                        enemy.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 0, false, false));
                    }
                }
            }

            // If player gets too close, Witch panic-retreats with Speed and curses the player with Slowness
            if (t instanceof Player p && w.distanceTo(p) < 4.0) {
                w.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
            }
        }

        // ---------------- Drowned (Fast swimmer & Whirlpool grab) ----------------
        private void drownedTick(Drowned dr) {
            if (dr.isInWater()) {
                dr.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, false, false));
                dr.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));

                LivingEntity t = dr.getTarget();
                // Pull swimming players down
                if (t instanceof Player p && p.isInWater() && dr.distanceTo(p) < 3.0) {
                    p.setDeltaMovement(p.getDeltaMovement().add(0, -0.08, 0));
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

        // ---------------- Damage & Hit Events ----------------
        @SubscribeEvent
        public void onHurt(LivingHurtEvent e) {
            LivingEntity victim = e.getEntity();
            Entity attacker = e.getSource().getEntity();

            // Enderman Flanking: when attacked or shot, teleports behind attacker and blinds them
            if (victim instanceof EnderMan em && attacker instanceof Player p) {
                Vec3 pLook = p.getViewVector(1.0F).normalize();
                Vec3 behind = p.position().subtract(pLook.scale(2.2));
                em.teleportTo(behind.x, p.getY(), behind.z);
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false));
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
