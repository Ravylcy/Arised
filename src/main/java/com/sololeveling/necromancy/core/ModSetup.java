package com.sololeveling.necromancy.core;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup {
    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Common setup logic, like worldgen, goes here in the future.
        });
    }

    // --- IMPROVEMENT: This method was empty and its listener registration was unnecessary. It has been removed. ---
}