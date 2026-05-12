package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.model.GeoModel;

public class DecorationModel extends GeoModel<DecorationItem> {

    static final ThreadLocal<ItemDisplayContext> CURRENT_CONTEXT = ThreadLocal.withInitial(() -> ItemDisplayContext.NONE);

    public static void setCurrentContext(ItemDisplayContext context) {
        CURRENT_CONTEXT.set(context);
    }

    @Override
    public ResourceLocation getModelResource(DecorationItem animatable) {
        String itemId = animatable.getItemId();
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "geo/" + itemId + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DecorationItem animatable) {
        String itemId = animatable.getItemId();
        ItemDisplayContext context = CURRENT_CONTEXT.get();

        // 如果是GUI显示，尝试使用单独的gui文件夹和_gui后缀
        if (context == ItemDisplayContext.GUI) {
            String guiTexturePath = "textures/item/gui/" + itemId + "_gui.png";
            ResourceLocation guiTextureLoc = ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, guiTexturePath);

            // 检查资源是否存在，如果不存在则回退到普通纹理
            if (Minecraft.getInstance().getResourceManager().getResource(guiTextureLoc).isPresent()) {
                return guiTextureLoc;
            }
        }

        // 默认使用普通纹理
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "textures/item/" + itemId + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(DecorationItem animatable) {
        String itemId = animatable.getItemId();
        return ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "animations/" + itemId + ".animation.json");
    }

}
