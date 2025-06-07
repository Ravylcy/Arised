package com.sololeveling.necromancy.common.util;

import com.sololeveling.necromancy.common.capability.IShadowMinion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class ShadowAI {

    /**
     * Configures the AI for a given shadow entity based on the owner's current stance.
     */
    public static void setup(PathfinderMob shadow, Player owner, AIStance stance) {
        // Clear any pre-existing goals to ensure a clean slate.
        shadow.targetSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).collect(Collectors.toList())
                .forEach(shadow.targetSelector::removeGoal);
        shadow.goalSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).collect(Collectors.toList())
                .forEach(shadow.goalSelector::removeGoal);

        // --- Standard Behavior Goals (Always Active) ---
        shadow.goalSelector.addGoal(1, new MeleeAttackGoal(shadow, 1.2D, true));
        shadow.goalSelector.addGoal(2, new FollowOwnerGoal(shadow, owner, 1.2D, 10.0F, 2.0F));
        shadow.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(shadow, 1.0D));
        shadow.goalSelector.addGoal(4, new LookAtPlayerGoal(shadow, Player.class, 8.0F));
        shadow.goalSelector.addGoal(5, new RandomLookAroundGoal(shadow));

        // --- Stance-based Targeting Goals ---
        switch (stance) {
            case AGGRESSIVE:
                addAggressiveGoals(shadow, owner);
                break;
            case DEFENSIVE:
                addDefensiveGoals(shadow, owner);
                break;
            case PASSIVE:
                // No targeting goals for passive stance.
                break;
        }
    }

    private static void addDefensiveGoals(PathfinderMob shadow, Player owner) {
        // Goal 1: Defend the owner if they are attacked.
        shadow.targetSelector.addGoal(1, new CustomOwnerHurtByTargetGoal(shadow, owner));
        // Goal 2: Attack the entity that the owner attacks.
        shadow.targetSelector.addGoal(2, new CustomOwnerHurtTargetGoal(shadow, owner));
        // Goal 3: Retaliate when hurt by non-friendly entities.
        shadow.targetSelector.addGoal(3, new CustomHurtByTargetGoal(shadow, owner).setAlertOthers());
    }

    private static void addAggressiveGoals(PathfinderMob shadow, Player owner) {
        // Includes all defensive goals, plus proactive targeting.
        addDefensiveGoals(shadow, owner);

        // Goal 4: Proactively attack nearby hostile mobs.
        shadow.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(shadow, LivingEntity.class, 10, true, false,
                (potentialTarget) -> {
                    if (!(potentialTarget instanceof Enemy) || owner.isAlliedTo(potentialTarget)) {
                        return false;
                    }
                    boolean isOurShadow = IShadowMinion.get(potentialTarget)
                            .map(cap -> cap.isShadow() && owner.getUUID().equals(cap.getOwnerUUID()))
                            .orElse(false);
                    return !isOurShadow;
                }));
    }

    private static class FollowOwnerGoal extends Goal {
        private final PathfinderMob mob;
        private final Player owner;
        private final double speedModifier;
        private final float stopDistance;
        private final float startDistance;
        private int timeToRecalcPath;

        public FollowOwnerGoal(PathfinderMob mob, Player owner, double speedModifier, float stopDistance, float startDistance) {
            this.mob = mob;
            this.owner = owner;
            this.speedModifier = speedModifier;
            this.stopDistance = stopDistance;
            this.startDistance = startDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.owner == null || this.owner.isSpectator()) return false;
            return this.mob.distanceToSqr(this.owner) >= (double)(this.startDistance * this.startDistance);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone() && this.mob.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance);
        }

        @Override
        public void start() { this.timeToRecalcPath = 0; }

        @Override
        public void stop() { this.mob.getNavigation().stop(); }

        @Override
        public void tick() {
            this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float)this.mob.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (!this.mob.isLeashed() && !this.mob.isPassenger()) {
                    if (this.mob.distanceToSqr(this.owner) >= 144.0D) {
                        this.teleportToOwner();
                    } else {
                        this.mob.getNavigation().moveTo(this.owner, this.speedModifier);
                    }
                }
            }
        }

        private void teleportToOwner() {
            this.mob.moveTo(this.owner.getX(), this.owner.getY(), this.owner.getZ(), this.owner.getYRot(), this.owner.getXRot());
        }
    }

    private static class CustomOwnerHurtByTargetGoal extends TargetGoal {
        private final Player owner;
        private LivingEntity attacker;
        private int lastAttackTime;

        public CustomOwnerHurtByTargetGoal(PathfinderMob mob, Player owner) {
            super(mob, false);
            this.owner = owner;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.owner == null) return false;
            this.attacker = this.owner.getLastHurtByMob();
            int i = this.owner.getLastHurtByMobTimestamp();
            if (i == this.lastAttackTime || this.attacker == null) {
                return false;
            }
            boolean isOurShadow = IShadowMinion.get(this.attacker)
                    .map(cap -> cap.isShadow() && owner.getUUID().equals(cap.getOwnerUUID()))
                    .orElse(false);
            if (isOurShadow) {
                return false;
            }
            return this.canAttack(this.attacker, TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting());
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            this.lastAttackTime = this.owner.getLastHurtByMobTimestamp();
            super.start();
        }
    }

    private static class CustomOwnerHurtTargetGoal extends TargetGoal {
        private final Player owner;
        private LivingEntity target;
        private int lastAttackTime;

        public CustomOwnerHurtTargetGoal(PathfinderMob mob, Player owner) {
            super(mob, false);
            this.owner = owner;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.owner == null) return false;
            this.target = this.owner.getLastHurtMob();
            int i = this.owner.getLastHurtMobTimestamp();
            if (i == this.lastAttackTime || this.target == null) {
                return false;
            }
            boolean isOurShadow = IShadowMinion.get(this.target)
                    .map(cap -> cap.isShadow() && owner.getUUID().equals(cap.getOwnerUUID()))
                    .orElse(false);
            if (isOurShadow) {
                return false;
            }
            return this.canAttack(this.target, TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting());
        }

        @Override
        public void start() {
            this.mob.setTarget(this.target);
            this.lastAttackTime = this.owner.getLastHurtMobTimestamp();
            super.start();
        }
    }

    private static class CustomHurtByTargetGoal extends HurtByTargetGoal {
        private final Player owner;

        public CustomHurtByTargetGoal(PathfinderMob mob, Player owner) {
            super(mob);
            this.owner = owner;
        }

        @Override
        protected boolean canAttack(@Nullable LivingEntity potentialTarget, @NotNull TargetingConditions conditions) {
            if (potentialTarget == null || this.owner == null) {
                return false;
            }

            if (potentialTarget.getUUID().equals(this.owner.getUUID())) {
                return false;
            }

            boolean isOurShadow = IShadowMinion.get(potentialTarget)
                    .map(cap -> cap.isShadow() && this.owner.getUUID().equals(cap.getOwnerUUID()))
                    .orElse(false);

            if (isOurShadow) {
                return false;
            }

            return super.canAttack(potentialTarget, conditions);
        }
    }
}
