package com.core.dream_sakura_blue_archive.ciorastao.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

public class StrengthenEffect extends MobEffect {
    public StrengthenEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3366FF);
    }

    @Override
    public void applyEffectTick(@Nonnull LivingEntity entity, int amplifier) {
    }

    @Override
    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        return false;
    }
}
