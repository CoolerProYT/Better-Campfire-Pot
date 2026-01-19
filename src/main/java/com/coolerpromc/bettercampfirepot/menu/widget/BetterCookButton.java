package com.coolerpromc.bettercampfirepot.menu.widget;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import static com.cobblemon.mod.common.api.gui.GuiUtilsKt.blitk;
import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

public class BetterCookButton extends Button {
    private static final float SIZE = 20F;
    private static final ResourceLocation BUTTON_RESOURCE = cobblemonResource("textures/gui/campfirepot/button.png");

    private boolean selected;
    private final BetterCampfirePotItem potItem;

    public BetterCookButton(int pX, int pY, boolean selected, BetterCampfirePotItem potItem, OnPress onPress) {
        super(pX, pY, (int) SIZE, (int) SIZE, Component.literal("Cook"), onPress, DEFAULT_NARRATION);
        this.selected = selected;
        this.potItem = potItem;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int pMouseX, int pMouseY, float pPartialTicks) {
        ResourceLocation closedIcon = BetterCampfirePot.id("textures/item/campfire_pot/campfire_pot_" + potItem.color.getSuffix() + ".png");
        ResourceLocation closedIconOverlay = BetterCampfirePot.id("textures/item/tier/" + potItem.tier + ".png");
        ResourceLocation openIcon = BetterCampfirePot.id("textures/item/campfire_pot/campfire_pot_" + potItem.color.getSuffix() + "_open.png");
        ResourceLocation openIconOverlay = BetterCampfirePot.id("textures/item/tier/" + potItem.tier + "_open.png");

        blitk(
                context.pose(),
                BUTTON_RESOURCE,
                (double) getX(),
                (double) getY(),
                SIZE,
                SIZE,
                0F,
                isMouseOver(pMouseX, pMouseY) ? SIZE : 0F,
                SIZE,
                SIZE * 2
        );

        blitk(
                context.pose(),
                selected ? closedIcon : openIcon,
                (double) (getX() + 2),
                (double) (getY() + 2),
                16F,
                16F,
                0F,
                0F,
                16F,
                16F
        );

        blitk(
                context.pose(),
                selected ? closedIconOverlay : openIconOverlay,
                (double) (getX() + 2),
                (double) (getY() + 2),
                16F,
                16F,
                0F,
                0F,
                16F,
                16F
        );
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
