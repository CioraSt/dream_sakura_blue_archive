package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DecorationModel extends GeoModel<DecorationItem> {

    @Override
    public ResourceLocation getModelResource(DecorationItem animatable) {
        String itemId = animatable.getItemId();
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "geo/" + itemId + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DecorationItem animatable) {
        String itemId = animatable.getItemId();
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "textures/item/" + itemId + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(DecorationItem animatable) {
        String itemId = animatable.getItemId();
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "animations/" + itemId + ".animation.json");
    }

}
