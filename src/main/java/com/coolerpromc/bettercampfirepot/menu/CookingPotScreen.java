package com.coolerpromc.bettercampfirepot.menu;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.menu.widget.BetterCookButton;
import com.coolerpromc.bettercampfirepot.menu.widget.ChangeCapabilityButton;
import com.coolerpromc.bettercampfirepot.menu.widget.ToggleConfigButton;
import com.coolerpromc.bettercampfirepot.network.CapabilityChangeSyncC2SPacket;
import com.coolerpromc.bettercampfirepot.network.ToggleCookingPotLidPacket;
import com.coolerpromc.bettercampfirepot.util.Side;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;
import static com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity.IS_LID_OPEN_INDEX;

public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(BetterCampfirePot.MODID, "textures/gui/campfire_pot.png");
    private final ResourceLocation COOK_PROGRESS_SPRITE = cobblemonResource("textures/gui/campfirepot/cook_progress.png");

    private BetterCookButton cookButton;
    private ToggleConfigButton toggleConfigButton;
    private final List<ChangeCapabilityButton> capabilityButtons = new ArrayList<>();

    public CookingPotScreen(CookingPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = this.leftPos + this.imageWidth - (this.width / 2) - (this.font.width(this.title) / 2);
        this.titleLabelY = 6;
        this.inventoryLabelY = 99999;

        if (cookButton != null) removeWidget(cookButton);
        if (menu.blockEntity.getPotItem() != null && menu.blockEntity.getPotItem().getItem() instanceof BetterCampfirePotItem campfirePotItem){
            cookButton = new BetterCookButton(this.leftPos + 97, this.topPos + 56, menu.containerData.get(IS_LID_OPEN_INDEX) == 0, campfirePotItem, button -> {
                boolean isLidClosed = menu.containerData.get(IS_LID_OPEN_INDEX) == 0;
                ClientPlayNetworking.send(new ToggleCookingPotLidPacket(isLidClosed));
            });
            addRenderableWidget(cookButton);
        }

        toggleConfigButton = new ToggleConfigButton(leftPos + imageWidth + 4, topPos + 25, 20, 20, button -> {
            if (button.isToggled()){
                openConfigScreen();
            }
            else{
                closeConfigScreen();
            }
        });
        addRenderableWidget(toggleConfigButton);

        int x = leftPos + imageWidth + 1;
        int y = topPos + 49;

        capabilityButtons.add(new ChangeCapabilityButton(x + 9, y + 4, 8, 8, this::onPress, Side.TOP, this.menu.blockEntity.getCapabilityBySide(Side.TOP)));
        capabilityButtons.add(new ChangeCapabilityButton(x + 1, y + 12, 8, 8, this::onPress, Side.LEFT, this.menu.blockEntity.getCapabilityBySide(Side.LEFT)));
        capabilityButtons.add(new ChangeCapabilityButton(x + 9, y + 12, 8, 8, this::onPress, Side.FRONT, this.menu.blockEntity.getCapabilityBySide(Side.FRONT)));
        capabilityButtons.add(new ChangeCapabilityButton(x + 17, y + 12, 8, 8, this::onPress, Side.RIGHT, this.menu.blockEntity.getCapabilityBySide(Side.RIGHT)));
        capabilityButtons.add(new ChangeCapabilityButton(x + 1, y + 20, 8, 8, this::onPress, Side.BACK, this.menu.blockEntity.getCapabilityBySide(Side.BACK)));
        capabilityButtons.add(new ChangeCapabilityButton(x + 9, y + 20, 8, 8, this::onPress, Side.BOTTOM, this.menu.blockEntity.getCapabilityBySide(Side.BOTTOM)));
    }

    private void onPress(ChangeCapabilityButton button){
        button.setSlot(button.getSlot().next());
        ClientPlayNetworking.send(new CapabilityChangeSyncC2SPacket(this.menu.blockEntity.getBlockPos(), button.getSide(), button.getSlot()));
    }

    private void openConfigScreen(){
        this.capabilityButtons.forEach(this::addRenderableWidget);
    }

    private void closeConfigScreen(){
        this.capabilityButtons.forEach(this::removeWidget);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 176, 166);
        int cookProgress = (int) Math.ceil(menu.getBurnProgress() * 22);
        guiGraphics.blit(COOK_PROGRESS_SPRITE, leftPos + 96, topPos + 39, 0, 0, cookProgress, 12, 22, 12);
        blitWithBorder(guiGraphics, BetterCampfirePot.id("textures/gui/sprites/gui.png"), leftPos + imageWidth - 1, topPos + 20, 0, 0, 30, 30, 24, 24, 4);
        if (toggleConfigButton.isToggled()){
            blitWithBorder(guiGraphics, BetterCampfirePot.id("textures/gui/sprites/gui.png"), leftPos + imageWidth - 1, topPos + 50, 24, 0, 30, 30, 24, 24, 4);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        cookButton.setSelected(menu.containerData.get(IS_LID_OPEN_INDEX) == 0);
        cookButton.setPosition(this.leftPos + 97, topPos + 56);


        Slot resultSlot = menu.getSlot(48);
        var recipe = menu.blockEntity.currentRecipe;
        if (recipe != null && !recipe.result().isEmpty() && !resultSlot.hasItem()) {
            var resultItem = recipe.result();
            recipe.applySeasoning(resultItem, menu.blockEntity.getSeasonings());

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F);

            guiGraphics.renderFakeItem(resultItem, leftPos + resultSlot.x, topPos + resultSlot.y);
            guiGraphics.renderItemDecorations(font, resultItem, leftPos + resultSlot.x, topPos + resultSlot.y);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();

            if (isHovering(resultSlot.x, resultSlot.y, 16, 16, mouseX, mouseY) && menu.getCarried().isEmpty()) {
                guiGraphics.renderTooltip(font, getTooltipFromContainerItem(resultItem), resultItem.getTooltipImage(), mouseX, mouseY);
            }
        }

        renderSlotOutline(guiGraphics);

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.index >= 45 && slot.index <= 47 && ((!menu.getCarried().isEmpty() && !slot.mayPlace(menu.getCarried())) || (slot.hasItem() && !slot.mayPlace(slot.getItem())))){
            int x = slot.x;
            int y = slot.y;
            guiGraphics.fill(x,y,x+16,y+16,822018048);
        }
        super.renderSlot(guiGraphics, slot);
    }

    public boolean isConfigOpened(){
        return toggleConfigButton.isToggled();
    }

    private void renderSlotOutline(GuiGraphics guiGraphics){
        for (ChangeCapabilityButton btn : capabilityButtons){
            if (btn.isHovered()){
                int baseX = leftPos;
                int baseY = topPos;
                int maxX = baseX;
                int maxY = baseY;

                switch (btn.getSlot()){
                    case INPUT:
                        baseX += 32;
                        baseY += 17;
                        maxX += 54 + 32;
                        maxY += 54 + 17;
                        break;
                    case SEASONING:
                        baseX += 109;
                        baseY += 17;
                        maxX += 54 + 109;
                        maxY += 18 + 17;
                        break;
                    case OUTPUT:
                        baseX += 123;
                        baseY += 50;
                        maxX += 26 + 123;
                        maxY += 26 + 50;
                        break;
                }

                guiGraphics.fill(baseX, baseY, maxX, maxY, FastColor.ARGB32.color(0x66, btn.getSlot().color()));
            }
        }
    }

    public int getGuiLeft(){
        return leftPos;
    }

    public int getGuiTop(){
        return topPos;
    }

    public int getXSize(){
        return imageWidth;
    }

    private void blitWithBorder(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int borderSize) {
        this.blitWithBorder(guiGraphics, texture, x, y, u, v, width, height, textureWidth, textureHeight, borderSize, borderSize, borderSize, borderSize);
    }

    private void blitWithBorder(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;
        guiGraphics.blit(texture, x, y, u, v, leftBorder, topBorder);
        guiGraphics.blit(texture, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder);
        guiGraphics.blit(texture, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder);
        guiGraphics.blit(texture, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder);

        for(int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); ++i) {
            guiGraphics.blit(texture, x + leftBorder + i * fillerWidth, y, u + leftBorder, v, i == xPasses ? remainderWidth : fillerWidth, topBorder);
            guiGraphics.blit(texture, x + leftBorder + i * fillerWidth, y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, i == xPasses ? remainderWidth : fillerWidth, bottomBorder);

            for(int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); ++j) {
                guiGraphics.blit(texture, x + leftBorder + i * fillerWidth, y + topBorder + j * fillerHeight, u + leftBorder, v + topBorder, i == xPasses ? remainderWidth : fillerWidth, j == yPasses ? remainderHeight : fillerHeight);
            }
        }

        for(int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); ++j) {
            guiGraphics.blit(texture, x, y + topBorder + j * fillerHeight, u, v + topBorder, leftBorder, j == yPasses ? remainderHeight : fillerHeight);
            guiGraphics.blit(texture, x + leftBorder + canvasWidth, y + topBorder + j * fillerHeight, u + leftBorder + fillerWidth, v + topBorder, rightBorder, j == yPasses ? remainderHeight : fillerHeight);
        }
    }
}
