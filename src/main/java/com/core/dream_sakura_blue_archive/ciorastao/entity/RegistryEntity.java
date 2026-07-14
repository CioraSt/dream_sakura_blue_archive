package com.core.dream_sakura_blue_archive.ciorastao.entity;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RegistryEntity {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, dream_sakura_blue_archive.MODID);

    public static final RegistryObject<EntityType<AronaEntity>> ARONA = ENTITY_TYPES.register("arona", () ->
            EntityType.Builder.of(AronaEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("arona")
    );

    public static final RegistryObject<EntityType<AronaProjectile>> ARONA_PROJECTILE = ENTITY_TYPES.register(
            "arona_projectile", () ->
                    EntityType.Builder.<AronaProjectile>of(AronaProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("arona_projectile")
    );

    private RegistryEntity() {
    }

    @Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Attributes {
        private Attributes() {
        }

        @SubscribeEvent
        public static void register(EntityAttributeCreationEvent event) {
            event.put(ARONA.get(), AronaEntity.createAttributes().build());
        }
    }
}
