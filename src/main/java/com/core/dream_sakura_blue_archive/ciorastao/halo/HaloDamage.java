package com.core.dream_sakura_blue_archive.ciorastao.halo;

import com.core.dream_sakura.api.combat.DamageBypass;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** 唯一的光环伤害出口：保留死亡归因、伤害事件和其他模组兼容性。 */
public final class HaloDamage {
    private static final ThreadLocal<Integer> INTERNAL_DEPTH = ThreadLocal.withInitial(() -> 0);

    public static boolean isInternal() {
        return INTERNAL_DEPTH.get() > 0;
    }

    public static void deal(Player attacker, LivingEntity target, HaloAttackType type,
                            float amount, float penetration) {
        if (amount <= 0 || !target.isAlive() || attacker.level().isClientSide) return;
        if (target instanceof net.minecraft.server.level.ServerPlayer player) {
            if (HaloRuntime.blocksIncoming(player)) return;
            HaloRuntime.recordIncoming(player);
            amount *= HaloRuntime.incomingMultiplier(player);
        }
        float clampedPenetration = Math.max(0, Math.min(1, penetration));
        float normal = amount * (1 - clampedPenetration);
        float bypass = amount * clampedPenetration;

        float resistance = target instanceof Player player
                ? HaloProfile.equipped(player).map(e -> e.profile().resistance(type)).orElse(0f)
                : 0f;
        normal *= 1 - Math.max(0, Math.min(.999f, resistance));

        INTERNAL_DEPTH.set(INTERNAL_DEPTH.get() + 1);
        try {
            if (normal > 0 && target.isAlive()) {
                target.hurt(source(attacker, type), normal);
            }
            if (bypass > 0 && target.isAlive()) {
                if (target instanceof net.minecraft.server.level.ServerPlayer player
                        && HaloRuntime.preventLethalBypass(player, attacker, bypass)) return;
                DamageSource source = source(attacker, type);
                DamageBypass.bypassAll(source);
                target.invulnerableTime = 0;
                target.hurt(source, bypass);
            }
        } finally {
            int depth = INTERNAL_DEPTH.get() - 1;
            if (depth <= 0) INTERNAL_DEPTH.remove();
            else INTERNAL_DEPTH.set(depth);
        }
    }

    private static DamageSource source(Player player, HaloAttackType type) {
        return switch (type) {
            case PHYSICAL -> player.damageSources().playerAttack(player);
            case MAGIC -> player.damageSources().indirectMagic(player, player);
            case EXPLOSION -> player.damageSources().explosion(player, player);
        };
    }

    private HaloDamage() {}
}
