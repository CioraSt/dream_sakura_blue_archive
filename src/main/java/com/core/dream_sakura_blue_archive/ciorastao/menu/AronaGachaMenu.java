package com.core.dream_sakura_blue_archive.ciorastao.menu;

import com.core.dream_sakura_blue_archive.ciorastao.entity.AronaEntity;
import com.core.dream_sakura_blue_archive.ciorastao.gacha.Gacha;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import com.core.dream_sakura_blue_archive.ciorastao.network.NetworkHandler;
import com.core.dream_sakura_blue_archive.ciorastao.network.S2CGachaResultPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AronaGachaMenu extends AbstractContainerMenu {
    private final AronaEntity arona;
    private final SimpleContainer payment = new SimpleContainer(1);
    private final DataSlot pity;

    private static final Item CURRENCY = RegistryItem.PYROXENE.get();

    public AronaGachaMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, findArona(inventory.player, buffer.readInt()));
    }

    public AronaGachaMenu(int id, Inventory inventory, AronaEntity arona) {
        super(RegistryMenu.ARONA_GACHA.get(), id);
        this.arona = arona;
        this.addSlot(new Slot(payment, 0, 80, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(CURRENCY); }
        });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 116 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 174));
        this.pity = DataSlot.standalone();
        if (inventory.player instanceof ServerPlayer serverPlayer) pity.set(Gacha.getPity(serverPlayer));
        addDataSlot(pity);
    }

    private static AronaEntity findArona(Player player, int entityId) {
        Entity entity = player.level().getEntity(entityId);
        return entity instanceof AronaEntity arona ? arona : null;
    }

    public int getPity() { return pity.get(); }

    public void draw(ServerPlayer player, int count) {
        if (count != 1 && count != 10 || arona == null || arona.isAngry() || !stillValid(player)) return;
        ItemStack currency = payment.getItem(0);
        if (!currency.is(CURRENCY) || currency.getCount() < count) return;

        currency.shrink(count);
        payment.setChanged();
        this.clearContainer(player, payment);

        List<Gacha.Reward> rewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Gacha.Reward reward = Gacha.draw(player);
            rewards.add(reward);
            Gacha.grant(player, reward.stack());
        }
        pity.set(Gacha.getPity(player));
        NetworkHandler.sendToPlayer(new S2CGachaResultPacket(rewards), player);
    }

    @Override
    public boolean stillValid(Player player) {
        return arona != null && arona.isAlive() && !arona.isAngry() && player.distanceToSqr(arona) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        if (index == 0) {
            if (!moveItemStackTo(slot.getItem(), 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (slot.getItem().is(CURRENCY)) {
            if (!moveItemStackTo(slot.getItem(), 0, 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (slot.getItem().isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) this.clearContainer(player, payment);
    }
}
