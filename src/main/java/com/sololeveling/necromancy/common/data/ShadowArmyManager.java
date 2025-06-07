package com.sololeveling.necromancy.common.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShadowArmyManager extends SavedData {
    private static final String DATA_NAME = "sololeveling_shadow_army";
    private final Map<UUID, List<ShadowInfo>> playerArmies = new HashMap<>();

    public List<ShadowInfo> getArmy(UUID playerUuid) {
        return playerArmies.computeIfAbsent(playerUuid, k -> new ArrayList<>());
    }

    public void addShadow(UUID playerUuid, ShadowInfo shadowInfo) {
        getArmy(playerUuid).add(shadowInfo);
        setDirty();
    }

    public void removeShadow(UUID playerUuid, UUID shadowId) {
        getArmy(playerUuid).removeIf(info -> info.getShadowId().equals(shadowId));
        setDirty();
    }

    public ShadowInfo findShadowById(UUID playerUuid, String nameOrId) {
        try {
            UUID id = UUID.fromString(nameOrId);
            return findShadowByEntityUUID(playerUuid, id);
        } catch (IllegalArgumentException e) {
            return getArmy(playerUuid).stream().filter(s -> s.getCustomName().equalsIgnoreCase(nameOrId)).findFirst().orElse(null);
        }
    }

    public ShadowInfo findShadowByEntityUUID(UUID playerUuid, UUID entityUuid) {
        return getArmy(playerUuid).stream()
                .filter(info -> info.getShadowId().equals(entityUuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag) {
        CompoundTag armiesTag = new CompoundTag();
        playerArmies.forEach((uuid, army) -> {
            ListTag armyList = new ListTag();
            army.forEach(shadow -> armyList.add(shadow.serializeNBT()));
            armiesTag.put(uuid.toString(), armyList);
        });
        compoundTag.put("PlayerArmies", armiesTag);
        return compoundTag;
    }

    public static ShadowArmyManager load(CompoundTag nbt) {
        ShadowArmyManager manager = new ShadowArmyManager();
        CompoundTag armiesTag = nbt.getCompound("PlayerArmies");
        for (String key : armiesTag.getAllKeys()) {
            UUID playerUuid = UUID.fromString(key);
            ListTag armyList = armiesTag.getList(key, 10);
            List<ShadowInfo> shadows = armyList.stream()
                    .map(tag -> ShadowInfo.deserializeNBT((CompoundTag) tag))
                    .collect(Collectors.toList());
            manager.playerArmies.put(playerUuid, shadows);
        }
        return manager;
    }

    public static ShadowArmyManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        // --- FIX: This is the modern, correct way to load SavedData. ---
        return storage.computeIfAbsent(ShadowArmyManager::load, ShadowArmyManager::new, DATA_NAME);
    }
}