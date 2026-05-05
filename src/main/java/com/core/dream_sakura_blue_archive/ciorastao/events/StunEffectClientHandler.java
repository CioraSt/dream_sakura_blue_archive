package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class StunEffectClientHandler {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        try {
            // 检查当前玩家是否具有眩晕效果
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.hasEffect(RegistryEffect.STUN_EFFECT.get())) {
                // 阻止鼠标滚动
                event.setCanceled(true);
            }
        } catch (Exception e) {
            // 避免因异常导致游戏崩溃
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        try {
            // 检查当前玩家是否具有眩晕效果
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.hasEffect(RegistryEffect.STUN_EFFECT.get())) {
                // 阻止所有鼠标按键（左键、右键、中键）
                int button = event.getButton();
                // 鼠标左键(GLFW.GLFW_MOUSE_BUTTON_LEFT), 右键(GLFW.GLFW_MOUSE_BUTTON_RIGHT), 中键(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT ||
                        button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ||
                        button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    event.setCanceled(true);
                }
            }
        } catch (Exception e) {
            // 避免因异常导致游戏崩溃
        }
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        try {
            // 检查当前玩家是否具有眩晕效果
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.hasEffect(RegistryEffect.STUN_EFFECT.get())) {
                // 直接将输入设置为静止状态，这会阻止所有移动
                Input movementInput = event.getInput();
                movementInput.up = false;
                movementInput.down = false;
                movementInput.left = false;
                movementInput.right = false;
                movementInput.forwardImpulse = 0.0F;
                movementInput.leftImpulse = 0.0F;
                movementInput.jumping = false;
                movementInput.shiftKeyDown = false;
            }
        } catch (Exception e) {
            // 避免因异常导致游戏崩溃
        }
    }

    // 使用 TickEvent 来处理键盘输入，只允许ESC键
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.player.hasEffect(RegistryEffect.STUN_EFFECT.get()) && mc.screen == null) {
                    // 获取当前窗口
                    long window = mc.getWindow().getWindow();

                    // 检查ESC键是否被按下
                    boolean escPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;

                    // 如果ESC键被按下，允许玩家打开菜单
                    if (escPressed) {
                        // 允许ESC键正常工作，不做任何处理
                        return;
                    }

                    // 重置玩家的移动输入
                    if (mc.player.input != null) {
                        mc.player.input.up = false;
                        mc.player.input.down = false;
                        mc.player.input.left = false;
                        mc.player.input.right = false;
                        mc.player.input.forwardImpulse = 0.0F;
                        mc.player.input.leftImpulse = 0.0F;
                        mc.player.input.jumping = false;
                        mc.player.input.shiftKeyDown = false;
                    }

                    // 重置Minecraft按键状态，阻止所有按键输入
                    mc.options.keyUp.setDown(false);
                    mc.options.keyDown.setDown(false);
                    mc.options.keyLeft.setDown(false);
                    mc.options.keyRight.setDown(false);
                    mc.options.keyJump.setDown(false);
                    mc.options.keyShift.setDown(false);
                    mc.options.keySprint.setDown(false);
                    mc.options.keyAttack.setDown(false);
                    mc.options.keyUse.setDown(false);
                    mc.options.keyPickItem.setDown(false);
                    mc.options.keyDrop.setDown(false);
                    mc.options.keyChat.setDown(false);
                    mc.options.keyPlayerList.setDown(false);
                    mc.options.keyCommand.setDown(false);
                    mc.options.keySocialInteractions.setDown(false);
                    mc.options.keyScreenshot.setDown(false);
                    mc.options.keyTogglePerspective.setDown(false);
                    mc.options.keySmoothCamera.setDown(false);
                    mc.options.keyFullscreen.setDown(false);
                    mc.options.keySpectatorOutlines.setDown(false);
                    mc.options.keySwapOffhand.setDown(false);
                }
            } catch (Exception e) {
                // 避免因异常导致游戏崩溃
            }
        }
    }
}