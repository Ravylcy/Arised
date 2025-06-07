package com.sololeveling.necromancy.client.renderer;

import net.minecraft.world.entity.LivingEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ShadowRenderHandler {
    private static final Set<UUID> ACTIVE_SHADOWS = new HashSet<>();

    public static void updateActiveShadows(List<UUID> shadowUuids) {
        ACTIVE_SHADOWS.clear();
        ACTIVE_SHADOWS.addAll(shadowUuids);
    }

    // Called from the mixin to determine if the tint should be applied
    public static boolean isEntityShadow(LivingEntity entity) {
        return ACTIVE_SHADOWS.contains(entity.getUUID());
    }
}