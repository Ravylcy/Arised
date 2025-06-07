package com.sololeveling.necromancy.common.data;

import com.sololeveling.necromancy.common.capability.IShadowMinion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ActiveShadowsManager extends SavedData {
    private static final String DATA_NAME = "sololeveling_active_shadows";
    private final Set<UUID> activeShadows = new HashSet<>();

    public Set<UUID> getActiveShadows() {
        return activeShadows;
    }

    public void add(UUID uuid) {
        if (activeShadows.add(uuid)) {
            setDirty();
        }
    }

    public void remove(UUID uuid) {
        if (activeShadows.remove(uuid)) {
            setDirty();
        }
    }

    // This method is very inefficient (iterates all world entities) and is no longer
    // needed by the improved `recallAll` command. It is kept here in case a full
    // sync is ever needed for a different reason (e.g., a server crash recovery command).
    public void syncFrom(ServerLevel level) {
        activeShadows.clear();
        level.getAllEntities().forEach(entity -> {
            if (entity instanceof LivingEntity le) {
                IShadowMinion.get(le).ifPresent(cap -> {
                    if (cap.isShadow()) {
                        activeShadows.add(le.getUUID());
                    }
                });
            }
        });
        setDirty();
    }

    public static ActiveShadowsManager load(CompoundTag nbt) {
        ActiveShadowsManager manager = new ActiveShadowsManager();
        ListTag list = nbt.getList("ActiveShadows", 11); // 11 is the NBT type for IntArray, where UUIDs are stored
        for (int i = 0; i < list.size(); ++i) {
            manager.activeShadows.add(NbtUtils.loadUUID(list.get(i)));
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for (UUID uuid : activeShadows) {
            list.add(NbtUtils.createUUID(uuid));
        }
        compoundTag.put("ActiveShadows", list);
        return compoundTag;
    }

    public static ActiveShadowsManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(ActiveShadowsManager::load, ActiveShadowsManager::new, DATA_NAME);
    }
}