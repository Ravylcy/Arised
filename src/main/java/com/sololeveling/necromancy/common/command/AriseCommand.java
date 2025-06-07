package com.sololeveling.necromancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sololeveling.necromancy.common.data.ShadowArmyManager;
import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.common.event.ModEvents;
import com.sololeveling.necromancy.core.ModConfigs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.UUID;

public class AriseCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("arise")
                .requires(source -> source.hasPermission(2)) // Admin only
                .executes(AriseCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Command must be run by a player."));
            return 0;
        }

        final double searchRadius = 20.0; // Command radius can be hardcoded as it's a debug/admin tool

        Vec3 playerPos = player.position();
        LivingEntity closestCorpse = ModEvents.RECENTLY_DECEASED.values().stream()
                .map(entry -> entry.entity)
                .filter(e -> e.level() == player.level() && e.position().distanceTo(playerPos) < searchRadius)
                .min(Comparator.comparingDouble(e -> e.position().distanceToSqr(playerPos)))
                .orElse(null);

        if (closestCorpse == null) {
            player.sendSystemMessage(Component.literal("§5No suitable corpses nearby to arise."));
            return 0;
        }

        // Remove from the list so it can't be raised again
        ModEvents.RECENTLY_DECEASED.remove(closestCorpse.getUUID());
        closestCorpse.discard();

        // Save the entity's data
        CompoundTag nbt = new CompoundTag();
        closestCorpse.saveWithoutId(nbt);

        ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
        ShadowInfo newShadow = new ShadowInfo(UUID.randomUUID(), closestCorpse.getType(), nbt);

        manager.addShadow(player.getUUID(), newShadow);

        player.sendSystemMessage(Component.literal("§5Arise. A new shadow, " + newShadow.getCustomName() + ", has joined your army."));
        return 1;
    }
}