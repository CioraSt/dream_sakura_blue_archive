package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloVariantHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import javax.annotation.Nullable;

/**
 * 装饰物品的渲染器类，继承自GeoItemRenderer，专门用于处理装饰物品的渲染逻辑
 */
public class DecorationRenderer extends GeoItemRenderer<DecorationItem> {
    /**
     * 构造函数，初始化装饰渲染器
     */
    public DecorationRenderer() {
        super(new DecorationModel());
    }

    /**
     * 根据物品进行渲染的方法
     *
     * @param stack         要渲染的物品堆栈
     * @param transformType 物品显示上下文
     * @param poseStack     姿态堆栈，用于处理位置和旋转
     * @param bufferSource  多缓冲源，用于获取渲染缓冲区
     * @param packedLight   打包的光照信息
     * @param packedOverlay 打包的叠加层信息
     */
    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {

        // 检查物品是否为装饰物品实例，如果不是则直接返回
        if (!(stack.getItem() instanceof DecorationItem item)) {
            return;
        }

        // 保存当前状态
        poseStack.pushPose();

        // 使用try-finally块确保poseStack状态正确恢复
        try {
            if (item.getGlowColor().length != 0) {
                // 根据显示上下文决定渲染顺序：第一人称时先渲染发光层，其他情况后渲染
                if (transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                    // 第一人称视角：先渲染发光层
                renderGlowLayer(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay, item);
                    // 再进行基础渲染
                    super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
                } else {
                    // 其他视角：先进行基础渲染
                    super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
                    // 再渲染发光层
                    renderGlowLayer(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay, item);
                }
            } else {
                // 没有发光颜色配置时，直接进行基础渲染
                super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
            }
        } finally {
            // 确保无论如何都会恢复poseStack的状态
            poseStack.popPose();
        }
    }

    /**
     * 渲染发光层的私有方法
     *
     * @param stack         物品堆栈对象
     * @param transformType 物品显示上下文
     * @param poseStack     姿态堆栈，用于变换
     * @param bufferSource  多缓冲源，用于获取渲染缓冲区
     * @param packedLight   打包的光照信息
     * @param packedOverlay 打包的叠加信息
     * @param item          装饰品项
     */
    private void renderGlowLayer(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            DecorationItem item
    ) {
        // 获取发光纹理资源，如果为null则直接返回
        ResourceLocation glowTexture = getGlowTextureResource(stack, item);
        if (glowTexture == null) return;

        // 获取发光颜色和强度
        float[] glowColor = item.getGlowColor();
        float glowIntensity = item.getGlowIntensity();

        // 获取烘焙的几何模型
        BakedGeoModel model = this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(item, this));

        poseStack.pushPose();
        try {
            // 将模型偏移到中心位置并略微上移
            poseStack.translate(0.5, 0.5 + 0.01, 0.5); //偏移
            boolean planarHalo = isSinglePlaneModel(model);
            if (planarHalo) {
                RenderType depthRenderType = Renders.HALO_DEPTH_MASK.apply(glowTexture);
                VertexConsumer depthBuffer = bufferSource.getBuffer(depthRenderType);
                this.reRender(
                        model,
                        poseStack,
                        bufferSource,
                        item,
                        depthRenderType,
                        depthBuffer,
                        0,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        1,
                        1,
                        1,
                        1
                );

                // Ensure the mask reaches the depth buffer before drawing color.
                // Flush only this RenderType so unrelated entity batches stay intact.
                if (bufferSource instanceof MultiBufferSource.BufferSource immediateBufferSource) {
                    immediateBufferSource.endBatch(depthRenderType);
                }
            }
            RenderType glowRenderType = planarHalo
                    ? Renders.HALO_GLOW_COLOR.apply(glowTexture)
                    : Renders.MODELED_HALO_GLOW.apply(glowTexture);
            VertexConsumer glowBuffer = bufferSource.getBuffer(glowRenderType);
            // A zero-thickness cube contains two coincident faces. Culling keeps
            // only the camera-facing one; two identical color submissions retain
            // the original additive brightness without reintroducing z-fighting.
            int colorPassCount = planarHalo ? 2 : 1;
            for (int pass = 0; pass < colorPassCount; pass++) {
                this.reRender(
                        model,
                        poseStack,
                        bufferSource,
                        item,
                        glowRenderType,
                        glowBuffer,
                        0,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        glowColor[0] * glowIntensity,
                        glowColor[1] * glowIntensity,
                        glowColor[2] * glowIntensity,
                        1
                );
            }
        } finally {
            // 确保恢复之前的姿态状态
            poseStack.popPose();
        }
    }

    private static boolean isSinglePlaneModel(BakedGeoModel model) {
        if (model.topLevelBones().size() != 1) return false;

        GeoBone bone = model.topLevelBones().get(0);
        if (!bone.getChildBones().isEmpty() || bone.getCubes().size() != 1) return false;

        GeoCube cube = bone.getCubes().get(0);
        int flatAxes = 0;
        if (Math.abs(cube.size().x) < 1.0E-6) flatAxes++;
        if (Math.abs(cube.size().y) < 1.0E-6) flatAxes++;
        if (Math.abs(cube.size().z) < 1.0E-6) flatAxes++;
        return flatAxes == 1;
    }

    private ResourceLocation getGlowTextureResource(ItemStack stack, DecorationItem item) {
        String itemId = item.getEffectiveItemId(stack);
        if (itemId.equals(item.getItemId())) {
            itemId = item.getGlowTextureId();
        } else {
            itemId = HaloVariantHelper.baseItemId(itemId);
        }
        String glowPath = "textures/item/glow/" + itemId + "_glow.png";
        
        return ResourceLocation.fromNamespaceAndPath(
                dream_sakura_blue_archive.MODID,
                glowPath
        );
    }

    @Override
    public RenderType getRenderType(DecorationItem animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
