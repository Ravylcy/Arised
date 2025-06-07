package com.sololeveling.necromancy.common.data;

import net.minecraft.ChatFormatting;

public enum ShadowGrade {
    NORMAL(1.0f, 1.0f, "§7Normal", ChatFormatting.GRAY),
    ELITE(1.25f, 1.2f, "§aElite", ChatFormatting.GREEN),
    KNIGHT(1.6f, 1.5f, "§bKnight", ChatFormatting.AQUA),
    COMMANDER(2.0f, 2.0f, "§dCommander", ChatFormatting.LIGHT_PURPLE);

    private final float healthModifier;
    private final float damageModifier;
    private final String displayString;
    private final ChatFormatting displayFormatting;

    ShadowGrade(float healthModifier, float damageModifier, String displayString, ChatFormatting formatting) {
        this.healthModifier = healthModifier;
        this.damageModifier = damageModifier;
        this.displayString = displayString;
        this.displayFormatting = formatting;
    }

    public float getHealthModifier() {
        return healthModifier;
    }

    public float getDamageModifier() {
        return damageModifier;
    }

    public String getDisplayString() {
        return displayString;
    }

    public ChatFormatting getDisplayFormatting() {
        return displayFormatting;
    }
}