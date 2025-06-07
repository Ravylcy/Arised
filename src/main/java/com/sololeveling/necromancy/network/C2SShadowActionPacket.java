package com.sololeveling.necromancy.network;

import com.sololeveling.necromancy.common.data.ShadowArmyManager;
import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.common.util.NecromancerUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SShadowActionPacket {
    private final Action action;
    private final UUID shadowUUID;
    private final String data;

    public C2SShadowActionPacket(Action action, UUID shadowUUID, String data) {
        this.action = action;
        this.shadowUUID = shadowUUID;
        this.data = data;
    }

    public static void encode(C2SShadowActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeUUID(msg.shadowUUID);
        buf.writeUtf(msg.data);
    }

    public static C2SShadowActionPacket decode(FriendlyByteBuf buf) {
        return new C2SShadowActionPacket(buf.readEnum(Action.class), buf.readUUID(), buf.readUtf());
    }

    public static void handle(C2SShadowActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
            ShadowInfo info = manager.findShadowByEntityUUID(player.getUUID(), msg.shadowUUID);

            if (info == null) {
                player.sendSystemMessage(Component.literal("Could not find the specified shadow. It may have been released."));
                return;
            }

            switch (msg.action) {
                case SUMMON:
                    NecromancerUtil.summonShadow(player, info.getShadowId().toString());
                    break;
                // --- NEW: Handle the single recall action ---
                case RECALL:
                    NecromancerUtil.recallSingleShadow(player, info.getShadowId());
                    break;
                case RENAME:
                    info.setCustomName(msg.data);
                    manager.setDirty();
                    player.sendSystemMessage(Component.literal("Shadow renamed to " + msg.data));
                    break;
                case RELEASE:
                    manager.removeShadow(player.getUUID(), info.getShadowId());
                    player.sendSystemMessage(Component.literal(info.getCustomName() + " has been released."));
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum Action {
        SUMMON,
        RECALL, // --- NEW ---
        RENAME,
        RELEASE
    }
}