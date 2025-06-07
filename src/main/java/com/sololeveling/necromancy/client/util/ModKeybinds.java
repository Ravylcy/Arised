package com.sololeveling.necromancy.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static final String KEY_CATEGORY_SOLOLEVELING = "key.category.sololeveling";
    public static final String KEY_ARISE = "key.sololeveling.arise";
    public static final String KEY_OPEN_ARMY_GUI = "key.sololeveling.open_army_gui";
    public static final String KEY_CYCLE_STANCE = "key.sololeveling.cycle_stance";

    public static final KeyMapping ARISE_KEY = new KeyMapping(
            KEY_ARISE,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY_SOLOLEVELING
    );

    public static final KeyMapping OPEN_ARMY_GUI_KEY = new KeyMapping(
            KEY_OPEN_ARMY_GUI,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY_SOLOLEVELING
    );

    public static final KeyMapping CYCLE_STANCE_KEY = new KeyMapping(
            KEY_CYCLE_STANCE,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            KEY_CATEGORY_SOLOLEVELING
    );

    // --- THE FIX: This empty method is no longer needed. ---
    // Registration now happens in ClientSetup.
}