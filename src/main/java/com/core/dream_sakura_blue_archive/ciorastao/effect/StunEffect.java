package com.core.dream_sakura_blue_archive.ciorastao.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public class StunEffect extends MobEffect {
    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0x808080); // 灰色表示眩晕
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity entity, int amplifier) {
        if (entity instanceof Monster monster) {
            monster.setNoAi(true);
        } else if (entity instanceof Player player) {
            // 禁止移动和跳跃
            if (!player.level().isClientSide) {
                // 在服务端设置玩家动作为0，这会影响客户端表现
                player.setDeltaMovement(player.getDeltaMovement().multiply(0, 1, 0)); // 只保留Y轴动量，防止卡在空中
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(@Nonnull LivingEntity entity, @Nonnull AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Monster monster) {
            monster.setNoAi(false);
        } else if (entity instanceof Player player) {
            // 恢复玩家正常移动
        }
    }

    @Override
    public void addAttributeModifiers(@Nonnull LivingEntity entity, @Nonnull AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        if (entity instanceof Monster monster) {
            monster.setNoAi(true);
        } else if (entity instanceof Player player) {
            // 禁止移动和跳跃
            if (!player.level().isClientSide) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(0, 1, 0));
            }
        }
    }
}
