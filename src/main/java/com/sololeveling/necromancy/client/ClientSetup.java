package com.sololeveling.necromancy.client;

import com.sololeveling.necromancy.SoloLevelingMod;
import com.sololeveling.necromancy.client.event.ClientForgeEvents;
import com.sololeveling.necromancy.client.gui.ShadowArmyScreen;
import com.sololeveling.necromancy.client.util.ModKeybinds;
import com.sololeveling.necromancy.common.event.client.ClientEvents;
import com.sololeveling.necromancy.core.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register GUI Screens
            MenuScreens.register(ModMenuTypes.SHADOW_ARMY_MENU.get(), ShadowArmyScreen::new);
        });

        // Register Client-side event handlers for key presses
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(ModKeybinds.ARISE_KEY);
        event.register(ModKeybinds.OPEN_ARMY_GUI_KEY);
        event.register(ModKeybinds.CYCLE_STANCE_KEY);
    }
}