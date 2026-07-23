package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.SwordOfLightItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
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
    private static final ResourceLocation RIGHT_MUZZLE = model("right_muzzle");
    private static final ResourceLocation RIGHT_CORE = model("right_core");
    private static final ResourceLocation LEFT_STATIC = model("left_static");
    private static final ResourceLocation LEFT_UPPER = model("left_upper");
    private static final ResourceLocation LEFT_UPPER_SMALL = model("left_upper_small");
    private static final ResourceLocation LEFT_LOWER = model("left_lower");
    private static final ResourceLocation LEFT_LOWER_SMALL = model("left_lower_small");
    private static final ResourceLocation LEFT_UPPER_ROD_HEAD = model("left_upper_rod_head");
    private static final ResourceLocation LEFT_LOWER_ROD_HEAD = model("left_lower_rod_head");
    private static final ResourceLocation LEFT_GLOW = model("left_glow");
    private static final ResourceLocation LEFT_MUZZLE = model("left_muzzle");
    private static final ResourceLocation LEFT_CORE = model("left_core");

    private static final float ROD_HINGE_Y = 0.50F;
    private static final float ROD_HINGE_Z = 0.68F;
    private static final float MUZZLE_AXIS_Y = 0.4476265F;
    private static final float CORE_AXIS_Y = 0.389264F;
    private static final float FULL_ROTATION = (float) (Math.PI * 2.0D);
    private static final int SECOND_STAGE_TICKS = 200; // 10 秒
    private static final float ROTATION_RADIANS_PER_SECOND = FULL_ROTATION * 0.75F;
    private static final float SECOND_STAGE_ROTATION_MULTIPLIER = 2.0F;
    private static final float INERTIA_DURATION_SECONDS = 1.25F;

    // 渲染器是客户端单例；这些值让松开右键后仍能继续渲染平滑回退，而不是瞬间回到 0。
    private static float animationCharge;
    private static float rotationAngle;
    private static float angularVelocity;
    private static float inertiaDeceleration;
    private static float displayFade;
    private static float visualStage1;
    private static float visualStage2;
    private static float secondStageHoldTicks;
    private static int displayNumber;
    private static long lastAnimationMillis = -1L;
    private static boolean wasUsing;


    public SwordOfLightRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static ResourceLocation[] additionalModels() {
        return new ResourceLocation[]{
                RIGHT_STATIC, RIGHT_UPPER, RIGHT_UPPER_SMALL,
                RIGHT_LOWER, RIGHT_LOWER_SMALL, RIGHT_UPPER_ROD_HEAD, RIGHT_LOWER_ROD_HEAD, RIGHT_GLOW,
                RIGHT_MUZZLE, RIGHT_CORE,
                LEFT_STATIC, LEFT_UPPER, LEFT_UPPER_SMALL,
                LEFT_LOWER, LEFT_LOWER_SMALL, LEFT_UPPER_ROD_HEAD, LEFT_LOWER_ROD_HEAD, LEFT_GLOW,
                LEFT_MUZZLE, LEFT_CORE
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
        BakedModel muzzleModel = getModel(leftHand ? LEFT_MUZZLE : RIGHT_MUZZLE);
        BakedModel coreModel = getModel(leftHand ? LEFT_CORE : RIGHT_CORE);

        AnimationState animation = updateAnimation();
        float charge = animation.charge();
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
        // 炮口和中间圆柱体从 glow 中独立出来，蓄力时绕自身中心轴旋转。
        poseStack.pushPose();
        rotateAroundPartAxis(poseStack, MUZZLE_AXIS_Y, animation.rotationAngle());
        renderNormally(muzzleModel, stack, poseStack, buffers, packedLight, packedOverlay);
        renderMuzzleSideGlow(poseStack, buffers, animation);
        poseStack.popPose();
        poseStack.pushPose();
        rotateAroundPartAxis(poseStack, CORE_AXIS_Y, -animation.rotationAngle());
        renderNormally(coreModel, stack, poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();
        // 动态发光不再给整个 OBJ 染色，而只覆盖指定的灯条、尾弧和数字区域。
        renderEnergyDisplays(poseStack, buffers, leftHand, animation);
    }

    private static AnimationState updateAnimation() {
        Minecraft minecraft = Minecraft.getInstance();
        long now = Util.getMillis();
        if (lastAnimationMillis < 0L) {
            lastAnimationMillis = now;
        }
        // 避免切屏/暂停后一次性跳很远，也避免左右手两次渲染重复推进状态。
        float seconds = Math.min(0.1F, Math.max(0.0F, (now - lastAnimationMillis) / 1000.0F));
        lastAnimationMillis = now;

        boolean using = minecraft.player != null && minecraft.player.isUsingItem()
                && minecraft.player.getUseItem().getItem() instanceof SwordOfLightItem;
        if (using) {
            animationCharge = Math.min(1.0F, animationCharge
                    + seconds * 20.0F / SwordOfLightItem.CHARGE_TICKS);
            // 不再依赖本次右键的 getTicksUsingItem()：松开后再次按下会从当前进度续充。
            if (animationCharge >= 0.999F) {
                secondStageHoldTicks += seconds * 20.0F;
            }
            visualStage2 = saturate((secondStageHoldTicks
                    - (SECOND_STAGE_TICKS - SwordOfLightItem.CHARGE_TICKS)) / 40.0F);
            float speed = ROTATION_RADIANS_PER_SECOND;
            if (visualStage2 > 0.0F) {
                speed *= SECOND_STAGE_ROTATION_MULTIPLIER;
            }
            angularVelocity = speed;
            rotationAngle = wrapRotation(rotationAngle + seconds * angularVelocity);
        } else {
            // 松键后上下盖板以蓄力速度的 1.5 倍回退；旋转角度不回退。
            animationCharge = Math.max(0.0F, animationCharge
                    - seconds * 20.0F * 1.5F / SwordOfLightItem.CHARGE_TICKS);
            if (animationCharge <= 0.0F) {
                // 完全合拢才清空二段计时；回退途中重新按键仍可从当前状态继续。
                secondStageHoldTicks = 0.0F;
                visualStage2 = 0.0F;
            }
            if (wasUsing && angularVelocity > 0.0F) {
                inertiaDeceleration = angularVelocity / INERTIA_DURATION_SECONDS;
            }
            if (angularVelocity > 0.0F) {
                float nextVelocity = Math.max(0.0F,
                        angularVelocity - inertiaDeceleration * seconds);
                // 使用前后速度的平均值积分，使最后一段惯性减速也保持平滑。
                rotationAngle = wrapRotation(rotationAngle
                        + seconds * (angularVelocity + nextVelocity) * 0.5F);
                angularVelocity = nextVelocity;
            }
        }
        visualStage1 = animationCharge;
        displayFade = animationCharge;
        // 数字始终跟随盖板开合比例，松键同步退回 000，再按键从当前数字继续。
        // 数字直接按线性进度计数，不再在起步和回退时使用 smoothstep 渐变。
        float chargedNumber = 520.0F + 479.0F * visualStage2;
        displayNumber = Math.round(chargedNumber * animationCharge);
        wasUsing = using;
        return new AnimationState(animationCharge, rotationAngle, visualStage1,
                visualStage2, displayFade, displayNumber);
    }

    private static float wrapRotation(float angle) {
        return angle % FULL_ROTATION;
    }

    private record AnimationState(float charge, float rotationAngle, float stage1,
                                  float stage2, float fade, int number) {}

    private static void renderMuzzleSideGlow(PoseStack poseStack, MultiBufferSource buffers,
                                             AnimationState animation) {
        float blueProgress = remap(animation.stage1(), 0.55F, 1.00F);
        float whiteProgress = remap(animation.stage2(), 0.50F, 1.00F);
        if (blueProgress <= 0.0F && whiteProgress <= 0.0F) return;
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        PoseStack.Pose pose = poseStack.last();

        // 由 OBJ UV 对应蓝色像素得到的炮口十边形侧壁。只绘制侧面，不覆盖炮口正面。
        float[][] ring = {
                {-0.066488F, 0.447626F}, {-0.053789F, 0.408546F},
                {-0.020546F, 0.384394F}, {0.020546F, 0.384394F},
                {0.053789F, 0.408546F}, {0.066488F, 0.447626F},
                {0.053789F, 0.486707F}, {0.020546F, 0.510859F},
                {-0.020546F, 0.510859F}, {-0.053789F, 0.486707F}
        };
        float[][] patches = {
                // z1、z2、侧面局部起点、侧面局部终点。每面蓝色都是中央方片。
                {0.475547F, 0.488417F, 0.05F, 0.75F},
                {0.516213F, 0.533003F, 0.05F, 0.75F},
                {0.563779F, 0.586500F, 0.25F, 0.95F}
        };
        renderMuzzleBands(pose, consumer, ring, patches, blueProgress, blueColor(animation));
        // 白色二段覆盖在已经点亮的蓝色侧壁上，不再关闭蓝光后重新开始。
        renderMuzzleBands(pose, consumer, ring, patches, whiteProgress, whiteColor(animation));
    }

    private static void renderMuzzleBands(PoseStack.Pose pose, VertexConsumer consumer,
                                          float[][] ring, float[][] patches, float progress,
                                          GlowColor color) {
        int litBands = Math.min(patches.length, (int) Math.ceil(progress * patches.length));
        // 只做约 0.0001 的防共面偏移，面积保持在原蓝色贴图边界内。
        float radialScale = 1.0005F;
        for (int band = 0; band < litBands; band++) {
            for (int index = 0; index < ring.length; index++) {
                float[] first = ring[index];
                float[] second = ring[(index + 1) % ring.length];
                float start = patches[band][2];
                float end = patches[band][3];
                float x1 = lerp(first[0], second[0], start);
                float y1 = lerp(first[1], second[1], start);
                float x2 = lerp(first[0], second[0], end);
                float y2 = lerp(first[1], second[1], end);
                quadSide(pose, consumer,
                        x1 * radialScale,
                        MUZZLE_AXIS_Y + (y1 - MUZZLE_AXIS_Y) * radialScale,
                        x2 * radialScale,
                        MUZZLE_AXIS_Y + (y2 - MUZZLE_AXIS_Y) * radialScale,
                        patches[band][0], patches[band][1], color);
            }
        }
    }

    private static void renderEnergyDisplays(PoseStack poseStack, MultiBufferSource buffers,
                                             boolean leftHand, AnimationState animation) {
        if (animation.fade() <= 0.0F) return;
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());
        PoseStack.Pose pose = poseStack.last();
        GlowColor blue = blueColor(animation);
        GlowColor white = whiteColor(animation);
        float upperBlue = remap(animation.stage1(), 0.00F, 0.55F);
        float lowerBlue = remap(animation.stage1(), 0.08F, 0.60F);
        float upperWhite = remap(animation.stage2(), 0.00F, 0.50F);
        float lowerWhite = remap(animation.stage2(), 0.05F, 0.55F);

        renderSegmentedSideStrips(pose, consumer, upperBlue, blue);
        renderLowerStrips(pose, consumer, lowerBlue, blue);
        renderSegmentedSideStrips(pose, consumer, upperWhite, white);
        renderLowerStrips(pose, consumer, lowerWhite, white);

        // 每一套左右手模型都在自身两侧显示尾部弧线、进度和数字。
        for (float side : new float[]{-0.118045F, 0.118045F}) {
            // 发光层沿显示面的外法线略微抬起，避免与 OBJ 原贴图共面而闪烁。
            float displayX = side + Math.copySign(0.00020F, side);
            renderTailArc(pose, consumer, displayX, animation.stage1(), blue);
            // 二段白光继续覆盖晶体管右侧进度段，不替换已经点亮的蓝光。
            renderTailArc(pose, consumer, displayX, animation.stage2(), white);
            renderNumber(pose, consumer, displayX, animation.number(), leftHand,
                    animation.stage2() > 0.0F
                            ? stableWhiteColor(animation)
                            : stableBlueColor(animation));
        }
    }

    private static void renderSegmentedSideStrips(PoseStack.Pose pose, VertexConsumer consumer,
                                                  float progress, GlowColor color) {
        int segments = 8;
        int lit = Math.min(segments, (int) Math.ceil(progress * segments));
        float startZ = 0.049F;
        float step = 0.03184F;
        for (int index = 0; index < lit; index++) {
            float z1 = startZ + index * step;
            float z2 = z1 + 0.0194F;
            quadX(pose, consumer, -0.07075F, 0.5310F, 0.5437F, z1, z2,
                    color.r(), color.g(), color.b(), color.a());
            quadX(pose, consumer, 0.07075F, 0.5310F, 0.5437F, z1, z2,
                    color.r(), color.g(), color.b(), color.a());
        }
    }

    private static void renderLowerStrips(PoseStack.Pose pose, VertexConsumer consumer,
                                          float progress, GlowColor color) {
        if (progress <= 0.0F) return;
        // 由贴图蓝色像素反查 UV 后得到的原模型长条真实边界。
        float z1 = 0.126191F;
        float z2 = z1 + (0.336993F - z1) * progress;
        for (float side : new float[]{-0.05675F, 0.05675F}) {
            quadX(pose, consumer, side, 0.468746F, 0.486305F, z1, z2,
                    color.r(), color.g(), color.b(), color.a());
        }
    }

    private static void renderTailArc(PoseStack.Pose pose, VertexConsumer consumer, float x,
                                      float progress, GlowColor color) {
        // 009/021 原始几何每段由两个三角形组成。这里按共同对角线重组为真实四边形，
        // 避免 lightning 的 QUADS 拓扑丢弃上一版的退化三角形。
        float[][][] quads = {
                {{0.404840F,-0.481033F},{0.411300F,-0.482315F},{0.411300F,-0.494897F},{0.404833F,-0.494897F}},
                {{0.405644F,-0.467577F},{0.412087F,-0.470143F},{0.411365F,-0.480465F},{0.404900F,-0.479182F}},
                {{0.406823F,-0.453957F},{0.414752F,-0.459858F},{0.412399F,-0.468326F},{0.405785F,-0.465730F}},
                {{0.411097F,-0.442869F},{0.420609F,-0.452032F},{0.415583F,-0.458245F},{0.407258F,-0.452177F}},
                {{0.417024F,-0.431220F},{0.428172F,-0.445172F},{0.421887F,-0.450700F},{0.411876F,-0.441190F}},
                {{0.423900F,-0.419417F},{0.436997F,-0.438446F},{0.429608F,-0.444004F},{0.417918F,-0.429598F}}
        };
        int litSegments = Math.min(quads.length, (int) Math.ceil(progress * quads.length));
        for (int index = 0; index < litSegments; index++) {
            polygonQuadX(pose, consumer, x, quads[index], color);
        }
    }

    private static void polygonQuadX(PoseStack.Pose pose, VertexConsumer consumer, float x,
                                     float[][] quad, GlowColor color) {
        // 正反两面各提交一个完整四边形。
        for (int index : new int[]{0, 1, 2, 3, 3, 2, 1, 0}) {
            vertex(consumer, pose, x, quad[index][0], quad[index][1],
                    color.r(), color.g(), color.b(), color.a());
        }
    }

    private static GlowColor blueColor(AnimationState animation) {
        float brightness = (0.55F + 0.35F * animation.stage1()) * animation.fade();
        int r = (int) (55 * brightness);
        int g = (int) (210 * brightness);
        int b = (int) (255 * brightness);
        int a = (int) (255 * Math.min(1.0F, 0.65F + brightness * 0.35F));
        return new GlowColor(r, g, b, a);
    }

    private static GlowColor whiteColor(AnimationState animation) {
        float brightness = (0.78F + 0.22F
                * (float) Math.sin(animation.stage2() * Math.PI)) * animation.fade();
        int r = (int) (175 * brightness);
        int g = (int) (245 * brightness);
        int b = (int) (255 * brightness);
        int a = (int) (255 * animation.stage2() * animation.fade());
        return new GlowColor(r, g, b, a);
    }

    private static GlowColor stableBlueColor(AnimationState animation) {
        // 晶体管数字从第一次出现起就保持固定亮度，只让数值随蓄力变化。
        return new GlowColor(55, 210, 255, animation.fade() > 0.0F ? 255 : 0);
    }

    private static GlowColor stableWhiteColor(AnimationState animation) {
        return new GlowColor(205, 250, 255, animation.fade() > 0.0F ? 255 : 0);
    }

    private record GlowColor(int r, int g, int b, int a) {}

    private static final int[] DIGITS = {0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F};

    private static void renderNumber(PoseStack.Pose pose, VertexConsumer consumer, float x,
                                     int value, boolean mirrored, GlowColor color) {
        int[] digits = {value / 100, value / 10 % 10, value % 10};
        for (int index = 0; index < 3; index++) {
            float z = -0.5535F + index * 0.0178F;
            int mask = DIGITS[digits[index]];
            segment(pose, consumer, x, mask, 0, 0.4280F, digitZ(z + 0.0018F, mirrored), 0.4303F, digitZ(z + 0.0132F, mirrored), color);
            segment(pose, consumer, x, mask, 1, 0.4170F, digitZ(z + 0.0115F, mirrored), 0.4285F, digitZ(z + 0.0138F, mirrored), color);
            segment(pose, consumer, x, mask, 2, 0.4055F, digitZ(z + 0.0115F, mirrored), 0.4165F, digitZ(z + 0.0138F, mirrored), color);
            segment(pose, consumer, x, mask, 3, 0.4035F, digitZ(z + 0.0018F, mirrored), 0.4058F, digitZ(z + 0.0132F, mirrored), color);
            segment(pose, consumer, x, mask, 4, 0.4053F, digitZ(z + 0.0012F, mirrored), 0.4167F, digitZ(z + 0.0035F, mirrored), color);
            segment(pose, consumer, x, mask, 5, 0.4170F, digitZ(z + 0.0012F, mirrored), 0.4285F, digitZ(z + 0.0035F, mirrored), color);
            segment(pose, consumer, x, mask, 6, 0.4158F, digitZ(z + 0.0018F, mirrored), 0.4181F, digitZ(z + 0.0132F, mirrored), color);
        }
    }

    private static float digitZ(float z, boolean mirrored) {
        // 左手模型是右手模型的镜像，显示面围绕三位数字中心同步镜像。
        return mirrored ? -1.0564F - z : z;
    }

    private static void segment(PoseStack.Pose pose, VertexConsumer consumer, float x, int mask,
                                int bit, float y1, float z1, float y2, float z2, GlowColor color) {
        if ((mask & (1 << bit)) != 0) {
            quadX(pose, consumer, x, y1, y2, z1, z2,
                    color.r(), color.g(), color.b(), color.a());
        }
    }

    private static void quadSide(PoseStack.Pose pose, VertexConsumer consumer,
                                 float x1, float y1, float x2, float y2,
                                 float z1, float z2, GlowColor color) {
        vertex(consumer, pose, x1, y1, z1, color.r(), color.g(), color.b(), color.a());
        vertex(consumer, pose, x1, y1, z2, color.r(), color.g(), color.b(), color.a());
        vertex(consumer, pose, x2, y2, z2, color.r(), color.g(), color.b(), color.a());
        vertex(consumer, pose, x2, y2, z1, color.r(), color.g(), color.b(), color.a());
        vertex(consumer, pose, x2, y2, z1, color.r(), color.g(), color.b(), color.a());
        vertex(consumer, pose, x2, y2, z2, color.r(), color.g(), color.b(), color.a());
        vertex(consumer, pose, x1, y1, z2, color.r(), color.g(), color.b(), color.a());
        vertex(consumer, pose, x1, y1, z1, color.r(), color.g(), color.b(), color.a());
    }

    private static float saturate(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float remap(float value, float start, float end) {
        return saturate((value - start) / (end - start));
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    private static float smoothstep(float value) {
        value = saturate(value);
        return value * value * (3.0F - 2.0F * value);
    }

    private static void quadY(PoseStack.Pose pose, VertexConsumer c, float x1, float x2, float z1, float z2,
                              float y, int r, int g, int b, int a) {
        vertex(c, pose, x1, y, z1, r, g, b, a); vertex(c, pose, x1, y, z2, r, g, b, a);
        vertex(c, pose, x2, y, z2, r, g, b, a); vertex(c, pose, x2, y, z1, r, g, b, a);
        vertex(c, pose, x2, y, z1, r, g, b, a); vertex(c, pose, x2, y, z2, r, g, b, a);
        vertex(c, pose, x1, y, z2, r, g, b, a); vertex(c, pose, x1, y, z1, r, g, b, a);
    }

    private static void quadX(PoseStack.Pose pose, VertexConsumer c, float x, float y1, float y2, float z1, float z2,
                              int r, int g, int b, int a) {
        vertex(c, pose, x, y1, z1, r, g, b, a); vertex(c, pose, x, y2, z1, r, g, b, a);
        vertex(c, pose, x, y2, z2, r, g, b, a); vertex(c, pose, x, y1, z2, r, g, b, a);
        vertex(c, pose, x, y1, z2, r, g, b, a); vertex(c, pose, x, y2, z2, r, g, b, a);
        vertex(c, pose, x, y2, z1, r, g, b, a); vertex(c, pose, x, y1, z1, r, g, b, a);
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

    private static void rotateAroundPartAxis(PoseStack poseStack, float axisY, float angle) {
        poseStack.translate(0.0F, axisY, 0.0F);
        poseStack.mulPose(Axis.ZP.rotation(angle));
        poseStack.translate(0.0F, -axisY, 0.0F);
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

}