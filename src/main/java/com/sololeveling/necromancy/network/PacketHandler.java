package com.sololeveling.necromancy.network;

import com.sololeveling.necromancy.SoloLevelingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(SoloLevelingMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, S2CUpdateActiveShadows.class, S2CUpdateActiveShadows::encode, S2CUpdateActiveShadows::decode, S2CUpdateActiveShadows::handle);
        INSTANCE.registerMessage(id++, S2CPlayerNecromancerSync.class, S2CPlayerNecromancerSync::encode, S2CPlayerNecromancerSync::decode, S2CPlayerNecromancerSync::handle);
        INSTANCE.registerMessage(id++, C2SKeybindActionPacket.class, C2SKeybindActionPacket::encode, C2SKeybindActionPacket::decode, C2SKeybindActionPacket::handle);
        INSTANCE.registerMessage(id++, C2SShadowActionPacket.class, C2SShadowActionPacket::encode, C2SShadowActionPacket::decode, C2SShadowActionPacket::handle);
    }
}