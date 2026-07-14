package com.core.dream_sakura_blue_archive.ciorastao.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * 阿罗娜愤怒状态下的远程攻击 AI。
 * 仅当 {@link AronaEntity#isAngry()} 为 true 且存在有效攻击目标时激活。
 * 在 16 格范围内持续向目标投射 {@link AronaProjectile}。
 */
public class AronaRangedAttackGoal extends Goal {

    private final AronaEntity arona;
    private int attackCooldown;
    private static final int ATTACK_INTERVAL = 20; // 每秒一发
    private static final double ATTACK_RANGE_SQR = 16.0D * 16.0D;

    public AronaRangedAttackGoal(AronaEntity arona) {
        this.arona = arona;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!arona.isAngry()) return false;
        LivingEntity target = arona.getTarget();
        return isValidTarget(target);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        attackCooldown = 0;
    }

    @Override
    public void stop() {
        arona.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = arona.getTarget();
        // 每 tick 校验目标有效性，排除创造/旁观玩家后自动清除无效目标
        if (!isValidTarget(target)) {
            arona.setTarget(null);
            return;
        }

        double distSqr = arona.distanceToSqr(target);
        arona.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 向目标靠近至射程边缘
        if (distSqr > ATTACK_RANGE_SQR) {
            arona.getNavigation().moveTo(target, 0.8D);
        } else {
            arona.getNavigation().stop();
        }

        if (--attackCooldown > 0) return;

        if (arona.getSensing().hasLineOfSight(target)) {
            performRangedAttack(target);
        }
        attackCooldown = ATTACK_INTERVAL;
    }

    /** 目标必须存活且不是创造/旁观模式玩家。 */
    private static boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return true;
    }

    private void performRangedAttack(LivingEntity target) {
        AronaProjectile projectile = new AronaProjectile(arona.level(), arona);
        double dx = target.getX() - arona.getX();
        double dy = target.getEyeY() - arona.getEyeY();
        double dz = target.getZ() - arona.getZ();
        projectile.shoot(dx, dy, dz, 1.6F, 0);
        arona.level().addFreshEntity(projectile);
    }
}
