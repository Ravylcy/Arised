package com.sololeveling.necromancy.common.util;

import com.sololeveling.necromancy.common.capability.IShadowMinion;
import com.sololeveling.necromancy.common.capability.player.IPlayerNecromancer;
import com.sololeveling.necromancy.common.data.ActiveShadowsManager;
import com.sololeveling.necromancy.common.data.ShadowArmyManager;
import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.core.ModConfigs;
import com.sololeveling.necromancy.core.registry.ModParticles;
import com.sololeveling.necromancy.core.registry.ModSounds;
import com.sololeveling.necromancy.network.PacketHandler;
import com.sololeveling.necromancy.network.S2CUpdateActiveShadows;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sololeveling.necromancy.common.event.ModEvents.RECENTLY_DECEASED;

public class NecromancerUtil {

    public static int arise(ServerPlayer player) {
        IPlayerNecromancer playerCap = IPlayerNecromancer.get(player).orElse(null);
        if (playerCap == null) return 0;

        final int ariseCost = ModConfigs.SERVER.ARISE_COST.get();
        if (playerCap.getMana() < ariseCost) {
            player.sendSystemMessage(Component.literal("Not enough mana to arise a shadow.").withStyle(ChatFormatting.RED));
            return 0;
        }

        final double searchRadius = ModConfigs.SERVER.ARISE_SEARCH_RADIUS.get();
        Vec3 playerPos = player.position();
        LivingEntity closestCorpse = RECENTLY_DECEASED.values().stream()
                .map(e -> e.entity)
                .filter(e -> e.level() == player.level() && !e.isRemoved() && e.position().distanceTo(playerPos) < searchRadius)
                .min(Comparator.comparingDouble(e -> e.position().distanceToSqr(playerPos)))
                .orElse(null);

        if (closestCorpse == null) {
            player.sendSystemMessage(Component.literal("§5No suitable corpses nearby to arise."));
            return 0;
        }

        playerCap.consumeMana(ariseCost);
        playerCap.sync(player);
        RECENTLY_DECEASED.remove(closestCorpse.getUUID());
        closestCorpse.discard();

        CompoundTag nbt = new CompoundTag();
        closestCorpse.saveWithoutId(nbt);
        ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
        ShadowInfo newShadow = new ShadowInfo(closestCorpse.getType(), nbt);
        manager.addShadow(player.getUUID(), newShadow);

        player.level().playSound(null, player.blockPosition(), ModSounds.ARISE_SOUND.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        if (player.serverLevel() != null) {
            player.serverLevel().sendParticles(ModParticles.SHADOW_PARTICLE.get(), closestCorpse.getX(), closestCorpse.getY(), closestCorpse.getZ(), 50, 0.5, 1.0, 0.5, 0.1);
        }

        player.sendSystemMessage(Component.literal("§5Arise. A new shadow, " + newShadow.getCustomName() + ", has joined your army."));
        return 1;
    }

    public static int summonShadow(ServerPlayer player, String shadowIdOrName) {
        IPlayerNecromancer playerCap = IPlayerNecromancer.get(player).orElse(null);
        if (playerCap == null) return 0;

        ShadowArmyManager manager = ShadowArmyManager.get(player.serverLevel());
        ShadowInfo info = manager.findShadowById(player.getUUID(), shadowIdOrName);

        if (info == null) {
            player.sendSystemMessage(Component.literal("Shadow with ID/Name '" + shadowIdOrName + "' not found.").withStyle(ChatFormatting.RED));
            return 0;
        }

        final int summonCost = ModConfigs.SERVER.SUMMON_COST_BASE.get() + (info.getLevel() * ModConfigs.SERVER.SUMMON_COST_PER_LEVEL.get());
        if(playerCap.getMana() < summonCost) {
            player.sendSystemMessage(Component.literal("Not enough mana to summon " + info.getCustomName()).withStyle(ChatFormatting.RED));
            return 0;
        }

        playerCap.consumeMana(summonCost);
        playerCap.sync(player);

        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(info.getOriginalEntityType());
        if (entityType == null) {
            player.sendSystemMessage(Component.literal("Could not find entity type: " + info.getOriginalEntityType()));
            return 0;
        }

        Entity createdEntity = entityType.create(player.level());
        if (!(createdEntity instanceof LivingEntity shadow)) {
            player.sendSystemMessage(Component.literal("Cannot summon non-living entity as a shadow."));
            return 0;
        }

        shadow.load(info.getOriginalNbt());
        shadow.setUUID(info.getShadowId());
        shadow.setPos(player.getX(), player.getY(), player.getZ());
        applyStatScaling(shadow, info);

        if (shadow instanceof Mob mob) {
            mob.finalizeSpawn(player.serverLevel(), player.level().getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.COMMAND, null, null);
        }

        IShadowMinion.get(shadow).ifPresent(cap -> {
            cap.setShadow(true);
            cap.setOwnerUUID(player.getUUID());
        });

        if (shadow instanceof PathfinderMob pathfinderMob) {
            ShadowAI.setup(pathfinderMob, player, playerCap.getAIStance());
        }

        shadow.setHealth(shadow.getMaxHealth());
        player.level().addFreshEntity(shadow);

        addShadowToTeam(player, shadow);

        ActiveShadowsManager.get(player.serverLevel()).add(shadow.getUUID());
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new S2CUpdateActiveShadows(ActiveShadowsManager.get(player.serverLevel()).getActiveShadows()));

        player.level().playSound(null, player.blockPosition(), ModSounds.SUMMON_SOUND.get(), SoundSource.PLAYERS, 0.8f, 1.2f);
        if (player.serverLevel() != null) {
            player.serverLevel().sendParticles(ModParticles.SHADOW_PARTICLE.get(), shadow.getX(), shadow.getY(), shadow.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
        }

        player.sendSystemMessage(Component.literal("Summoned " + info.getCustomName()).withStyle(ChatFormatting.DARK_PURPLE));
        return 1;
    }

    public static int recallSingleShadow(ServerPlayer player, UUID shadowUUID) {
        if (player == null) return 0;

        ActiveShadowsManager activeShadows = ActiveShadowsManager.get(player.serverLevel());
        if (!activeShadows.getActiveShadows().contains(shadowUUID)) {
            player.sendSystemMessage(Component.literal("That shadow is not currently summoned.").withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        Entity entity = player.serverLevel().getEntity(shadowUUID);
        if (entity instanceof LivingEntity livingEntity) {
            IShadowMinion.get(livingEntity).ifPresent(cap -> {
                if (cap.isShadow() && player.getUUID().equals(cap.getOwnerUUID())) {
                    livingEntity.discard();
                    player.sendSystemMessage(Component.literal("Recalled " + livingEntity.getName().getString() + "."));
                }
            });
            return 1;
        }
        return 0;
    }

    public static int recallAllShadows(ServerPlayer player) {
        if (player == null) return 0;

        ActiveShadowsManager activeShadows = ActiveShadowsManager.get(player.serverLevel());
        AtomicInteger count = new AtomicInteger(0);

        new HashSet<>(activeShadows.getActiveShadows()).forEach(uuid -> {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                IShadowMinion.get(livingEntity).ifPresent(cap -> {
                    if (cap.isShadow() && player.getUUID().equals(cap.getOwnerUUID())) {
                        livingEntity.discard();
                        count.getAndIncrement();
                    }
                });
            } else {
                activeShadows.remove(uuid);
            }
        });

        if (count.get() > 0) {
            player.sendSystemMessage(Component.literal("Recalled " + count.get() + " shadows."));
        } else {
            player.sendSystemMessage(Component.literal("No active shadows to recall."));
        }

        return count.get();
    }

    private static void applyStatScaling(LivingEntity shadow, ShadowInfo info) {
        if (shadow.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
            float healthBonus = (info.getLevel() - 1) * ModConfigs.SERVER.LEVEL_HEALTH_BONUS.get().floatValue();
            float gradeHealthMod = info.getGrade().getHealthModifier();
            double originalMaxHealth = shadow.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            double newMaxHealth = originalMaxHealth * (1 + healthBonus) * gradeHealthMod;
            shadow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
            shadow.setHealth((float)newMaxHealth);
        }

        if (shadow.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            float damageBonus = (info.getLevel() - 1) * ModConfigs.SERVER.LEVEL_DAMAGE_BONUS.get().floatValue();
            float gradeDamageMod = info.getGrade().getDamageModifier();
            double originalAttackDamage = shadow.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
            double newAttackDamage = originalAttackDamage * (1 + damageBonus) * gradeDamageMod;
            shadow.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(newAttackDamage);
        }
    }

    private static void addShadowToTeam(ServerPlayer player, LivingEntity shadow) {
        Scoreboard scoreboard = player.serverLevel().getScoreboard();
        String teamName = "sl_army_" + player.getUUID().toString();

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setDisplayName(Component.literal("Shadow Army"));
            team.setColor(ChatFormatting.DARK_PURPLE);
            team.setAllowFriendlyFire(false);
            team.setSeeFriendlyInvisibles(true);
        }

        scoreboard.addPlayerToTeam(shadow.getStringUUID(), team);
        if (scoreboard.getPlayersTeam(player.getGameProfile().getName()) == null || !scoreboard.getPlayersTeam(player.getGameProfile().getName()).equals(team)) {
            scoreboard.addPlayerToTeam(player.getGameProfile().getName(), team);
        }
    }
}