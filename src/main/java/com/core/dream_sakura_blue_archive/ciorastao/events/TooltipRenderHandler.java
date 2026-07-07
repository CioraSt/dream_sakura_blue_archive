package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, value = Dist.CLIENT)
public class TooltipRenderHandler {
    private static final int SCREEN_PADDING = 4;
    private static final int PORTRAIT_MARGIN = 8;

    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof DecorationItem decorationItem) {
            String itemId = decorationItem.getEffectiveItemId(stack);
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config = DreamSakuraTooltipAPI.getConfig(itemId);
            if (config != null) {
                DreamSakuraTooltipAPI.renderHaloTooltipBackground(event, createAdaptivePortraitConfig(event, config));
            }
        }
    }

    @SubscribeEvent
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof DecorationItem decorationItem) {
            String itemId = decorationItem.getEffectiveItemId(stack);
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config = DreamSakuraTooltipAPI.getConfig(itemId);
            if (config != null) {
                DreamSakuraTooltipAPI.setHaloTooltipColor(event, config);
            }
        }
    }

    private static DreamSakuraTooltipAPI.DreamSakuraTextureConfig createAdaptivePortraitConfig(
            RenderTooltipEvent.Pre event,
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config
    ) {
        if (!hasPortraitTexture(config)) {
            return config;
        }

        int[] tooltipSize = calculateTooltipSize(event.getComponents(), event.getFont());
        int tooltipWidth = tooltipSize[0];
        int tooltipHeight = tooltipSize[1];
        if (tooltipWidth <= 0 || tooltipHeight <= 0) {
            return config;
        }

        int[] tooltipPosition = calculateTooltipPosition(
                event.getX(),
                event.getY(),
                tooltipWidth,
                tooltipHeight,
                event.getScreenWidth(),
                event.getScreenHeight()
        );
        int tooltipX = tooltipPosition[0];
        int tooltipY = tooltipPosition[1];
        int screenWidth = event.getScreenWidth();
        int screenHeight = event.getScreenHeight();

        int textureIndex = getMainTextureIndex(config);
        int portraitWidth = getScaledTextureWidth(config, textureIndex, screenWidth);
        int portraitHeight = getScaledTextureHeight(config, textureIndex, screenHeight);
        int baseX = clamp(
                tooltipX + (tooltipWidth - portraitWidth) / 2,
                SCREEN_PADDING,
                screenWidth - portraitWidth - SCREEN_PADDING
        );
        int baseY = clamp(
                tooltipY + (tooltipHeight - portraitHeight) / 2,
                SCREEN_PADDING,
                screenHeight - portraitHeight - SCREEN_PADDING
        );

        int rightX = clamp(
                tooltipX + tooltipWidth + PORTRAIT_MARGIN,
                SCREEN_PADDING,
                screenWidth - portraitWidth - SCREEN_PADDING
        );
        int leftX = clamp(
                tooltipX - portraitWidth - PORTRAIT_MARGIN,
                SCREEN_PADDING,
                screenWidth - portraitWidth - SCREEN_PADDING
        );
        int rightSpace = screenWidth - SCREEN_PADDING - (tooltipX + tooltipWidth + PORTRAIT_MARGIN);
        int leftSpace = tooltipX - PORTRAIT_MARGIN - SCREEN_PADDING;
        boolean rightFits = rightSpace >= portraitWidth;
        boolean leftFits = leftSpace >= portraitWidth;

        int targetX;
        if (rightFits && !leftFits) {
            targetX = rightX;
        } else if (leftFits && !rightFits) {
            targetX = leftX;
        } else if (rightFits) {
            targetX = rightSpace >= leftSpace ? rightX : leftX;
        } else {
            targetX = getOverlapArea(rightX, baseY, portraitWidth, portraitHeight, tooltipX, tooltipY, tooltipWidth, tooltipHeight)
                    <= getOverlapArea(leftX, baseY, portraitWidth, portraitHeight, tooltipX, tooltipY, tooltipWidth, tooltipHeight)
                    ? rightX
                    : leftX;
        }
        int targetY = clamp(
                baseY + Math.round(config.mainTextureOffsetY),
                SCREEN_PADDING,
                screenHeight - portraitHeight - SCREEN_PADDING
        );

        return copyWithMainOffset(config, targetX - baseX, targetY - baseY);
    }

    private static boolean hasPortraitTexture(DreamSakuraTooltipAPI.DreamSakuraTextureConfig config) {
        return config.foxBladeTexturePaths != null
                && config.foxBladeTexturePaths.length > 0
                && config.foxBladeTexturePaths[0] != null
                && !config.foxBladeTexturePaths[0].isEmpty();
    }

    private static int[] calculateTooltipSize(List<ClientTooltipComponent> components, Font font) {
        int width = 0;
        int height = components.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent component : components) {
            width = Math.max(width, component.getWidth(font));
            height += component.getHeight();
        }
        return new int[]{width, height};
    }

    private static int[] calculateTooltipPosition(int mouseX, int mouseY, int width, int height, int screenWidth, int screenHeight) {
        int x = mouseX + 12;
        if (x + width > screenWidth) {
            x -= 28 + width;
        }
        if (x < SCREEN_PADDING) {
            x = SCREEN_PADDING;
        }

        int y = mouseY - 12;
        if (y < SCREEN_PADDING) {
            y = SCREEN_PADDING;
        }
        if (y + height + SCREEN_PADDING > screenHeight) {
            y = screenHeight - height - SCREEN_PADDING;
        }
        return new int[]{x, y};
    }

    private static int getMainTextureIndex(DreamSakuraTooltipAPI.DreamSakuraTextureConfig config) {
        if (config.swapWithMainTexture && config.textureSwapInterval > 0 && config.foxBladeTexturePaths != null && config.foxBladeTexturePaths.length > 0) {
            return (int) ((System.currentTimeMillis() / config.textureSwapInterval) % config.foxBladeTexturePaths.length);
        }
        return 0;
    }

    private static int getScaledTextureWidth(DreamSakuraTooltipAPI.DreamSakuraTextureConfig config, int textureIndex, int screenWidth) {
        int textureWidth = getTextureDimension(config.foxBladeTextureWidths, textureIndex);
        int textureHeight = getTextureDimension(config.foxBladeTextureHeights, textureIndex);
        float scale = Math.min(1.0F, Math.min((float) config.maxWidth / textureWidth, (float) config.maxHeight / textureHeight));
        int width = Math.max(1, Math.round(textureWidth * scale));
        return Math.min(width, Math.max(1, screenWidth - SCREEN_PADDING * 2));
    }

    private static int getScaledTextureHeight(DreamSakuraTooltipAPI.DreamSakuraTextureConfig config, int textureIndex, int screenHeight) {
        int textureWidth = getTextureDimension(config.foxBladeTextureWidths, textureIndex);
        int textureHeight = getTextureDimension(config.foxBladeTextureHeights, textureIndex);
        float scale = Math.min(1.0F, Math.min((float) config.maxWidth / textureWidth, (float) config.maxHeight / textureHeight));
        int height = Math.max(1, Math.round(textureHeight * scale));
        return Math.min(height, Math.max(1, screenHeight - SCREEN_PADDING * 2));
    }

    private static int getTextureDimension(int[] dimensions, int textureIndex) {
        if (dimensions != null && dimensions.length > textureIndex && dimensions[textureIndex] > 0) {
            return dimensions[textureIndex];
        }
        if (dimensions != null && dimensions.length > 0 && dimensions[0] > 0) {
            return dimensions[0];
        }
        return 64;
    }

    private static int getOverlapArea(int portraitX, int portraitY, int portraitWidth, int portraitHeight,
                                      int tooltipX, int tooltipY, int tooltipWidth, int tooltipHeight) {
        int overlapLeft = Math.max(portraitX, tooltipX);
        int overlapTop = Math.max(portraitY, tooltipY);
        int overlapRight = Math.min(portraitX + portraitWidth, tooltipX + tooltipWidth);
        int overlapBottom = Math.min(portraitY + portraitHeight, tooltipY + tooltipHeight);
        int overlapWidth = Math.max(0, overlapRight - overlapLeft);
        int overlapHeight = Math.max(0, overlapBottom - overlapTop);
        return overlapWidth * overlapHeight;
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static DreamSakuraTooltipAPI.DreamSakuraTextureConfig copyWithMainOffset(
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config,
            float mainTextureOffsetX,
            float mainTextureOffsetY
    ) {
        return new DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                config.maxWidth,
                config.maxHeight,
                config.floatAmplitude,
                config.floatPeriod,
                config.backgroundStart,
                config.backgroundEnd,
                config.borderStart,
                config.borderEnd,
                mainTextureOffsetX,
                mainTextureOffsetY,
                config.enableFoxBladeEffect,
                config.foxBladeTexturePaths,
                config.foxBladeTextureWidths,
                config.foxBladeTextureHeights,
                config.foxBladeMaxWidth,
                config.foxBladeMaxHeight,
                config.foxBladeCenterOffsetX,
                config.foxBladeCenterOffsetY,
                config.foxBladeOrbitRadius,
                config.foxBladeAlpha,
                config.foxBladeRotationSpeed,
                config.foxBladeLayerCount,
                config.textureSwapInterval,
                config.swapWithMainTexture,
                config.foxBladeUseIndependentTextures
        );
    }
}
