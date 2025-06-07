package com.sololeveling.necromancy.common.capability.player;

import com.sololeveling.necromancy.common.util.AIStance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public interface IPlayerNecromancer {
    int getMana();
    void setMana(int mana);
    void addMana(int mana);
    void consumeMana(int mana);
    int getMaxMana();
    void setMaxMana(int maxMana);

    AIStance getAIStance();
    void setAIStance(AIStance stance);
    AIStance cycleAIStance();

    void sync(Player player);

    // --- FIX: Added serialization methods to the interface so they can be called. ---
    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag nbt);

    static LazyOptional<IPlayerNecromancer> get(Player player) {
        return player.getCapability(PlayerNecromancerProvider.PLAYER_NECROMANCER_CAPABILITY);
    }
}