package com.coolerpromc.bettercampfirepot.menu;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.menu.widget.BetterCookButton;
import com.coolerpromc.bettercampfirepot.network.ToggleCookingPotLidPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;
import static com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity.IS_LID_OPEN_INDEX;

public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(BetterCampfirePot.MODID, "textures/gui/campfire_pot.png");
    private final ResourceLocation COOK_PROGRESS_SPRITE = cobblemonResource("textures/gui/campfirepot/cook_progress.png");

    private BetterCookButton cookButton;

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
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 176, 166);
        int cookProgress = (int) Math.ceil(menu.getBurnProgress() * 22);
        guiGraphics.blit(COOK_PROGRESS_SPRITE, leftPos + 96, topPos + 39, 0, 0, cookProgress, 12, 22, 12);
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
}
