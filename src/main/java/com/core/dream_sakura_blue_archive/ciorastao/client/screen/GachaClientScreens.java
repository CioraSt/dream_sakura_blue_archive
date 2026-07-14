package com.core.dream_sakura_blue_archive.ciorastao.client.screen;

import com.core.dream_sakura_blue_archive.ciorastao.gacha.Gacha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** 抽卡展示均为客户端表现；奖励已由服务端在抽取成功时结算。 */
public final class GachaClientScreens {
    private GachaClientScreens() {
    }

    public static void openSignature(List<Gacha.Reward> rewards) {
        Minecraft.getInstance().setScreen(new SignatureScreen(rewards));
    }

    private static final class SignatureScreen extends Screen {
        private final List<Gacha.Reward> rewards;
        private final List<int[]> points = new ArrayList<>();
        private boolean drawing;

        private SignatureScreen(List<Gacha.Reward> rewards) { super(Component.literal("请老师签字！")); this.rewards = rewards; }
        @Override protected void init() { addRenderableWidget(Button.builder(Component.literal("完成签名"), b -> finish())
                .bounds(width / 2 - 45, height - 42, 90, 20).build()); }
        @Override public boolean mouseClicked(double x, double y, int button) {
            if (button == 0 && x > 30 && x < width - 30 && y > 45 && y < height - 60) {
                drawing = !drawing;
                if (!drawing) finish();
                return true;
            }
            return super.mouseClicked(x, y, button);
        }
        @Override public void mouseMoved(double x, double y) {
            if (drawing) points.add(new int[]{(int)x, (int)y});
            super.mouseMoved(x, y);
        }
        private void finish() { Minecraft.getInstance().setScreen(new RevealScreen(rewards)); }
        @Override public void render(GuiGraphics g, int mx, int my, float tick) {
            renderBackground(g); g.drawCenteredString(font, title, width / 2, 18, 0xFFFFFFFF);
            g.drawString(font, drawing ? "正在签名，再次点击画布结束" : "点击画布开始签名", 35, 32, 0xFFF5E6A6);
            g.fill(30, 45, width - 30, height - 60, 0xFFFCFCF5);
            for (int i = 1; i < points.size(); i++) {
                int[] a = points.get(i - 1), b = points.get(i);
                g.hLine(Math.min(a[0], b[0]), Math.max(a[0], b[0]), a[1], 0xFF3B5A8D);
                g.vLine(b[0], Math.min(a[1], b[1]), Math.max(a[1], b[1]), 0xFF3B5A8D);
            }
            super.render(g, mx, my, tick);
        }
    }

    private static final class RevealScreen extends Screen {
        private final List<Gacha.Reward> rewards; private int index;
        private long revealStartedAt;
        private RevealScreen(List<Gacha.Reward> rewards) { super(Component.literal("阿罗娜的招募结果")); this.rewards = rewards; }
        @Override protected void init() {
            revealStartedAt = System.currentTimeMillis();
            addRenderableWidget(Button.builder(Component.literal("下一项"), b -> next())
                    .bounds(width / 2 - 35, height - 34, 70, 20).build());
            addRenderableWidget(Button.builder(Component.literal("跳过 →"), b -> summary())
                    .bounds(width - 80, height - 34, 64, 20).build());
        }
        private void next() {
            if (++index >= rewards.size()) summary();
            else revealStartedAt = System.currentTimeMillis();
        }
        private void summary() { Minecraft.getInstance().setScreen(new SummaryScreen(rewards)); }
        @Override public void render(GuiGraphics g, int mx, int my, float tick) {
            Gacha.Reward reward = rewards.get(index);
            double elapsed = (System.currentTimeMillis() - revealStartedAt) / 1000.0D;
            drawRecruitBackground(g, width, height, reward.rarity(), elapsed);
            g.renderItem(reward.stack(), width / 2 - 8, height / 2 - 8);
            g.drawCenteredString(font, reward.stack().getHoverName(), width / 2, 32, 0xFFFFFFFF);
            g.drawCenteredString(font, (index + 1) + "/" + rewards.size(), width / 2, 46, 0xFFFFFFFF);
            super.render(g, mx, my, tick);
        }
    }

