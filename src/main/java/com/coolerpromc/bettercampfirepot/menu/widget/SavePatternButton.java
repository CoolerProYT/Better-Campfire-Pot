package com.coolerpromc.bettercampfirepot.menu.widget;

import com.coolerpromc.bettercampfirepot.network.UpdateValidItemPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

public class SavePatternButton extends AbstractButton {
    private final BlockPos pos;

    public SavePatternButton(int x, int y, BlockPos pos) {
        super(x, y, 20, 20, Component.literal("S"));
        this.pos = pos;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(SPRITES.get(this.active, this.isHovered), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = getFGColor();
        this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void onPress() {
        PacketDistributor.sendToServer(new UpdateValidItemPacket(this.pos));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
