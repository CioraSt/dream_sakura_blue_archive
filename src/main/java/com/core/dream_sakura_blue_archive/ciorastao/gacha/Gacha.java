package com.core.dream_sakura_blue_archive.ciorastao.gacha;

import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** 阿罗娜抽卡的服务端规则、保底与卡池配置。 */
public final class Gacha {
    private static final String PITY_KEY = "dream_sakura_blue_archive.arona_gacha_pity";

    // ===== 卡池配置：在此直接增删物品即可调整对应稀有度的奖池 =====
    public static final List<Item> BLUE_POOL = List.of(
            RegistryItem.PRIMARY_EXP.get(), RegistryItem.INTERMEDIATE_EXP.get(),
            Items.GOLDEN_APPLE, Items.GOLDEN_CARROT);
    public static final List<Item> GOLD_POOL = List.of(
            RegistryItem.SENIOR_EXP.get(), Items.ENCHANTED_GOLDEN_APPLE,
            RegistryItem.HOSHINO_HALO.get(), RegistryItem.TENDOUARIS_HALO.get(),
            RegistryItem.AYANE_HALO.get(), RegistryItem.KAYOKO_HALO.get(), RegistryItem.YUME_HALO.get());
    public static final List<Item> RAINBOW_POOL = List.of(
            RegistryItem.HOSHINO_TACTICAL_SHIELD.get(), RegistryItem.MIKA_HALO.get(),
            RegistryItem.KUROKO_HALO.get(), RegistryItem.HINA_HALO.get(), RegistryItem.SHIROKO_HALO.get());

    private Gacha() {
    }

    public enum Rarity { BLUE, GOLD, RAINBOW }
    public record Reward(Rarity rarity, ItemStack stack) { }

    public static int getPity(ServerPlayer player) {
        return Math.max(0, Math.min(9, player.getPersistentData().getInt(PITY_KEY)));
    }

    public static Reward draw(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int pity = getPity(player);
        Rarity rarity = roll(player.getRandom(), pity == 9);
        data.putInt(PITY_KEY, (pity + 1) % 10);
        List<Item> pool = poolOf(rarity);
        return new Reward(rarity, new ItemStack(pool.get(player.getRandom().nextInt(pool.size()))));
    }

    private static Rarity roll(RandomSource random, boolean guarantee) {
        int value = random.nextInt(100);
        if (guarantee && value < 60) value = 60 + random.nextInt(40);
        if (value < 60) return Rarity.BLUE;
        return value < 90 ? Rarity.GOLD : Rarity.RAINBOW;
    }

    private static List<Item> poolOf(Rarity rarity) {
        return switch (rarity) {
            case BLUE -> BLUE_POOL;
            case GOLD -> GOLD_POOL;
            case RAINBOW -> RAINBOW_POOL;
        };
    }

    public static void grant(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack.copy())) player.drop(stack.copy(), false);
    }
}
