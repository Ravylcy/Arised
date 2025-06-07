package com.sololeveling.necromancy.network;

import com.sololeveling.necromancy.common.capability.player.IPlayerNecromancer;
import com.sololeveling.necromancy.common.util.AIStance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CPlayerNecromancerSync {

    private final int mana;
    private final int maxMana;
    private final AIStance stance;

    public S2CPlayerNecromancerSync(int mana, int maxMana, AIStance stance) {
        this.mana = mana;
        this.maxMana = maxMana;
        this.stance = stance;
    }

    public static void encode(S2CPlayerNecromancerSync msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.mana);
        buf.writeInt(msg.maxMana);
        buf.writeEnum(msg.stance);
    }

    public static S2CPlayerNecromancerSync decode(FriendlyByteBuf buf) {
        return new S2CPlayerNecromancerSync(buf.readInt(), buf.readInt(), buf.readEnum(AIStance.class));
    }

    public static void handle(S2CPlayerNecromancerSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side handling
            IPlayerNecromancer.get(Minecraft.getInstance().player).ifPresent(cap -> {
                cap.setMaxMana(msg.maxMana);
                cap.setMana(msg.mana);
                cap.setAIStance(msg.stance);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}