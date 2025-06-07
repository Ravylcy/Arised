package com.sololeveling.necromancy.common.capability;

import com.sololeveling.necromancy.SoloLevelingMod;
import com.sololeveling.necromancy.common.capability.player.PlayerNecromancerProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CapabilityHandler {
    public static final Capability<IShadowMinion> SHADOW_MINION_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(final FMLCommonSetupEvent event) {
        // No registration needed here anymore
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity && !(event.getObject() instanceof Player)) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(SoloLevelingMod.MOD_ID, "shadow_minion"), new ShadowMinionProvider());
        }

        if (event.getObject() instanceof Player) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(SoloLevelingMod.MOD_ID, "player_necromancer"), new PlayerNecromancerProvider());
        }
    }
}