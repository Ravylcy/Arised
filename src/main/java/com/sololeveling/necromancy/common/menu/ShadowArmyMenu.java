package com.sololeveling.necromancy.common.menu;

import com.sololeveling.necromancy.common.data.ShadowArmyManager;
import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.core.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShadowArmyMenu extends AbstractContainerMenu {

    public final List<ShadowInfo> army;
    private final Player player;

    // Server-side constructor
    public ShadowArmyMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        super(ModMenuTypes.SHADOW_ARMY_MENU.get(), pContainerId);
        this.player = pPlayer;
        // The server needs the real list for any potential logic it might have
        this.army = ShadowArmyManager.get(pPlayer.getServer().overworld()).getArmy(pPlayer.getUUID());
    }

    // --- THE FIX PART 3: This constructor is called on the client. It reads the list from the buffer. ---
    public ShadowArmyMenu(int pContainerId, Inventory pPlayerInventory, FriendlyByteBuf pBuffer) {
        super(ModMenuTypes.SHADOW_ARMY_MENU.get(), pContainerId);
        this.player = pPlayerInventory.player;
        this.army = pBuffer.readList(ShadowInfo::fromBytes); // Read the list from the packet
    }

    public List<ShadowInfo> getArmy() {
        return army;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}