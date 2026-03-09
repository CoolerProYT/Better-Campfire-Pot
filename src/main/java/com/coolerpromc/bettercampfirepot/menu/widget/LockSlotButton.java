package com.coolerpromc.bettercampfirepot.menu.widget;

import com.coolerpromc.bettercampfirepot.network.ToggleLockSlotPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LockSlotButton extends AbstractButton {
    public static final ResourceLocation LOCKED = ResourceLocation.withDefaultNamespace("widget/locked_button");
    public static final ResourceLocation UNLOCKED = ResourceLocation.withDefaultNamespace("widget/unlocked_button");

    private boolean isLocked;
    private final BlockPos pos;

    public LockSlotButton(int x, int y, boolean isLocked, BlockPos pos) {
        super(x, y, 20, 20,  Component.empty());
        this.isLocked = isLocked;
        this.pos = pos;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(this.isLocked ? LOCKED : UNLOCKED, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (isHovered){
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + 1, 0xFFFFFFFF);
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.getHeight(), 0xFFFFFFFF);
            guiGraphics.fill(this.getX(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFFFFFFF);
            guiGraphics.fill(this.getX() + this.getWidth() - 1, this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xFFFFFFFF);
        }
    }

    @Override
    public void onPress() {
        this.isLocked = !isLocked;
        ClientPlayNetworking.send(new ToggleLockSlotPacket(this.pos));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}