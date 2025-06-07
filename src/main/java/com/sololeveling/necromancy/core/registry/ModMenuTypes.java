package com.sololeveling.necromancy.core.registry;

import com.sololeveling.necromancy.SoloLevelingMod;
import com.sololeveling.necromancy.common.menu.ShadowArmyMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, SoloLevelingMod.MOD_ID);

    public static final RegistryObject<MenuType<ShadowArmyMenu>> SHADOW_ARMY_MENU =
            registerMenuType(ShadowArmyMenu::new, "shadow_army_menu");


    // --- FIX: The type is now IContainerFactory, not a nested class ---
    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}