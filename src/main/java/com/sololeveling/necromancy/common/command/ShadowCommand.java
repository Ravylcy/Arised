package com.sololeveling.necromancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.sololeveling.necromancy.common.capability.IShadowMinion;
import com.sololeveling.necromancy.common.capability.player.IPlayerNecromancer;
import com.sololeveling.necromancy.common.data.ActiveShadowsManager;
import com.sololeveling.necromancy.common.data.ShadowArmyManager;
import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.common.util.AIStance;
import com.sololeveling.necromancy.common.util.NecromancerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ShadowCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shadow")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("summon")
                        .then(Commands.argument("shadow_id", StringArgumentType.string())
                                .executes(ctx -> summon(ctx.getSource(), StringArgumentType.getString(ctx, "shadow_id")))))
                .then(Commands.literal("recall")
                        .executes(ctx -> NecromancerUtil.recallAllShadows(ctx.getSource().getPlayer())))
                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("rename")
                        .then(Commands.argument("shadow_id", StringArgumentType.string())
                                .then(Commands.argument("new_name", StringArgumentType.string())
                                        .executes(ctx -> rename(ctx.getSource(), StringArgumentType.getString(ctx, "shadow_id"), StringArgumentType.getString(ctx, "new_name"))))))
                .then(Commands.literal("release")
                        .then(Commands.argument("shadow_id", StringArgumentType.string())
                                .executes(ctx -> release(ctx.getSource(), StringArgumentType.getString(ctx, "shadow_id")))))
                .then(Commands.literal("stats")
                        .then(Commands.argument("shadow_id", StringArgumentType.string())
                                .executes(ctx -> stats(ctx.getSource(), StringArgumentType.getString(ctx, "shadow_id")))))
                .then(Commands.literal("stance")
                        .executes(ctx -> getStance(ctx.getSource()))
                        .then(Commands.literal("set")
                                .then(Commands.literal("passive").executes(ctx -> setStance(ctx.getSource(), AIStance.PASSIVE)))
                                .then(Commands.literal("defensive").executes(ctx -> setStance(ctx.getSource(), AIStance.DEFENSIVE)))
                                .then(Commands.literal("aggressive").executes(ctx -> setStance(ctx.getSource(), AIStance.AGGRESSIVE))))));
    }

    private static int summon(CommandSourceStack source, String shadowId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        return NecromancerUtil.summonShadow(player, shadowId);
    }

    private static int list(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
        List<ShadowInfo> army = manager.getArmy(player.getUUID());

        if (army.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Your shadow army is empty."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("--- Your Shadow Army ---"), false);
        army.forEach(info -> {
            source.sendSuccess(() -> Component.literal("- " + info.getCustomName() + " (ID: " + info.getShadowId().toString().substring(0, 8) + ")"), false);
        });
        return 1;
    }

    private static int rename(CommandSourceStack source, String shadowId, String newName) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
        ShadowInfo info = manager.findShadowById(player.getUUID(), shadowId);

        if (info == null) {
            source.sendFailure(Component.literal("Shadow with ID/Name '" + shadowId + "' not found."));
            return 0;
        }

        String oldName = info.getCustomName();
        info.setCustomName(newName);
        manager.setDirty(); // IMPORTANT: Mark manager as dirty to save changes.

        source.sendSuccess(() -> Component.literal("Renamed '" + oldName + "' to '" + newName + "'."), true);
        return 1;
    }

    private static int release(CommandSourceStack source, String shadowId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
        ShadowInfo info = manager.findShadowById(player.getUUID(), shadowId);

        if (info == null) {
            source.sendFailure(Component.literal("Shadow with ID/Name '" + shadowId + "' not found."));
            return 0;
        }

        manager.removeShadow(player.getUUID(), info.getShadowId());
        source.sendSuccess(() -> Component.literal("Permanently released shadow '" + info.getCustomName() + "'."), true);
        return 1;
    }

    private static int stats(CommandSourceStack source, String shadowId) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
        ShadowInfo info = manager.findShadowById(player.getUUID(), shadowId);

        if (info == null) {
            source.sendFailure(Component.literal("Shadow with ID/Name '" + shadowId + "' not found."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("--- Stats for " + info.getCustomName() + " ---"), false);
        source.sendSuccess(() -> Component.literal("Grade: " + info.getGrade().getDisplayString()), false);
        source.sendSuccess(() -> Component.literal("Level: " + info.getLevel()), false);
        source.sendSuccess(() -> Component.literal("XP: " + info.getXp() + " / " + info.getNextLevelXp()), false);
        source.sendSuccess(() -> Component.literal("ID: " + info.getShadowId().toString().substring(0, 8)), false);

        return 1;
    }

    private static int getStance(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        IPlayerNecromancer.get(player).ifPresent(cap -> {
            source.sendSuccess(() -> Component.literal("Current shadow stance: " + cap.getAIStance().name()), false);
        });

        return 1;
    }

    private static int setStance(CommandSourceStack source, AIStance stance) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        IPlayerNecromancer.get(player).ifPresent(cap -> {
            cap.setAIStance(stance);
            cap.sync(player);
            source.sendSuccess(() -> Component.literal("Shadow stance set to: " + stance.name()), true);
        });

        return 1;
    }
}