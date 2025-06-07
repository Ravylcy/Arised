package com.sololeveling.necromancy.common.capability;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

public class ShadowMinion implements IShadowMinion {
    private boolean isShadow = false;
    private UUID ownerUUID;

    @Override
    public boolean isShadow() {
        return this.isShadow;
    }

    @Override
    public void setShadow(boolean isShadow) {
        this.isShadow = isShadow;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    @Override
    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("IsShadow", this.isShadow);
        if (this.ownerUUID != null) {
            nbt.putUUID("OwnerUUID", this.ownerUUID);
        }
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.isShadow = nbt.getBoolean("IsShadow");
        if (nbt.hasUUID("OwnerUUID")) {
            this.ownerUUID = nbt.getUUID("OwnerUUID");
        } else {
            this.ownerUUID = null;
        }
    }
}