    /** 仿照招募揭示的背景语言：白闪切入、菱形线框持续向外掠过、少量几何碎片漂浮。 */
    private static void drawRecruitBackground(GuiGraphics graphics, int screenWidth, int screenHeight,
                                               Gacha.Rarity rarity, double elapsed) {
        int[] colors = auraColors(rarity);
        int[] background = backgroundColors(rarity);
        graphics.fillGradient(0, 0, screenWidth, screenHeight, background[0], background[1]);

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        // 开场白闪只存在一瞬，随后留下中心很淡的辉光以托起物品。
        if (elapsed < 0.22D) {
            int alpha = (int) (255 * (1.0D - elapsed / 0.22D));
            graphics.fill(0, 0, screenWidth, screenHeight, withAlpha(0xFFFFFF, alpha));
        }
        // 厚重的中心承托只属于出货瞬间，稳定后完全退场。
        double introFade = Math.max(0.0D, 1.0D - elapsed / 0.82D);
        if (introFade > 0.0D) {
            drawFilledDiamond(graphics, centerX, centerY, 112, 67,
                    withAlpha(0x1AB8EA, (int) (74 * introFade)));
            drawFilledDiamond(graphics, centerX, centerY, 92, 55,
                    withAlpha(0x2FDFF3, (int) (58 * introFade)));
            drawRevealFrame(graphics, centerX, centerY, colors, elapsed, introFade);
        }
        drawExpandingDiamonds(graphics, centerX, centerY, screenWidth, screenHeight, rarity, elapsed);
        drawGeometricFragments(graphics, screenWidth, screenHeight, colors, elapsed);
    }

    private static int[] backgroundColors(Gacha.Rarity rarity) {
        return switch (rarity) {
            case BLUE -> new int[]{0xFF59C8E8, 0xFF4861C8};
            case GOLD -> new int[]{0xFFFFD98A, 0xFFC97842};
            case RAINBOW -> new int[]{0xFFE0B7EF, 0xFF527BD0};
        };
    }

    private static void drawRevealFrame(GuiGraphics graphics, int centerX, int centerY, int[] rarityColors,
                                        double elapsed, double fade) {
        // 物品周围固定为 BA 招募的青蓝菱形；稀有度只在其外缘着色。
        drawDiamondStroke(graphics, centerX, centerY, 78, 47,
                withAlpha(rarityColors[1], (int) (105 * fade)), 14);
        drawDiamondStroke(graphics, centerX, centerY, 67, 40,
                withAlpha(0x26D8F4, (int) (220 * fade)), 10);
        drawDiamondStroke(graphics, centerX, centerY, 57, 34,
                withAlpha(0xFFFFFF, (int) (185 * fade)), 3);

        // 出货的一瞬间从物品中心发出白色光线，随后很快收掉。
        if (elapsed < 0.56D) {
            double rayFade = 1.0D - elapsed / 0.56D;
            for (int ray = 0; ray < 18; ray++) {
                double angle = Math.PI * 2.0D * ray / 18.0D;
                double inner = 42.0D + elapsed * 28.0D;
                double outer = inner + 36.0D + elapsed * 100.0D;
                int x0 = centerX + (int) (Math.cos(angle) * inner);
                int y0 = centerY + (int) (Math.sin(angle) * inner * 0.60D);
                int x1 = centerX + (int) (Math.cos(angle) * outer);
                int y1 = centerY + (int) (Math.sin(angle) * outer * 0.60D);
                drawThinLine(graphics, x0, y0, x1, y1, withAlpha(0xFFFFFF, (int) (190 * rayFade)));
            }
        }
    }

