package com.core.dream_sakura_blue_archive.ciorastao.effect;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
//
public class RegistryEffect {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, dream_sakura_blue_archive.MODID);

    public static final RegistryObject<MobEffect> STRENGTHEN_EFFECT =
            EFFECTS.register("strengthen", StrengthenEffect::new);

    public static final RegistryObject<MobEffect> PERCENT_REGENERATION_EFFECT =
            EFFECTS.register("percent_regeneration", PercentRegenerationEffect::new);

    public static final RegistryObject<MobEffect> STUN_EFFECT =
            EFFECTS.register("stun", StunEffect::new);
}
