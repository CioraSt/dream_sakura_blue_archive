package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class MikaObjCurioRenderer implements ICurioRenderer {
    @Override
    public <L extends LivingEntity, M extends EntityModel<L>> void render(
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
        matrixStack.pushPose();
        matrixStack.translate(1.0D, 0.7D, 0.8D);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
        matrixStack.scale(2.0F, 2.0F, 2.0F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.HEAD,
                light,
                OverlayTexture.NO_OVERLAY,
                matrixStack,
                renderTypeBuffer,
                slotContext.entity().level(),
                0
        );
        matrixStack.popPose();
    }
}