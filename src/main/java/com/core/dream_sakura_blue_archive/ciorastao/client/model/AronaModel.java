package com.core.dream_sakura_blue_archive.ciorastao.client.model;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.entity.AronaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AronaModel extends GeoModel<AronaEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            dream_sakura_blue_archive.MODID, "geo/arona.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            dream_sakura_blue_archive.MODID, "textures/entity/arona.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            dream_sakura_blue_archive.MODID, "animations/arona.animation.json");

    @Override
    public ResourceLocation getModelResource(AronaEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AronaEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AronaEntity animatable) {
        return ANIMATION;
    }
}
