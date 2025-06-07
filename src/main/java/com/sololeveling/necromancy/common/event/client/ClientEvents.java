package com.sololeveling.necromancy.common.event.client;

import com.sololeveling.necromancy.client.util.ModKeybinds;
import com.sololeveling.necromancy.network.C2SKeybindActionPacket;
import com.sololeveling.necromancy.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (ModKeybinds.ARISE_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeybindActionPacket(C2SKeybindActionPacket.Action.ARISE));
        }

        if (ModKeybinds.OPEN_ARMY_GUI_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeybindActionPacket(C2SKeybindActionPacket.Action.OPEN_GUI));
        }

        if (ModKeybinds.CYCLE_STANCE_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SKeybindActionPacket(C2SKeybindActionPacket.Action.CYCLE_STANCE));
        }
    }
}