    private static void drawExpandingDiamonds(GuiGraphics graphics, int centerX, int centerY,
                                               int screenWidth, int screenHeight, Gacha.Rarity rarity, double elapsed) {
        double maxRadius = Math.hypot(screenWidth, screenHeight) * 0.76D;
        // 稳定状态同时只保留一枚菱形：从中心粗深色向外扩张，并逐渐变细、变白。
        double stableTime = Math.max(0.0D, elapsed - 0.32D);
        double progress = (stableTime * 0.28D) % 1.0D;
        double radius = 72.0D + progress * maxRadius;
        int thickness = Math.max(1, (int) (11.0D * (1.0D - progress)));
        int rgb = blendRgb(diamondStartColor(rarity), 0xFFFFFF, progress);
        int alpha = Math.max(0, (int) (128 * (1.0D - progress * 0.68D)));
        drawDiamondStroke(graphics, centerX, centerY, radius, radius * 0.58D,
                withAlpha(rgb, alpha), thickness);
    }

    private static int diamondStartColor(Gacha.Rarity rarity) {
        return switch (rarity) {
            case BLUE -> 0x164ED3;
            case GOLD -> 0xAA5A0C;
            case RAINBOW -> 0x7048B8;
        };
    }

    private static int blendRgb(int first, int second, double amount) {
        double inverse = 1.0D - amount;
        int red = (int) (((first >>> 16) & 0xFF) * inverse + ((second >>> 16) & 0xFF) * amount);
        int green = (int) (((first >>> 8) & 0xFF) * inverse + ((second >>> 8) & 0xFF) * amount);
        int blue = (int) ((first & 0xFF) * inverse + (second & 0xFF) * amount);
        return red << 16 | green << 8 | blue;
    }

    private static void drawDiamondStroke(GuiGraphics graphics, int centerX, int centerY,
                                          double radiusX, double radiusY, int color, int thickness) {
        int topY = centerY - (int) radiusY;
        int rightX = centerX + (int) radiusX;
        int bottomY = centerY + (int) radiusY;
        int leftX = centerX - (int) radiusX;
        for (int offset = -(thickness / 2); offset <= thickness / 2; offset++) {
            drawThinLine(graphics, centerX + offset, topY, rightX, centerY + offset, color);
            drawThinLine(graphics, rightX, centerY + offset, centerX + offset, bottomY, color);
            drawThinLine(graphics, centerX + offset, bottomY, leftX, centerY + offset, color);
            drawThinLine(graphics, leftX, centerY + offset, centerX + offset, topY, color);
        }
    }

