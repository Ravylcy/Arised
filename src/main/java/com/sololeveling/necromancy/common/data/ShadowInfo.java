package com.sololeveling.necromancy.common.data;

import com.sololeveling.necromancy.core.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class ShadowInfo {
    private final UUID shadowId;
    private final ResourceLocation originalEntityType;
    private final CompoundTag originalNbt;
    private String customName;
    private int level;
    private int xp;
    private int nextLevelXp;
    private ShadowGrade grade;

    public ShadowInfo(EntityType<?> type, CompoundTag nbt) {
        this(UUID.randomUUID(), type, nbt);
    }

    public ShadowInfo(UUID shadowId, EntityType<?> type, CompoundTag nbt) {
        this.shadowId = shadowId;
        this.originalEntityType = ForgeRegistries.ENTITY_TYPES.getKey(type);
        this.originalNbt = nbt;
        this.customName = type.getDescription().getString();
        this.level = 1;
        this.xp = 0;
        this.nextLevelXp = calculateNextLevelXp(1);
        this.grade = ShadowGrade.NORMAL;
    }

    private ShadowInfo(UUID shadowId, EntityType<?> type, CompoundTag nbt, String customName, int level, int xp, int nextLevelXp, ShadowGrade grade) {
        this.shadowId = shadowId;
        this.originalEntityType = ForgeRegistries.ENTITY_TYPES.getKey(type);
        this.originalNbt = nbt;
        this.customName = customName;
        this.level = level;
        this.xp = xp;
        this.nextLevelXp = nextLevelXp;
        this.grade = grade;
    }

    public boolean addExperience(int amount) {
        this.xp += amount;
        if (this.xp >= this.nextLevelXp) {
            levelUp();
            return true;
        }
        return false;
    }

    private void levelUp() {
        while (this.xp >= this.nextLevelXp) {
            this.xp -= this.nextLevelXp;
            this.level++;
            this.nextLevelXp = calculateNextLevelXp(this.level);

            if (this.level % 10 == 0 && this.grade.ordinal() < ShadowGrade.values().length - 1) {
                if (Math.random() < ModConfigs.SERVER.GRADE_UP_CHANCE.get()) {
                    this.grade = ShadowGrade.values()[this.grade.ordinal() + 1];
                }
            }
        }
    }

    private static int calculateNextLevelXp(int level) {
        return ModConfigs.SERVER.XP_FORMULA_BASE.get() + (int) (Math.pow(level, ModConfigs.SERVER.XP_FORMULA_POWER.get()) * 20);
    }

    public UUID getShadowId() { return shadowId; }
    public ResourceLocation getOriginalEntityType() { return originalEntityType; }
    public CompoundTag getOriginalNbt() { return originalNbt.copy(); }
    public String getCustomName() { return customName; }
    public String getShortenedName() { return customName.length() > 20 ? customName.substring(0, 17) + "..." : customName; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getNextLevelXp() { return nextLevelXp; }
    public ShadowGrade getGrade() { return grade; }

    public void setCustomName(String name) { this.customName = name; }
    public void setGrade(ShadowGrade grade) { this.grade = grade; }


    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ShadowId", shadowId);
        tag.putString("EntityType", originalEntityType.toString());
        tag.put("OriginalNBT", originalNbt);
        tag.putString("CustomName", customName);
        tag.putInt("Level", level);
        tag.putInt("XP", xp);
        tag.putInt("NextLevelXP", nextLevelXp);
        tag.putString("Grade", grade.name());
        return tag;
    }

    public static ShadowInfo deserializeNBT(CompoundTag tag) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(tag.getString("EntityType")));
        if (type == null) return null;

        UUID id = tag.getUUID("ShadowId");
        CompoundTag nbt = tag.getCompound("OriginalNBT");
        String name = tag.getString("CustomName");
        int level = tag.getInt("Level");
        int xp = tag.getInt("XP");
        int nextXp = tag.getInt("NextLevelXP");
        ShadowGrade grade = ShadowGrade.valueOf(tag.getString("Grade"));

        return new ShadowInfo(id, type, nbt, name, level, xp, nextXp, grade);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(this.serializeNBT());
    }

    public static ShadowInfo fromBytes(FriendlyByteBuf buf) {
        CompoundTag nbt = buf.readNbt();
        if (nbt == null) {
            return null;
        }
        return deserializeNBT(nbt);
    }
}