package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.SwordOfLightItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 光之剑的分层 OBJ 渲染器。
 *
 * <p>主物品 JSON 已经在调用本渲染器前应用全部 display 变换；这里仅处理模型内部的
 * 盖板动画和发光叠加，不能再次应用第一/第三人称变换。</p>
 */
public final class SwordOfLightRenderer extends BlockEntityWithoutLevelRenderer {
    private static final String MOD_ID = dream_sakura_blue_archive.MODID;

    private static final ResourceLocation RIGHT_STATIC = model("right_static");
    private static final ResourceLocation RIGHT_UPPER = model("right_upper");
    private static final ResourceLocation RIGHT_UPPER_SMALL = model("right_upper_small");
    private static final ResourceLocation RIGHT_LOWER = model("right_lower");
    private static final ResourceLocation RIGHT_LOWER_SMALL = model("right_lower_small");
    private static final ResourceLocation RIGHT_UPPER_ROD_HEAD = model("right_upper_rod_head");
    private static final ResourceLocation RIGHT_LOWER_ROD_HEAD = model("right_lower_rod_head");
    private static final ResourceLocation RIGHT_GLOW = model("right_glow");
    private static final ResourceLocation LEFT_STATIC = model("left_static");
    private static final ResourceLocation LEFT_UPPER = model("left_upper");
    private static final ResourceLocation LEFT_UPPER_SMALL = model("left_upper_small");
    private static final ResourceLocation LEFT_LOWER = model("left_lower");
    private static final ResourceLocation LEFT_LOWER_SMALL = model("left_lower_small");
    private static final ResourceLocation LEFT_UPPER_ROD_HEAD = model("left_upper_rod_head");
    private static final ResourceLocation LEFT_LOWER_ROD_HEAD = model("left_lower_rod_head");
    private static final ResourceLocation LEFT_GLOW = model("left_glow");

    private static final float ROD_HINGE_Y = 0.50F;
    private static final float ROD_HINGE_Z = 0.68F;


    public SwordOfLightRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static ResourceLocation[] additionalModels() {
        return new ResourceLocation[]{
                RIGHT_STATIC, RIGHT_UPPER, RIGHT_UPPER_SMALL,
                RIGHT_LOWER, RIGHT_LOWER_SMALL, RIGHT_UPPER_ROD_HEAD, RIGHT_LOWER_ROD_HEAD, RIGHT_GLOW,
                LEFT_STATIC, LEFT_UPPER, LEFT_UPPER_SMALL,
                LEFT_LOWER, LEFT_LOWER_SMALL, LEFT_UPPER_ROD_HEAD, LEFT_LOWER_ROD_HEAD, LEFT_GLOW
        };
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        BakedModel staticModel = getModel(leftHand ? LEFT_STATIC : RIGHT_STATIC);
        BakedModel upperModel = getModel(leftHand ? LEFT_UPPER : RIGHT_UPPER);
        BakedModel upperSmallModel = getModel(leftHand ? LEFT_UPPER_SMALL : RIGHT_UPPER_SMALL);
        BakedModel lowerModel = getModel(leftHand ? LEFT_LOWER : RIGHT_LOWER);
        BakedModel lowerSmallModel = getModel(leftHand ? LEFT_LOWER_SMALL : RIGHT_LOWER_SMALL);
        BakedModel upperRodHeadModel = getModel(leftHand ? LEFT_UPPER_ROD_HEAD : RIGHT_UPPER_ROD_HEAD);
        BakedModel lowerRodHeadModel = getModel(leftHand ? LEFT_LOWER_ROD_HEAD : RIGHT_LOWER_ROD_HEAD);
        BakedModel glowModel = getModel(leftHand ? LEFT_GLOW : RIGHT_GLOW);

        float charge = getCharge(stack);
        float wave = charge * charge * (3.0F - 2.0F * charge);
        // 最大开合约为模型自身厚度的四分之一，手持和 GUI 中都能清晰看到。
        float opening = wave * 0.050F;
        // 前端成对的小盖板行程稍大，避免被大盖板遮住而看不出动作。
        float smallOpening = wave * 0.065F;

        renderNormally(staticModel, stack, poseStack, buffers, packedLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.0F, opening, 0.0F);
        renderNormally(upperModel, stack, poseStack, buffers, packedLight, packedOverlay);
        // 小盖板继承大盖板的位移后，再进行自己的额外开合。
        poseStack.translate(0.0F, smallOpening, 0.0F);
        renderNormally(upperSmallModel, stack, poseStack, buffers, packedLight, packedOverlay);
        poseStack.pushPose();
        rotateAroundHinge(poseStack, wave * 0.22F);
        renderNormally(upperRodHeadModel, stack, poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0F, -opening, 0.0F);
        renderNormally(lowerModel, stack, poseStack, buffers, packedLight, packedOverlay);
        // 下侧同样先继承大盖板的下移，再额外向下张开小盖板。
        poseStack.translate(0.0F, -smallOpening, 0.0F);
        renderNormally(lowerSmallModel, stack, poseStack, buffers, packedLight, packedOverlay);
        poseStack.pushPose();
        rotateAroundHinge(poseStack, -wave * 0.22F);
        renderNormally(lowerRodHeadModel, stack, poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();

        // 发光部件先参与正常光照，确保白天和 GUI 中仍保留原贴图细节。
        renderNormally(glowModel, stack, poseStack, buffers, packedLight, packedOverlay);
        // 再追加不受环境亮度影响的青白呼吸光。
        renderGlow(glowModel, poseStack, buffers, packedOverlay, 0.28F + wave * 0.62F);
        renderDisplays(poseStack, buffers, charge);
    }

    private static float getCharge(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isUsingItem()
                || minecraft.player.getUseItem() != stack) {
            return 0.0F;
        }
        return Math.min(1.0F, (minecraft.player.getTicksUsingItem() + minecraft.getFrameTime())
                / SwordOfLightItem.CHARGE_TICKS);
    }

