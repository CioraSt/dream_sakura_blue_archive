package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class GeckoCurioRendererY<T extends LivingEntity> implements ICurioRenderer {
    private final GeoItemRenderer<?> geoRenderer;


    public GeckoCurioRendererY(GeoItemRenderer<?> geoRenderer) {
        this.geoRenderer = geoRenderer;
    }

    @Override
    public <
            L extends LivingEntity,
            M extends EntityModel<L>
            > void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack matrixStack,
            RenderLayerParent<L, M> renderLayerParent,
            MultiBufferSource renderTypeBuffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        // LivingEntity entity = slotContext.entity();
        // float haedY = entity.getEyeHeight() - (entity.isBaby() ? 0.1F : 0.0F);
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.XP.rotationDegrees(180.0F));
//            matrixStack.mulPose(Axis.XP.rotationDegrees(0.0F));
//            matrixStack.mulPose(Axis.ZP.rotationDegrees(1.0F));
        matrixStack.translate(-0.50D, 0.0D, -0.5D);//左右，上下，前后

        geoRenderer.renderByItem(stack, ItemDisplayContext.HEAD, matrixStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }


}