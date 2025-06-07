package com.sololeveling.necromancy.network;

import com.sololeveling.necromancy.common.capability.player.IPlayerNecromancer;
import com.sololeveling.necromancy.common.data.ShadowArmyManager;
import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.common.util.NecromancerUtil;
import com.sololeveling.necromancy.common.menu.ShadowArmyMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.function.Supplier;

public class C2SKeybindActionPacket {

    private final Action action;

    public C2SKeybindActionPacket(Action action) {
        this.action = action;
    }

    public static void encode(C2SKeybindActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
    }

    public static C2SKeybindActionPacket decode(FriendlyByteBuf buf) {
        return new C2SKeybindActionPacket(buf.readEnum(Action.class));
    }

    public static void handle(C2SKeybindActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            switch (msg.action) {
                case ARISE:
                    NecromancerUtil.arise(player);
                    break;
                case OPEN_GUI:
                    MenuProvider menuProvider = new SimpleMenuProvider(
                            (id, inv, p) -> new ShadowArmyMenu(id, inv, p), Component.translatable("gui.sololeveling.shadow_army")
                    );
                    NetworkHooks.openScreen(player, menuProvider, buf -> {
                        List<ShadowInfo> army = ShadowArmyManager.get(player.serverLevel()).getArmy(player.getUUID());
                        buf.writeCollection(army, (b, info) -> info.toBytes(b));
                    });
                    break;
                case CYCLE_STANCE:
                    IPlayerNecromancer.get(player).ifPresent(cap -> {
                        player.sendSystemMessage(Component.literal("Shadow stance: " + cap.cycleAIStance().name()), true);
                        cap.sync(player);
                    });
                    break;
                // --- NEW: Handle the Recall All action ---
                case RECALL_ALL:
                    NecromancerUtil.recallAllShadows(player);
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum Action {
        ARISE,
        OPEN_GUI,
        CYCLE_STANCE,
        RECALL_ALL // --- NEW ---
    }
}