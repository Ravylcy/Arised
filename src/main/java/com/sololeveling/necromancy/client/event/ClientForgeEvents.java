package com.sololeveling.necromancy.client.event;

import com.sololeveling.necromancy.SoloLevelingMod;
import com.sololeveling.necromancy.common.capability.player.IPlayerNecromancer;
import com.sololeveling.necromancy.core.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!ModConfigs.CLIENT.ENABLE_HUD.get() || event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        IPlayerNecromancer.get(player).ifPresent(cap -> {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int x = ModConfigs.CLIENT.HUD_X_OFFSET.get();
            int y = ModConfigs.CLIENT.HUD_Y_OFFSET.get();

            // Draw Mana
            String manaText = String.format("Mana: %d / %d", cap.getMana(), cap.getMaxMana());
            guiGraphics.drawString(mc.font, manaText, x, y, 0x5555FF, true);

            // Draw Stance
            String stanceText = "Stance: " + cap.getAIStance().name();
            ChatFormatting stanceColor = switch (cap.getAIStance()) {
                case PASSIVE -> ChatFormatting.AQUA;
                case DEFENSIVE -> ChatFormatting.GREEN;
                case AGGRESSIVE -> ChatFormatting.RED;
            };
            guiGraphics.drawString(mc.font, stanceText, x, y + 10, stanceColor.getColor(), true);
        });
    }
}