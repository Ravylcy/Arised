package com.sololeveling.necromancy;

import com.sololeveling.necromancy.client.ClientSetup;
import com.sololeveling.necromancy.client.event.ClientForgeEvents;
import com.sololeveling.necromancy.common.capability.CapabilityHandler;
import com.sololeveling.necromancy.common.event.ModEvents;
import com.sololeveling.necromancy.core.ModConfigs;
import com.sololeveling.necromancy.core.ModSetup;
import com.sololeveling.necromancy.core.registry.ModMenuTypes;
import com.sololeveling.necromancy.core.registry.ModParticles;
import com.sololeveling.necromancy.core.registry.ModSounds;
import com.sololeveling.necromancy.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SoloLevelingMod.MOD_ID)
public class SoloLevelingMod {
    public static final String MOD_ID = "sololeveling";
    public static final Logger LOGGER = LogManager.getLogger();

    public SoloLevelingMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        // Register Deferred Registers
        ModSounds.register(modEventBus);
        ModParticles.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        // Register setup handlers
        modEventBus.addListener(ModSetup::init);

        // Register capability system
        modEventBus.addListener(CapabilityHandler::register);
        forgeEventBus.register(new CapabilityHandler());

        // Register general events
        forgeEventBus.register(new ModEvents());

        // Register networking
        PacketHandler.register();

        // Register Configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModConfigs.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ModConfigs.SERVER_SPEC);

        // Register client-side setup
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(ClientSetup::init);
            // Register client-side FORGE bus events (for overlays, etc.)
            MinecraftForge.EVENT_BUS.register(new ClientForgeEvents());
        });
    }
}