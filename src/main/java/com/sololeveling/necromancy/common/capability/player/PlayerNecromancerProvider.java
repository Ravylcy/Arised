package com.sololeveling.necromancy.common.capability.player;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerNecromancerProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {

    public static Capability<IPlayerNecromancer> PLAYER_NECROMANCER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerNecromancer necromancer = null;
    private final LazyOptional<IPlayerNecromancer> optional = LazyOptional.of(this::getOrCreate);

    private IPlayerNecromancer getOrCreate() {
        if (this.necromancer == null) {
            this.necromancer = new PlayerNecromancer();
        }
        return this.necromancer;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_NECROMANCER_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return getOrCreate().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreate().deserializeNBT(nbt);
    }
}