package com.sololeveling.necromancy.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShadowMinionProvider implements ICapabilitySerializable<CompoundTag> {
    private final ShadowMinion shadowMinion = new ShadowMinion();
    private final LazyOptional<IShadowMinion> optional = LazyOptional.of(() -> shadowMinion);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityHandler.SHADOW_MINION_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return shadowMinion.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        shadowMinion.deserializeNBT(nbt);
    }
}