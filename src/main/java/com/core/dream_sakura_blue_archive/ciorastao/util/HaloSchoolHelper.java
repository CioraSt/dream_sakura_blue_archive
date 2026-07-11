package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.api.CreativeCategoryRegistry;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.function.Predicate;

public final class HaloSchoolHelper {

    // 阿比多斯高等学校
    private static final Set<String> ABYDOS = Set.of(
            "shiroko_halo", "shiroko_cycling_halo", "shiroko_swimsuit_halo",
            "hoshino_halo", "hoshino_swimsuit_halo",
            "serlka_halo", "nonomi_halo",
            "ayaneko_halo", "ayane_swimsuit_halo",
            "yume_halo"
    );

    // 格黑娜学园
    private static final Set<String> GEHENNA = Set.of(
            "hina_halo", "hina_dress_halo", "hina_swimsuit_halo",
            "kayoko_halo", "kayoko_newyear_halo"
    );

    // 千禧年科学学园
    private static final Set<String> MILLENNIUM = Set.of(
            "tendouaris_halo", "tendouaris_battle_halo", "tendouaris_maid_halo",
            "yuzu_halo", "yuzu_battle_halo", "yuzu_maid_halo",
            "momoi_halo", "momoi_maid_halo",
            "midori_halo", "midori_maid_halo",
            "kaiyi_halo"
    );

    // 三一综合学园
    private static final Set<String> TRINITY = Set.of(
            "shirasuazusa_halo", "shirasuazusa_swimsuit_halo",
            "mari_halo", "mari_idol_halo", "mari_gym_halo",
            "seia_halo", "seia_swimsuit_halo",
            "mika_halo",
            "natsu_halo", "natsu_band_halo"
    );

    // 山海经高级中学
    private static final Set<String> SHANHAIJING = Set.of(
            "shun_halo", "karena_halo"
    );

    private static final String MODID = dream_sakura_blue_archive.MODID;

    private HaloSchoolHelper() {
    }

    public static void registerCategories() {
        CreativeCategoryRegistry.setTargetTab(dream_sakura_blue_archive.DREAM_SAKURA_BA_TAB.get());
        CreativeCategoryRegistry.showAllWhenNoneSelected = true;

        // 学院分类
        fromPath("阿比多斯", RegistryItem.SHIROKO_HALO.get(), id -> ABYDOS.contains(id));
        fromPath("格黑娜", RegistryItem.HINA_HALO.get(), id -> GEHENNA.contains(id));
        fromPath("千禧年", RegistryItem.TENDOUARIS_HALO.get(), id -> MILLENNIUM.contains(id));
        fromPath("三一", RegistryItem.SHIRASUAZUSA_HALO.get(), id -> TRINITY.contains(id));
        fromPath("山海经", RegistryItem.SHUN_HALO.get(), id -> SHANHAIJING.contains(id));
        fromPath("其他光环", RegistryItem.KUROKO_HALO.get(), HaloSchoolHelper::filterOthers);

        // 其他物品分类
        fromStack("经验书", RegistryItem.SUPERIOR_EXP.get(), stack -> {
            String path = getItemPath(stack);
            return path != null && path.contains("_exp");
        });
        fromStack("装备", RegistryItem.HOSHINO_TACTICAL_SHIELD.get(), stack -> {
            String path = getItemPath(stack);
            return path != null && !path.contains("_halo") && !path.contains("_exp");
        });
    }

    private static void fromPath(String name, net.minecraft.world.item.Item icon, Predicate<String> idPredicate) {
        CreativeCategoryRegistry.create(
                Component.literal("§b" + name),
                new ItemStack(icon),
                stack -> {
                    String path = getItemPath(stack);
                    return path != null && idPredicate.test(path);
                }
        );
    }

    private static void fromStack(String name, net.minecraft.world.item.Item icon, Predicate<ItemStack> predicate) {
        CreativeCategoryRegistry.create(
                Component.literal("§b" + name),
                new ItemStack(icon),
                predicate
        );
    }

    private static boolean filterOthers(String id) {
        return id.endsWith("_halo")
                && !ABYDOS.contains(id) && !GEHENNA.contains(id) && !MILLENNIUM.contains(id)
                && !TRINITY.contains(id) && !SHANHAIJING.contains(id);
    }

    private static String getItemPath(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key != null && MODID.equals(key.getNamespace())) {
            return HaloVariantHelper.effectiveItemId(stack, key.getPath());
        }
        return null;
    }
}
