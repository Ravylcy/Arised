package com.sololeveling.necromancy.common.capability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IShadowMinion {

    boolean isShadow();

    void setShadow(boolean isShadow);

    @Nullable
    UUID getOwnerUUID();

    void setOwnerUUID(@Nullable UUID ownerUUID);

    // Helper to easily get the capability from an entity
    static LazyOptional<IShadowMinion> get(LivingEntity entity) {
        return entity.getCapability(CapabilityHandler.SHADOW_MINION_CAPABILITY);
    }
}