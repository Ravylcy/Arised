package com.sololeveling.necromancy.core.registry;

import com.sololeveling.necromancy.SoloLevelingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SoloLevelingMod.MOD_ID);

    public static final RegistryObject<SoundEvent> ARISE_SOUND = registerSoundEvent("arise");
    public static final RegistryObject<SoundEvent> SUMMON_SOUND = registerSoundEvent("summon");
    public static final RegistryObject<SoundEvent> RECALL_SOUND = registerSoundEvent("recall");
    public static final RegistryObject<SoundEvent> LEVEL_UP_SOUND = registerSoundEvent("level_up");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SoloLevelingMod.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}