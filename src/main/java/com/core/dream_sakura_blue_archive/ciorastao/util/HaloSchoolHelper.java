package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.api.CreativeCategoryRegistry;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.halo.HaloCatalog;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/** Dream Sakura sidebar categories backed by the HALO catalog and academy art. */
public final class HaloSchoolHelper {
    private static final String MODID = dream_sakura_blue_archive.MODID;

    private HaloSchoolHelper() {
    }

    public static void registerCategories() {
        CreativeCategoryRegistry.setTargetTab(dream_sakura_blue_archive.DREAM_SAKURA_BA_TAB.get());
        CreativeCategoryRegistry.showAllWhenNoneSelected = true;

        Set<String> schools = new LinkedHashSet<>();
        HaloCatalog.entries().forEach(entry -> schools.add(entry.school()));
        for (String school : schools) {
            ItemStack icon = "collaboration".equals(school)
                    ? new ItemStack(RegistryItem.halo("hatsune_miku_halo").get())
                    : new ItemStack(RegistryItem.categoryIconFor(school).get());
            Component name = Component.translatable("category." + MODID + ".school." + school);
            fromStack(name, icon, stack -> school.equals(schoolOf(stack)));
        }

        fromStack(Component.translatable("category." + MODID + ".other"),
                new ItemStack(RegistryItem.SAKULUNA_HALO.get()), stack -> {
                    String id = getItemPath(stack);
                    return "kluonuoya_halo".equals(id) || "sakuluna_halo".equals(id) || "karena_halo".equals(id);
                });
        fromStack(Component.translatable("category." + MODID + ".experience"),
                new ItemStack(RegistryItem.SUPERIOR_EXP.get()), stack -> {
                    String path = getItemPath(stack);
                    return path != null && path.contains("_exp");
                });
        fromStack(Component.translatable("category." + MODID + ".equipment"),
                new ItemStack(RegistryItem.HOSHINO_TACTICAL_SHIELD.get()), stack -> {
                    String path = getItemPath(stack);
                    return path != null && !path.contains("_halo") && !path.contains("_exp");
                });
    }

    private static String schoolOf(ItemStack stack) {
        String id = getItemPath(stack);
        return id == null ? null : HaloCatalog.schoolOf(HaloVariantHelper.baseItemId(id));
    }

    private static void fromStack(Component name, ItemStack icon, Predicate<ItemStack> predicate) {
        CreativeCategoryRegistry.create(name, icon, predicate);
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
