package com.coolerpromc.bettercampfirepot.menu.widget;

import com.coolerpromc.bettercampfirepot.util.CampfirePotSlot;
import com.coolerpromc.bettercampfirepot.util.Side;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ChangeCapabilityButton extends AbstractButton {
    private final Side side;
    private CampfirePotSlot slot;
    private final OnPress onPress;

    public ChangeCapabilityButton(int x, int y, int width, int height, OnPress onPress, Side direction, CampfirePotSlot slot) {
        super(x, y, width, height, Component.empty());
        this.side = direction;
        this.slot = slot;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(ResourceLocation.withDefaultNamespace("widget/button"), this.getX(), this.getY(), this.getWidth(), this.getHeight());

        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + 1, slot.color());
        guiGraphics.fill(this.getX(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth(), this.getY() + this.getHeight(), slot.color());
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.getHeight(), slot.color());
        guiGraphics.fill(this.getX() + this.getWidth() - 1, this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), slot.color());

        if (this.isHovered){
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable("screen.bettercampfirepot.side_" + side.getName().toLowerCase(Locale.ROOT)));
            tooltips.add(Component.translatable("screen.bettercampfirepot.change_slot").withStyle(ChatFormatting.GRAY));
            guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltips, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        createNarrationMessage();
    }

    public void setSlot(CampfirePotSlot slot) {
        this.slot = slot;
    }

    public CampfirePotSlot getSlot() {
        return slot;
    }

    public Side getSide() {
        return side;
    }

    @FunctionalInterface
    public interface OnPress{
        void onPress(ChangeCapabilityButton button);
    }
}