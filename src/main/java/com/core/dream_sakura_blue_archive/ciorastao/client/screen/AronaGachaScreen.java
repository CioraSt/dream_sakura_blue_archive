package com.core.dream_sakura_blue_archive.ciorastao.client.screen;

import com.core.dream_sakura_blue_archive.ciorastao.menu.AronaGachaMenu;
import com.core.dream_sakura_blue_archive.ciorastao.network.C2SGachaDrawPacket;
import com.core.dream_sakura_blue_archive.ciorastao.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AronaGachaScreen extends AbstractContainerScreen<AronaGachaMenu> {
    public AronaGachaScreen(AronaGachaMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 198;
    }

    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("抽 1 发"), button -> NetworkHandler.sendToServer(new C2SGachaDrawPacket(1)))
                .bounds(leftPos + 22, topPos + 70, 62, 20).build());
        addRenderableWidget(Button.builder(Component.literal("抽 10 连！"), button -> NetworkHandler.sendToServer(new C2SGachaDrawPacket(10)))
                .bounds(leftPos + 92, topPos + 70, 62, 20).build());
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFFFFFFFF);
        graphics.fill(leftPos + 7, topPos + 27, leftPos + 169, topPos + 58, 0xFFE8E8E8);
        drawSlot(graphics, leftPos + 79, topPos + 34);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            drawSlot(graphics, leftPos + 7 + column * 18, topPos + 115 + row * 18);
        for (int column = 0; column < 9; column++) drawSlot(graphics, leftPos + 7 + column * 18, topPos + 173);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "放入青辉石", 60, 15, 0xFFFFFF, false);
        graphics.drawString(font, "当前 10 抽保底：" + menu.getPity() + "/10", 25, 96, 0xFFF5E6A6, false);
        graphics.drawString(font, playerInventoryTitle, 8, 105, 0xFFD0D0D0, false);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF111111);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF707070);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFF2A2A2A);
    }
}
