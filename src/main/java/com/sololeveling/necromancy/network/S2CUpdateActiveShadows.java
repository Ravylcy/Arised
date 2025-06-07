package com.sololeveling.necromancy.network;

import com.sololeveling.necromancy.client.renderer.ShadowRenderHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CUpdateActiveShadows {

    private final List<UUID> activeShadows;

    public S2CUpdateActiveShadows(Set<UUID> activeShadows) {
        this.activeShadows = new ArrayList<>(activeShadows);
    }

    public S2CUpdateActiveShadows(List<UUID> activeShadows) {
        this.activeShadows = activeShadows;
    }

    public static void encode(S2CUpdateActiveShadows msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.activeShadows, FriendlyByteBuf::writeUUID);
    }

    public static S2CUpdateActiveShadows decode(FriendlyByteBuf buf) {
        return new S2CUpdateActiveShadows(buf.readList(FriendlyByteBuf::readUUID));
    }

    public static void handle(S2CUpdateActiveShadows msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ShadowRenderHandler.updateActiveShadows(msg.activeShadows));
        ctx.get().setPacketHandled(true);
    }
}