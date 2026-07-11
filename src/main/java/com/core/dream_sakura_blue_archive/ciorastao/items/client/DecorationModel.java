package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

public class DecorationModel extends GeoModel<DecorationItem> {

    @Override
    public ResourceLocation getModelResource(DecorationItem animatable, GeoRenderer<DecorationItem> renderer) {
        return modelResource(effectiveItemId(animatable, renderer));
    }

    @Override
    public ResourceLocation getModelResource(DecorationItem animatable) {
        return modelResource(animatable.getItemId());
    }

    @Override
    public ResourceLocation getTextureResource(DecorationItem animatable, GeoRenderer<DecorationItem> renderer) {
        return textureResource(effectiveItemId(animatable, renderer));
    }

    @Override
    public ResourceLocation getTextureResource(DecorationItem animatable) {
        return textureResource(animatable.getItemId());
    }

    @Override
    public ResourceLocation getAnimationResource(DecorationItem animatable) {
        String itemId = animatable.getItemId();
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "animations/" + itemId + ".animation.json");
    }

    private static ResourceLocation modelResource(String itemId) {
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "geo/" + itemId + ".geo.json");
    }

    private static ResourceLocation textureResource(String itemId) {
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "textures/item/" + itemId + ".png");
    }

    private static String effectiveItemId(DecorationItem item, GeoRenderer<DecorationItem> renderer) {
        if (renderer instanceof GeoItemRenderer<?> itemRenderer) {
            ItemStack stack = itemRenderer.getCurrentItemStack();
            if (stack != null && !stack.isEmpty()) {
                return item.getEffectiveItemId(stack);
            }
        }
        return item.getItemId();
    }

}
