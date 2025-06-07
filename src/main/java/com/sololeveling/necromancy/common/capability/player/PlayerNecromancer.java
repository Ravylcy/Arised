package com.sololeveling.necromancy.common.capability.player;

import com.sololeveling.necromancy.common.util.AIStance;
import com.sololeveling.necromancy.network.PacketHandler;
import com.sololeveling.necromancy.network.S2CPlayerNecromancerSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class PlayerNecromancer implements IPlayerNecromancer {

    private int mana = 100;
    private int maxMana = 100;
    private AIStance aiStance = AIStance.DEFENSIVE;

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = Math.max(0, Math.min(mana, this.maxMana));
    }

    @Override
    public void addMana(int mana) {
        setMana(this.mana + mana);
    }

    @Override
    public void consumeMana(int mana) {
        setMana(this.mana - mana);
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    @Override
    public AIStance getAIStance() {
        return aiStance;
    }

    @Override
    public void setAIStance(AIStance stance) {
        this.aiStance = stance;
    }

    @Override
    public AIStance cycleAIStance() {
        int next = (this.aiStance.ordinal() + 1) % AIStance.values().length;
        this.aiStance = AIStance.values()[next];
        return this.aiStance;
    }

    @Override
    public void sync(Player player) {
        if (!player.level().isClientSide) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new S2CPlayerNecromancerSync(this.mana, this.maxMana, this.aiStance));
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Mana", this.mana);
        nbt.putInt("MaxMana", this.maxMana);
        nbt.putString("AIStance", this.aiStance.name());
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.maxMana = nbt.contains("MaxMana") ? nbt.getInt("MaxMana") : 100;
        this.mana = nbt.getInt("Mana");
        if (nbt.contains("AIStance")) {
            this.aiStance = AIStance.valueOf(nbt.getString("AIStance"));
        }
    }
}