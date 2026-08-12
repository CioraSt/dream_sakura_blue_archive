package com.core.dream_sakura_blue_archive.ciorastao.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import com.core.dream_sakura_blue_archive.ciorastao.network.S2CHaloSkillVisualPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/** 在技能期间把现有光之剑模型挂到施法者右手，并向物品渲染器提供蓄力进度。 */
@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HaloSkillVisuals {
    private static final Map<Integer, Visual> VISUALS = new HashMap<>();

    public static void start(int entityId, int visual, int durationTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        long start = minecraft.level.getGameTime();
        VISUALS.put(entityId, new Visual(visual, start, start + durationTicks));
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) VISUALS.clear();
        else VISUALS.values().removeIf(visual -> minecraft.level.getGameTime() > visual.endTick);
    }

    @SubscribeEvent
    public static void renderPlayer(RenderPlayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        Visual visual = activeVisual(event.getEntity().getId(), minecraft.level.getGameTime());
        long now = minecraft.level.getGameTime();
        if (visual == null) return;
        if (visual.kind != S2CHaloSkillVisualPacket.ALICE_SWORD_CHARGE) return;
        if (event.getEntity() == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) return;

        float partial = minecraft.getFrameTime();
        float progress = Math.max(0, Math.min(1,
                (now + partial - visual.startTick) / Math.max(1f, visual.endTick - visual.startTick)));
        ItemStack sword = new ItemStack(RegistryItem.TENDOUARIS_SWORD_OF_LIGHT.get());
        sword.getOrCreateTag().putFloat("DBAForcedCharge", progress);

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        PlayerRenderer renderer = event.getRenderer();
        renderer.getModel().rightArm.translateAndRotate(pose);
        pose.translate(-0.0625, 0.72, -0.08);
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(1.15f, 1.15f, 1.15f);
        minecraft.getItemRenderer().renderStatic(event.getEntity(), sword,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, pose,
                event.getMultiBufferSource(), event.getEntity().level(), event.getPackedLight(),
                OverlayTexture.NO_OVERLAY, event.getEntity().getId());
        pose.popPose();
    }

    @SubscribeEvent
    public static void renderFirstPerson(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        Visual visual = activeVisual(minecraft.player.getId(), minecraft.level.getGameTime());
        if (visual == null || visual.kind != S2CHaloSkillVisualPacket.ALICE_SWORD_CHARGE) return;

        float progress = progress(visual, minecraft.level.getGameTime(), event.getPartialTick());
        ItemStack sword = new ItemStack(RegistryItem.TENDOUARIS_SWORD_OF_LIGHT.get());
        sword.getOrCreateTag().putFloat("DBAForcedCharge", progress);
        HumanoidArm arm = minecraft.player.getMainArm();
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;

        event.setCanceled(true);
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(direction * .56f, -.52f + event.getEquipProgress() * -.6f, -.72f);
        float swing = event.getSwingProgress();
        float squaredWave = Mth.sin(swing * swing * Mth.PI);
        float rootWave = Mth.sin(Mth.sqrt(swing) * Mth.PI);
        pose.mulPose(Axis.YP.rotationDegrees(direction * (45f - squaredWave * 20f)));
        pose.mulPose(Axis.ZP.rotationDegrees(direction * rootWave * -20f));
        pose.mulPose(Axis.XP.rotationDegrees(rootWave * -80f));
        pose.mulPose(Axis.YP.rotationDegrees(direction * -45f));
        minecraft.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
                minecraft.player, sword,
                arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                arm == HumanoidArm.LEFT, pose, event.getMultiBufferSource(), event.getPackedLight());
        pose.popPose();
    }

    private static Visual activeVisual(int entityId, long now) {
        Visual visual = VISUALS.get(entityId);
        if (visual != null && now > visual.endTick) {
            VISUALS.remove(entityId);
            return null;
        }
        return visual;
    }

    private static float progress(Visual visual, long now, float partialTick) {
        return Math.max(0, Math.min(1,
                (now + partialTick - visual.startTick) / Math.max(1f, visual.endTick - visual.startTick)));
    }

    private record Visual(int kind, long startTick, long endTick) {}

    private HaloSkillVisuals() {}
}
