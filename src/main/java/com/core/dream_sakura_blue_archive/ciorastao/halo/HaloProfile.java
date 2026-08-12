package com.core.dream_sakura_blue_archive.ciorastao.halo;

import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

/**
 * 光环战斗配置。外观变体以有效物品 id 为键，不再继承基础物品的技能配置。
 */
public record HaloProfile(String id, HaloAttackType attackType,
                          float physicalResistance, float magicResistance,
                          float explosionResistance, float penetration) {
    private static final Map<String, HaloProfile> PROFILES = Map.ofEntries(
            entry("tendouaris_halo", HaloAttackType.MAGIC, 0, .50f, 0),
            entry("tendouaris_maid_halo", HaloAttackType.MAGIC, .50f, 0, 0),
            entry("hoshino_halo", HaloAttackType.EXPLOSION, 0, 0, .50f),
            entry("hoshino_swimsuit_halo", HaloAttackType.PHYSICAL, 0, .50f, 0),
            entry("hina_halo", HaloAttackType.PHYSICAL, 0, 0, .50f),
            entry("hina_swimsuit_halo", HaloAttackType.PHYSICAL, 0, 0, .50f),
            entry("hina_dress_halo", HaloAttackType.PHYSICAL, .25f, .25f, .25f),
            entry("sakuluna_halo", HaloAttackType.MAGIC, 0, .50f, 0)
    );

    private static Map.Entry<String, HaloProfile> entry(String id, HaloAttackType attack,
                                                         float physical, float magic, float explosion) {
        return Map.entry(id, new HaloProfile(id, attack, physical, magic, explosion, .50f));
    }

    public float resistance(HaloAttackType type) {
        return switch (type) {
            case PHYSICAL -> physicalResistance;
            case MAGIC -> magicResistance;
            case EXPLOSION -> explosionResistance;
        };
    }

    public static Optional<HaloProfile> byId(String id) {
        return Optional.ofNullable(PROFILES.get(id));
    }

    public static Optional<Equipped> equipped(Player player) {
        ItemStack stack = OtherHelper.getCurio(player, "halo", 0);
        if (!(stack.getItem() instanceof DecorationItem item)) return Optional.empty();
        String id = item.getEffectiveItemId(stack);
        HaloProfile profile = PROFILES.get(id);
        return profile == null ? Optional.empty() : Optional.of(new Equipped(profile, stack));
    }

    public record Equipped(HaloProfile profile, ItemStack stack) {}
}
