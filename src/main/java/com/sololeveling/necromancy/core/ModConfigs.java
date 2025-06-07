package com.sololeveling.necromancy.core;

import com.sololeveling.necromancy.SoloLevelingMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = SoloLevelingMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfigs {

    public static class Client {
        public final ForgeConfigSpec.BooleanValue ENABLE_HUD;
        public final ForgeConfigSpec.IntValue HUD_X_OFFSET;
        public final ForgeConfigSpec.IntValue HUD_Y_OFFSET;

        public final ForgeConfigSpec.DoubleValue SHADOW_COLOR_R;
        public final ForgeConfigSpec.DoubleValue SHADOW_COLOR_G;
        public final ForgeConfigSpec.DoubleValue SHADOW_COLOR_B;
        public final ForgeConfigSpec.DoubleValue SHADOW_ALPHA;


        Client(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side settings for the Solo Leveling Necromancy mod.").push("client");

            builder.push("hud");
            ENABLE_HUD = builder
                    .comment("Enable the Mana and Stance HUD on screen.")
                    .define("enableHud", true);
            HUD_X_OFFSET = builder
                    .comment("Horizontal offset for the HUD from the left side of the screen.")
                    .defineInRange("hudXOffset", 5, 0, Integer.MAX_VALUE);
            HUD_Y_OFFSET = builder
                    .comment("Vertical offset for the HUD from the top of the screen.")
                    .defineInRange("hudYOffset", 5, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("visuals");
            SHADOW_COLOR_R = builder
                    .comment("The RED component of the shadow's color (0.0 to 1.0).")
                    .defineInRange("shadowColorR", 0.05, 0.0, 1.0);
            SHADOW_COLOR_G = builder
                    .comment("The GREEN component of the shadow's color (0.0 to 1.0).")
                    .defineInRange("shadowColorG", 0.0, 0.0, 1.0);
            SHADOW_COLOR_B = builder
                    .comment("The BLUE component of the shadow's color (0.0 to 1.0).")
                    .defineInRange("shadowColorB", 0.1, 0.0, 1.0);
            SHADOW_ALPHA = builder
                    .comment("The ALPHA (transparency) of the shadow's color (0.0 to 1.0).")
                    .defineInRange("shadowAlpha", 0.9, 0.0, 1.0);
            builder.pop();

            builder.pop();
        }
    }

    public static class Server {
        public final ForgeConfigSpec.IntValue MANA_REGEN_RATE;
        public final ForgeConfigSpec.IntValue ARISE_COST;
        public final ForgeConfigSpec.IntValue SUMMON_COST_BASE;
        public final ForgeConfigSpec.IntValue SUMMON_COST_PER_LEVEL;

        public final ForgeConfigSpec.DoubleValue ARISE_SEARCH_RADIUS;
        public final ForgeConfigSpec.IntValue CORPSE_DECAY_TIME;

        public final ForgeConfigSpec.IntValue XP_FORMULA_BASE;
        public final ForgeConfigSpec.DoubleValue XP_FORMULA_POWER;
        public final ForgeConfigSpec.DoubleValue GRADE_UP_CHANCE;
        public final ForgeConfigSpec.DoubleValue LEVEL_HEALTH_BONUS;
        public final ForgeConfigSpec.DoubleValue LEVEL_DAMAGE_BONUS;


        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server-side settings that affect gameplay.").push("server");

            builder.push("mana");
            MANA_REGEN_RATE = builder
                    .comment("Mana points regenerated per second.")
                    .defineInRange("manaRegenRate", 1, 0, 100);
            ARISE_COST = builder
                    .comment("The amount of mana required to use the 'Arise' ability.")
                    .defineInRange("ariseCost", 50, 0, 1000);
            SUMMON_COST_BASE = builder
                    .comment("The base mana cost to summon a shadow.")
                    .defineInRange("summonCostBase", 10, 0, 1000);
            SUMMON_COST_PER_LEVEL = builder
                    .comment("Additional mana cost per level of the shadow being summoned.")
                    .defineInRange("summonCostPerLevel", 2, 0, 100);
            builder.pop();

            builder.push("necromancy");
            ARISE_SEARCH_RADIUS = builder
                    .comment("The radius (in blocks) the 'Arise' ability will search for corpses.")
                    .defineInRange("ariseSearchRadius", 15.0, 1.0, 64.0);
            CORPSE_DECAY_TIME = builder
                    .comment("How long (in ticks) a corpse remains available to be raised before disappearing. 20 ticks = 1 second.")
                    .defineInRange("corpseDecayTime", 1200, 20, 72000);
            builder.pop();

            builder.push("shadows");
            XP_FORMULA_BASE = builder
                    .comment("Base XP required for the next level.")
                    .defineInRange("xpFormulaBase", 100, 10, 1000);
            XP_FORMULA_POWER = builder
                    .comment("The exponent in the XP formula (level^power). Higher values make leveling much slower.")
                    .defineInRange("xpFormulaPower", 1.5, 1.0, 3.0);
            GRADE_UP_CHANCE = builder
                    .comment("The chance (0.0 to 1.0) for a shadow to increase its grade every 10 levels.")
                    .defineInRange("gradeUpChance", 0.25, 0.0, 1.0);
            LEVEL_HEALTH_BONUS = builder
                    .comment("Percentage health bonus per level (e.g., 0.1 = +10% hp per level).")
                    .defineInRange("levelHealthBonus", 0.1, 0.0, 1.0);
            LEVEL_DAMAGE_BONUS = builder
                    .comment("Percentage damage bonus per level (e.g., 0.05 = +5% dmg per level).")
                    .defineInRange("levelDamageBonus", 0.05, 0.0, 1.0);
            builder.pop();

            builder.pop();
        }
    }

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final Server SERVER;

    static {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();

        final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPEC = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            bakeClientConfig();
        } else if (configEvent.getConfig().getSpec() == SERVER_SPEC) {
            bakeServerConfig();
        }
    }

    public static void bakeClientConfig() {
        // You can add cached values here if needed for performance
    }

    public static void bakeServerConfig() {
        // You can add cached values here if needed for performance
    }
}