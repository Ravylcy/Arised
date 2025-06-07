package com.sololeveling.necromancy.common.event;

import com.sololeveling.necromancy.SoloLevelingMod;
import com.sololeveling.necromancy.common.capability.IShadowMinion;
import com.sololeveling.necromancy.common.capability.player.IPlayerNecromancer;
import com.sololeveling.necromancy.common.command.AriseCommand;
import com.sololeveling.necromancy.common.command.ShadowCommand;
import com.sololeveling.necromancy.common.data.ActiveShadowsManager;
import com.sololeveling.necromancy.common.data.ShadowArmyManager;
import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.core.ModConfigs;
import com.sololeveling.necromancy.core.registry.ModParticles;
import com.sololeveling.necromancy.core.registry.ModSounds;
import com.sololeveling.necromancy.network.PacketHandler;
import com.sololeveling.necromancy.network.S2CUpdateActiveShadows;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MOD_ID)
public class ModEvents {

    public static class CorpseEntry {
        public final LivingEntity entity;
        public final long deathTime;
        public CorpseEntry(LivingEntity entity, long time) { this.entity = entity; this.deathTime = time; }
    }

    public static final Map<UUID, CorpseEntry> RECENTLY_DECEASED = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        AriseCommand.register(event.getDispatcher());
        ShadowCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IPlayerNecromancer.get(player).ifPresent(cap -> cap.sync(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IPlayerNecromancer.get(player).ifPresent(cap -> cap.sync(player));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Mana Regeneration for all players online
        if (event.getServer().getTickCount() % 20 == 0) {
            event.getServer().getPlayerList().getPlayers().forEach(player -> {
                IPlayerNecromancer.get(player).ifPresent(cap -> {
                    cap.addMana(ModConfigs.SERVER.MANA_REGEN_RATE.get());
                    cap.sync(player);
                });
            });
        }

        // Corpse cleanup
        if (event.getServer().getTickCount() % 100 == 0) { // Every 5 seconds
            long currentTime = event.getServer().overworld().getGameTime();
            long corpseTimeout = ModConfigs.SERVER.CORPSE_DECAY_TIME.get();
            RECENTLY_DECEASED.entrySet().removeIf(entry ->
                    currentTime - entry.getValue().deathTime > corpseTimeout || entry.getValue().entity.isRemoved()
            );
        }
    }


    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity deadEntity = event.getEntity();
        if (deadEntity == null || deadEntity.level().isClientSide) {
            return;
        }

        LazyOptional<IShadowMinion> cap = IShadowMinion.get(deadEntity);
        if (cap.isPresent() && cap.orElseThrow(RuntimeException::new).isShadow()) {
            handleShadowDeath(deadEntity, cap.orElseThrow(RuntimeException::new));
            return;
        }

        DamageSource source = event.getSource();
        Entity killer = source.getEntity();
        if (killer instanceof Player) {
            awardXpToArmy((Player) killer, deadEntity);
        } else if (killer instanceof LivingEntity livingKiller) {
            IShadowMinion.get(livingKiller).ifPresent(killerCap -> {
                if (killerCap.isShadow() && killerCap.getOwnerUUID() != null) {
                    Player owner = deadEntity.level().getPlayerByUUID(killerCap.getOwnerUUID());
                    if (owner != null) {
                        awardXpToArmy(owner, deadEntity);
                    }
                }
            });
        }

        if (event.getEntity() instanceof Monster) {
            long currentTime = deadEntity.level().getGameTime();
            RECENTLY_DECEASED.put(deadEntity.getUUID(), new CorpseEntry(deadEntity, currentTime));
        }
    }

    private static void handleShadowDeath(LivingEntity deadShadow, IShadowMinion shadowCap) {
        if (deadShadow.level() instanceof ServerLevel serverLevel) {
            UUID ownerUUID = shadowCap.getOwnerUUID();

            ActiveShadowsManager manager = ActiveShadowsManager.get(serverLevel);
            manager.remove(deadShadow.getUUID());

            PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new S2CUpdateActiveShadows(manager.getActiveShadows()));

            if (ownerUUID != null) {
                Player owner = serverLevel.getPlayerByUUID(ownerUUID);
                if (owner != null) {
                    owner.sendSystemMessage(Component.literal("§5Your shadow has been destroyed. It can be summoned again."));
                    owner.level().playSound(null, owner.blockPosition(), ModSounds.RECALL_SOUND.get(), SoundSource.PLAYERS, 0.6f, 1.0f);
                }
            }
        }
    }

    private static void awardXpToArmy(Player owner, LivingEntity victim) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) return;

        int xpToAward = (int) victim.getMaxHealth() / 4;
        if (xpToAward <= 0) return;

        ActiveShadowsManager activeShadowsManager = ActiveShadowsManager.get(serverLevel);
        ShadowArmyManager armyManager = ShadowArmyManager.get(serverLevel);
        final boolean[] changed = {false};

        activeShadowsManager.getActiveShadows().forEach(uuid -> {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                IShadowMinion.get(livingEntity).ifPresent(cap -> {
                    if (cap.isShadow() && owner.getUUID().equals(cap.getOwnerUUID())) {
                        ShadowInfo info = armyManager.findShadowByEntityUUID(owner.getUUID(), uuid);
                        if (info != null && info.addExperience(xpToAward)) {
                            owner.sendSystemMessage(Component.literal("§d" + info.getCustomName() + " has leveled up to level " + info.getLevel() + "!"));
                            owner.level().playSound(null, livingEntity.blockPosition(), ModSounds.LEVEL_UP_SOUND.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
                            serverLevel.sendParticles(ModParticles.SHADOW_PARTICLE.get(), livingEntity.getX(), livingEntity.getY() + livingEntity.getBbHeight() / 2, livingEntity.getZ(), 20, 0.3, 0.5, 0.3, 0.1);
                            changed[0] = true;
                        }
                    }
                });
            }
        });

        if (changed[0]) {
            armyManager.setDirty();
        }
    }
}