    private static void renderDisplays(PoseStack poseStack, MultiBufferSource buffers, float progress) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        PoseStack.Pose pose = poseStack.last();
        int alpha = 110 + (int) (145.0F * progress);
        // 中部蓝色长条：沿剑身 Z 方向逐渐点亮。
        quadY(pose, consumer, -0.066F, 0.066F, 0.050F, 0.050F + 0.239F * progress,
                0.5442F, 75, 220, 255, alpha);
        // 尾部两侧进度条与三位数表。
        for (float side : new float[]{-0.1185F, 0.1185F}) {
            quadX(pose, consumer, side, 0.376F, 0.392F, -0.552F, -0.552F + 0.130F * progress,
                    65, 210, 255, alpha);
            renderNumber(pose, consumer, side, Math.round(progress * 100.0F), alpha);
        }
    }

    private static final int[] DIGITS = {0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F};

    private static void renderNumber(PoseStack.Pose pose, VertexConsumer consumer, float x, int value, int alpha) {
        int[] digits = {value / 100, value / 10 % 10, value % 10};
        for (int index = 0; index < 3; index++) {
            float z = -0.545F + index * 0.040F;
            int mask = DIGITS[digits[index]];
            segment(pose, consumer, x, mask, 0, 0.448F, z + 0.004F, 0.452F, z + 0.030F, alpha);
            segment(pose, consumer, x, mask, 1, 0.425F, z + 0.027F, 0.448F, z + 0.031F, alpha);
            segment(pose, consumer, x, mask, 2, 0.400F, z + 0.027F, 0.424F, z + 0.031F, alpha);
            segment(pose, consumer, x, mask, 3, 0.396F, z + 0.004F, 0.400F, z + 0.030F, alpha);
            segment(pose, consumer, x, mask, 4, 0.400F, z + 0.003F, 0.424F, z + 0.007F, alpha);
            segment(pose, consumer, x, mask, 5, 0.425F, z + 0.003F, 0.448F, z + 0.007F, alpha);
            segment(pose, consumer, x, mask, 6, 0.422F, z + 0.004F, 0.426F, z + 0.030F, alpha);
        }
    }

    private static void segment(PoseStack.Pose pose, VertexConsumer consumer, float x, int mask,
                                int bit, float y1, float z1, float y2, float z2, int alpha) {
        if ((mask & (1 << bit)) != 0) quadX(pose, consumer, x, y1, y2, z1, z2, 90, 235, 255, alpha);
    }

    private static void quadY(PoseStack.Pose pose, VertexConsumer c, float x1, float x2, float z1, float z2,
                              float y, int r, int g, int b, int a) {
        vertex(c, pose, x1, y, z1, r, g, b, a); vertex(c, pose, x1, y, z2, r, g, b, a);
        vertex(c, pose, x2, y, z2, r, g, b, a); vertex(c, pose, x2, y, z1, r, g, b, a);
    }

    private static void quadX(PoseStack.Pose pose, VertexConsumer c, float x, float y1, float y2, float z1, float z2,
                              int r, int g, int b, int a) {
        vertex(c, pose, x, y1, z1, r, g, b, a); vertex(c, pose, x, y2, z1, r, g, b, a);
        vertex(c, pose, x, y2, z2, r, g, b, a); vertex(c, pose, x, y1, z2, r, g, b, a);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z,
                               int r, int g, int b, int a) {
        c.vertex(pose.pose(), x, y, z).color(r, g, b, a).endVertex();
    }

    private static ResourceLocation model(String part) {
        return new ResourceLocation(MOD_ID, "item/tendouaris_sword_of_light_" + part);
    }

    private static void rotateAroundHinge(PoseStack poseStack, float angle) {
        poseStack.translate(0.0F, ROD_HINGE_Y, ROD_HINGE_Z);
        poseStack.mulPose(Axis.XP.rotation(angle));
        poseStack.translate(0.0F, -ROD_HINGE_Y, -ROD_HINGE_Z);
    }

    private static BakedModel getModel(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getModel(location);
    }

    private static void renderNormally(BakedModel model, ItemStack stack, PoseStack poseStack,
                                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        for (BakedModel pass : model.getRenderPasses(stack, true)) {
            pass.getRenderTypes(stack, true).forEach(renderType -> {
                VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(
                        buffers, renderType, true, stack.hasFoil());
                renderer.renderModelLists(pass, stack, packedLight, packedOverlay, poseStack, consumer);
            });
        }
    }

    private static void renderGlow(BakedModel model, PoseStack poseStack,
                                   MultiBufferSource buffers, int packedOverlay, float strength) {
        VertexConsumer consumer = buffers.getBuffer(Sheets.translucentItemSheet());
        PoseStack.Pose pose = poseStack.last();
        RandomSource random = RandomSource.create();

        for (BakedModel pass : model.getRenderPasses(ItemStack.EMPTY, true)) {
            for (Direction direction : Direction.values()) {
                random.setSeed(42L);
                renderGlowQuads(pass.getQuads(null, direction, random), pose, consumer, packedOverlay, strength);
            }
            random.setSeed(42L);
            renderGlowQuads(pass.getQuads(null, null, random), pose, consumer, packedOverlay, strength);
        }
    }

    private static void renderGlowQuads(Iterable<BakedQuad> quads, PoseStack.Pose pose,
                                        VertexConsumer consumer, int packedOverlay, float strength) {
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad,
                    0.72F * strength, 0.92F * strength, strength, strength,
                    LightTexture.FULL_BRIGHT, packedOverlay, true);
        }
    }
}