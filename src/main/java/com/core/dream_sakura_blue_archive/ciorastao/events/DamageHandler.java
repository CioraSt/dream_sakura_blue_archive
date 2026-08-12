package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura_blue_archive.ciorastao.halo.HaloAttackType;
import com.core.dream_sakura_blue_archive.ciorastao.halo.HaloDamage;
import com.core.dream_sakura_blue_archive.ciorastao.halo.HaloSkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** @deprecated 战斗事件已统一迁移到 HaloRuntime；仅保留旧扩展调用的二进制入口。 */
@Deprecated
public final class DamageHandler {
    public static void executeActiveSkill(Player player, float multiplier) {
        if (player instanceof ServerPlayer serverPlayer) HaloSkills.fireAlice(serverPlayer, multiplier);
    }

    public static void applyHinaSkillDamage(Player player, LivingEntity target, float damage) {
        HaloDamage.deal(player, target, HaloAttackType.PHYSICAL, damage, .5f);
    }

    private DamageHandler() {}
}
