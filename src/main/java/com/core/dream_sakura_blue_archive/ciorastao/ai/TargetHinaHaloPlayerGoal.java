package com.core.dream_sakura_blue_archive.ciorastao.ai;

import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class TargetHinaHaloPlayerGoal extends TargetGoal {
    private final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forCombat()
            .ignoreLineOfSight()
            .selector(this::isHinaPlayer);

    private final Mob mob;
    private Player targetPlayer;
    private int unseenTicks;

    public TargetHinaHaloPlayerGoal(final Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean isHinaPlayer(LivingEntity entity) {
        return (entity instanceof Player player) &&
                player.isAlive() &&
                OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hina_halo");
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            return false;
        }

        AABB searchArea = this.mob.getBoundingBox().inflate(this.getFollowDistance(), 4.0D, this.getFollowDistance());
        List<Player> players = this.mob.level().getNearbyEntities(
                Player.class,
                TARGET_CONDITIONS,
                this.mob,
                searchArea
        );
        if (players.isEmpty()) {
            return false;
        }

        this.targetPlayer = players.get(0);
        double minDistance = this.mob.distanceToSqr(this.targetPlayer);

        for (Player player : players) {
            double distance = this.mob.distanceToSqr(player);
            if (distance < minDistance) {
                minDistance = distance;
                this.targetPlayer = player;
            }
        }

        return true;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.targetPlayer);
        this.unseenTicks = 0;
        super.start();
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }

        // 检查目标是否仍然佩戴Hina光环
        if (!OtherHelper.getCuriosItem(this.targetPlayer, "halo", "dream_sakura_blue_archive:hina_halo")) {
            return false;
        }

        // 检查距离
        if (this.mob.distanceToSqr(this.targetPlayer) > this.getFollowDistance() * this.getFollowDistance()) {
            return false;
        }

        // 检查视线
        if (this.mob.getSensing().hasLineOfSight(this.targetPlayer)) {
            this.unseenTicks = 0;
        } else if (++this.unseenTicks > this.unseenMemoryTicks()) {
            return false;
        }

        return true;
    }

    @Override
    protected double getFollowDistance() {
        return 32.0D;
    }

    private int unseenMemoryTicks() {
        return 60; // 失去视线后60ticks(3秒)内仍保持目标
    }
}
