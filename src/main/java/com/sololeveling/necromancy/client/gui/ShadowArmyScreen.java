package com.sololeveling.necromancy.client.gui;

import com.sololeveling.necromancy.common.data.ShadowInfo;
import com.sololeveling.necromancy.common.menu.ShadowArmyMenu;
import com.sololeveling.necromancy.network.C2SKeybindActionPacket;
import com.sololeveling.necromancy.network.C2SShadowActionPacket;
import com.sololeveling.necromancy.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShadowArmyScreen extends AbstractContainerScreen<ShadowArmyMenu> {

    private final List<ShadowInfo> army;
    private int scrollOffset = 0;
    private ShadowInfo selectedShadow = null;
    private LivingEntity renderedEntity = null;

    private EditBox renameBox;
    private Button confirmRenameButton, cancelRenameButton;
    private final List<Button> shadowListButtons = new ArrayList<>();
    private final List<Button> detailButtons = new ArrayList<>();

    private enum View { LIST, DETAILS, RENAMING }
    private View currentView = View.LIST;

    public ShadowArmyScreen(ShadowArmyMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 256;  // Increased width for better spacing
        this.imageHeight = 240; // Increased height to prevent overlap
        this.army = pMenu.getArmy();
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;

        // Rename box positioned better
        this.renameBox = new EditBox(this.font, this.leftPos + 8, this.topPos + 160, 160, 20, Component.translatable("gui.sololeveling.new_name"));
        this.confirmRenameButton = Button.builder(Component.translatable("gui.sololeveling.confirm"), b -> handleConfirmRename())
                .bounds(this.leftPos + 8, this.topPos + 185, 78, 20).build();
        this.cancelRenameButton = Button.builder(Component.translatable("gui.sololeveling.cancel"), b -> setView(View.DETAILS))
                .bounds(this.leftPos + 90, this.topPos + 185, 78, 20).build();
        this.addRenderableWidget(renameBox);
        this.addRenderableWidget(confirmRenameButton);
        this.addRenderableWidget(cancelRenameButton);

        // Scroll buttons with better positioning
        addRenderableWidget(Button.builder(Component.literal("▲"), b -> scroll(-1))
                .bounds(leftPos + 220, topPos + 18, 20, 16).build());
        addRenderableWidget(Button.builder(Component.literal("▼"), b -> scroll(1))
                .bounds(leftPos + 220, topPos + 120, 20, 16).build());

        // Recall all button moved to bottom
        addRenderableWidget(Button.builder(Component.translatable("gui.sololeveling.recall_all"), b -> {
            PacketHandler.INSTANCE.sendToServer(new C2SKeybindActionPacket(C2SKeybindActionPacket.Action.RECALL_ALL));
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
        }).bounds(leftPos + 8, topPos + 210, 160, 20).build());

        createDetailWidgets();
        rebuildShadowList();
        setView(View.LIST);
    }

    private void setView(View view) {
        this.currentView = view;
        renameBox.setVisible(view == View.RENAMING);
        confirmRenameButton.visible = view == View.RENAMING;
        cancelRenameButton.visible = view == View.RENAMING;
        detailButtons.forEach(b -> b.visible = (view == View.DETAILS));

        if (view == View.RENAMING && selectedShadow != null) {
            renameBox.setValue(selectedShadow.getCustomName());
            renameBox.setFocused(true);
        } else {
            renameBox.setFocused(false);
        }
    }

    private void scroll(int direction) {
        int maxScroll = Math.max(0, army.size() - 5);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset + direction, maxScroll));
        rebuildShadowList();
    }

    private void clearShadowListWidgets() {
        shadowListButtons.forEach(this::removeWidget);
        shadowListButtons.clear();
    }

    private void rebuildShadowList() {
        clearShadowListWidgets();
        int listSize = Math.min(5, army.size() - scrollOffset);
        for (int i = 0; i < listSize; i++) {
            ShadowInfo info = army.get(i + scrollOffset);
            int x = leftPos + 8;
            int y = topPos + 18 + (i * 22); // Increased spacing between buttons

            Button btn = Button.builder(Component.literal(info.getShortenedName()), b -> selectShadow(info))
                    .bounds(x, y, 200, 20) // Wider buttons for better readability
                    .tooltip(createShadowTooltip(info))
                    .build();
            this.shadowListButtons.add(btn);
            addRenderableWidget(btn);
        }
    }

    private Tooltip createShadowTooltip(ShadowInfo info) {
        Component text = Component.literal(info.getCustomName()).withStyle(ChatFormatting.GOLD)
                .append(Component.literal("\nGrade: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(info.getGrade().name()).withStyle(info.getGrade().getDisplayFormatting()))
                .append(Component.literal("\nLevel: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(info.getLevel())).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nXP: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format("%d / %d", info.getXp(), info.getNextLevelXp())).withStyle(ChatFormatting.WHITE));
        return Tooltip.create(text);
    }

    private void selectShadow(ShadowInfo info) {
        if (this.selectedShadow == info) {
            this.selectedShadow = null;
            this.renderedEntity = null;
            setView(View.LIST);
        } else {
            this.selectedShadow = info;
            if(this.minecraft != null && this.minecraft.level != null) {
                EntityType.byString(info.getOriginalEntityType().toString()).ifPresent(type -> {
                    this.renderedEntity = (LivingEntity) type.create(Objects.requireNonNull(this.minecraft.level));
                    if (this.renderedEntity != null) {
                        this.renderedEntity.load(info.getOriginalNbt());
                        // Reset entity to clean state for rendering
                        this.renderedEntity.setHealth(this.renderedEntity.getMaxHealth());
                        this.renderedEntity.hurtTime = 0;
                        this.renderedEntity.hurtDuration = 0;
                        this.renderedEntity.deathTime = 0;
                        this.renderedEntity.invulnerableTime = 0;
                        this.renderedEntity.clearFire();
                        // Remove any potion effects that might affect rendering
                        this.renderedEntity.removeAllEffects();
                    }
                });
            }
            setView(View.DETAILS);
        }
    }

    private void handleConfirmRename() {
        if (currentView == View.RENAMING && !this.renameBox.getValue().isEmpty() && selectedShadow != null) {
            PacketHandler.INSTANCE.sendToServer(new C2SShadowActionPacket(C2SShadowActionPacket.Action.RENAME, selectedShadow.getShadowId(), this.renameBox.getValue()));
            selectedShadow.setCustomName(this.renameBox.getValue());
            rebuildShadowList();
            setView(View.DETAILS);
        }
    }

    private void createDetailWidgets() {
        detailButtons.clear();

        // Left column for entity preview area
        int leftColumnX = this.leftPos + 8;
        int rightColumnX = this.leftPos + 130; // Right column for buttons
        int topButtonY = this.topPos + 140;
        int buttonWidth = 60;
        int buttonHeight = 20;
        int buttonSpacing = 4;

        // Summon button (wide, top)
        Button summonButton = Button.builder(Component.translatable("gui.sololeveling.summon"), b -> {
            if(selectedShadow != null) {
                PacketHandler.INSTANCE.sendToServer(new C2SShadowActionPacket(C2SShadowActionPacket.Action.SUMMON, selectedShadow.getShadowId(), ""));
                if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.closeContainer();
            }
        }).bounds(rightColumnX, topButtonY, (buttonWidth * 2) + buttonSpacing, buttonHeight).build();

        // Recall and Rename buttons (side by side)
        Button recallButton = Button.builder(Component.translatable("gui.sololeveling.recall"), b -> {
            if(selectedShadow != null) {
                PacketHandler.INSTANCE.sendToServer(new C2SShadowActionPacket(C2SShadowActionPacket.Action.RECALL, selectedShadow.getShadowId(), ""));
                selectShadow(selectedShadow);
            }
        }).bounds(rightColumnX, topButtonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight).build();

        Button renameButton = Button.builder(Component.translatable("gui.sololeveling.rename"), b -> setView(View.RENAMING))
                .bounds(rightColumnX + buttonWidth + buttonSpacing, topButtonY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight).build();

        // Release button (wide, bottom)
        Button releaseButton = Button.builder(Component.translatable("gui.sololeveling.release"), b -> {
            if(selectedShadow != null) {
                PacketHandler.INSTANCE.sendToServer(new C2SShadowActionPacket(C2SShadowActionPacket.Action.RELEASE, selectedShadow.getShadowId(), ""));
                this.army.remove(selectedShadow);
                selectShadow(selectedShadow);
                rebuildShadowList();
            }
        }).bounds(rightColumnX, topButtonY + (buttonHeight + buttonSpacing) * 2, (buttonWidth * 2) + buttonSpacing, buttonHeight).build();

        detailButtons.addAll(List.of(summonButton, recallButton, renameButton, releaseButton));
        detailButtons.forEach(this::addRenderableWidget);
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        if (this.renderedEntity != null && currentView == View.DETAILS) {
            // Position entity preview in the left side of details view
            int previewX = this.leftPos + 60;
            int previewY = this.topPos + 180;
            int size = 35;

            // Make it look away from the mouse
            float lookX = (float)(this.leftPos + 60) - pMouseX;
            float lookY = (float)(this.topPos + 145) - pMouseY;

            // Ensure the entity is in a clean state for rendering
            this.renderedEntity.setHealth(this.renderedEntity.getMaxHealth());
            this.renderedEntity.hurtTime = 0;
            this.renderedEntity.hurtDuration = 0;
            this.renderedEntity.deathTime = 0;
            this.renderedEntity.invulnerableTime = 0;
            this.renderedEntity.clearFire();

            InventoryScreen.renderEntityInInventoryFollowsMouse(pGuiGraphics, previewX, previewY, size, lookX, lookY, this.renderedEntity);
        }

        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        // Draw a simple background
        pGuiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xC0101010);
        pGuiGraphics.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + this.imageWidth - 1, this.topPos + this.imageHeight - 1, 0x80000000);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        if (selectedShadow != null && currentView == View.DETAILS) {
            // Draw separator line
            int separatorY = 128;
            pGuiGraphics.fill(8, separatorY, this.imageWidth - 8, separatorY + 1, 0xFF808080);

            // Draw selected shadow name
            pGuiGraphics.drawString(this.font, selectedShadow.getCustomName(), 8, 132, 0xFFD700, true);
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.renameBox.isFocused()) {
            if (pKeyCode == GLFW.GLFW_KEY_ENTER || pKeyCode == GLFW.GLFW_KEY_KP_ENTER) {
                handleConfirmRename();
                return true;
            }
            return this.renameBox.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}