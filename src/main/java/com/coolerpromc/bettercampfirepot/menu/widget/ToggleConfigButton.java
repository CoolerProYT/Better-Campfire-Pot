package com.coolerpromc.bettercampfirepot.menu.widget;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ToggleConfigButton extends AbstractButton {
    private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"), ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));
    private static final ResourceLocation ICON = BetterCampfirePot.id("textures/gui/sprites/icon.png");
    private final OnPress onPress;
    private boolean isToggled = false;

    public ToggleConfigButton(int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, Component.empty());
        this.onPress = onPress;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(SPRITES.get(this.active, this.isHovered), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(ICON, this.getX() + 2, this.getY() + 2, this.isToggled ? 0 : 16, 0, 16, 16);

        if (isHovered()){
            guiGraphics.renderTooltip(minecraft.font, Component.translatable("tooltip.bettercampfirepot.toggle_config"), mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        this.isToggled = !this.isToggled;
        this.onPress.onPress(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        createNarrationMessage();
    }

    public boolean isToggled() {
        return isToggled;
    }

    public interface OnPress {
        void onPress(ToggleConfigButton button);
    }
}