    private static void drawThinLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int error = dx - dy;
        while (true) {
            graphics.fill(x0, y0, x0 + 2, y0 + 2, color);
            if (x0 == x1 && y0 == y1) return;
            int twiceError = error * 2;
            if (twiceError > -dy) { error -= dy; x0 += sx; }
            if (twiceError < dx) { error += dx; y0 += sy; }
        }
    }

    private static void drawGeometricFragments(GuiGraphics graphics, int screenWidth, int screenHeight,
                                               int[] colors, double elapsed) {
        for (int i = 0; i < 16; i++) {
            double seed = i * 19.731D;
            int x = (int) (((Math.sin(seed) * 0.5D + 0.5D) * screenWidth + elapsed * (16 + i % 5)) % screenWidth);
            int y = (int) ((Math.cos(seed * 1.71D) * 0.5D + 0.5D) * screenHeight);
            int size = 4 + i % 5 * 3;
            drawSmallDiamond(graphics, x, y, size, withAlpha(i % 3 == 0 ? 0xFFFFFF : colors[i & 1], 24 + i % 4 * 7));
        }
    }

    private static void drawSmallDiamond(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        drawFilledDiamond(graphics, centerX, centerY, radius, radius, color);
    }

    private static void drawFilledDiamond(GuiGraphics graphics, int centerX, int centerY,
                                          int radiusX, int radiusY, int color) {
        for (int y = -radiusY; y <= radiusY; y++) {
            double proportion = 1.0D - Math.abs(y) / (double) radiusY;
            int halfWidth = Math.max(1, (int) (radiusX * proportion));
            graphics.fill(centerX - halfWidth, centerY + y, centerX + halfWidth + 1, centerY + y + 1, color);
        }
    }

    private static int withAlpha(int rgb, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0x00FFFFFF);
    }

    private static int[] auraColors(Gacha.Rarity rarity) {
        return switch (rarity) {
            case BLUE -> new int[]{0x9FD6FF, 0x4A90E2};
            case GOLD -> new int[]{0xFFF0A0, 0xFFB52F};
            case RAINBOW -> new int[]{0xFFB9D3, 0x9FDEFF};
        };
    }

    /** 用多层半透明椭圆模拟柔光；GUI 坐标系中纵向压缩使光团更自然。 */
    private static void drawSoftEllipse(GuiGraphics graphics, int centerX, int centerY, double radiusX, double radiusY, int rgb, int maxAlpha) {
        for (int layer = 5; layer >= 1; layer--) {
            double scale = layer / 5.0D;
            double xRadius = Math.max(1.0D, radiusX * scale);
            double yRadius = Math.max(1.0D, radiusY * scale);
            int alpha = Math.max(1, (int) (maxAlpha * (1.0D - scale * 0.72D)));
            int color = (alpha << 24) | rgb;
            for (int y = (int) -yRadius; y <= (int) yRadius; y++) {
                double width = xRadius * Math.sqrt(Math.max(0.0D, 1.0D - y * y / (yRadius * yRadius)));
                graphics.fill(centerX - (int) width, centerY + y, centerX + (int) width + 1, centerY + y + 1, color);
            }
        }
    }

    private static final class SummaryScreen extends Screen {
        private final List<Gacha.Reward> rewards;
        private SummaryScreen(List<Gacha.Reward> rewards) { super(Component.literal("招募结果合集")); this.rewards = rewards; }
        @Override protected void init() { addRenderableWidget(Button.builder(Component.literal("确认"), b -> onClose())
                .bounds(width / 2 - 40, height - 38, 80, 20).build()); }
        @Override public void render(GuiGraphics g, int mx, int my, float tick) {
            renderBackground(g); g.drawCenteredString(font, title, width / 2, 22, 0xFFFFFFFF);
            ItemStack hoveredStack = ItemStack.EMPTY;
            for (int i = 0; i < rewards.size(); i++) {
                int x = width / 2 - 82 + (i % 5) * 41, y = 62 + (i / 5) * 47;
                drawSummaryFrame(g, x, y, rewards.get(i).rarity());
                g.renderItem(rewards.get(i).stack(), x + 4, y + 4);
                if (mx >= x + 4 && mx < x + 20 && my >= y + 4 && my < y + 20) {
                    hoveredStack = rewards.get(i).stack();
                }
            }
            super.render(g, mx, my, tick);
            if (!hoveredStack.isEmpty()) g.renderTooltip(font, hoveredStack, mx, my);
        }
        private static void drawSummaryFrame(GuiGraphics graphics, int x, int y, Gacha.Rarity rarity) {
            if (rarity == Gacha.Rarity.RAINBOW) {
                graphics.fillGradient(x - 4, y - 4, x + 28, y + 28, 0xFF9FDEFF, 0xFFFFB9D3);
            } else if (rarity == Gacha.Rarity.GOLD) {
                graphics.fillGradient(x - 4, y - 4, x + 28, y + 28, 0xFFFFF0A0, 0xFFFFB52F);
            } else {
                graphics.fillGradient(x - 4, y - 4, x + 28, y + 28, 0xFF9FD6FF, 0xFF4A90E2);
            }
            graphics.fill(x - 2, y - 2, x + 26, y + 26, 0x5AFFFFFF);
        }
    }
}
