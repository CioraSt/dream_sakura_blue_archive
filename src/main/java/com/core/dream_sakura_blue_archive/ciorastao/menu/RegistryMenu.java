package com.core.dream_sakura_blue_archive.ciorastao.menu;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RegistryMenu {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, dream_sakura_blue_archive.MODID);
    public static final RegistryObject<MenuType<AronaGachaMenu>> ARONA_GACHA = MENUS.register(
            "arona_gacha", () -> IForgeMenuType.create(AronaGachaMenu::new));

    private RegistryMenu() {
    }
}
