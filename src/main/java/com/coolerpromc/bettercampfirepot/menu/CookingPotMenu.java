package com.coolerpromc.bettercampfirepot.menu;

import com.cobblemon.mod.common.api.cooking.Seasonings;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CookingPotMenu extends AbstractContainerMenu {
    public final ContainerData containerData;
    public final BetterCampfireBlockEntity blockEntity;

    public CookingPotMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, (BetterCampfireBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(4));
    }

    public CookingPotMenu(int containerId, Inventory playerInventory, BetterCampfireBlockEntity blockEntity, ContainerData containerData){
        super(BetterCampfirePot.BETTER_CAMPFIRE_MENU.get(), containerId);
        this.containerData = containerData;
        this.blockEntity = blockEntity;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        IItemHandler inputHandler = blockEntity.inputHandler;
        for (int i = 0; i < inputHandler.getSlots();i++){
            int x = i % 3;
            int y = i / 3;
            addSlot(new SlotItemHandler(inputHandler, i, 33 + x * 18, 18 + y * 18));
        }

        IItemHandler seasoningHandler = blockEntity.seasoningHandler;
        for (int i = 0; i < seasoningHandler.getSlots(); i++) {
            addSlot(new SlotItemHandler(seasoningHandler, i, 110 + i * 18, 18));
        }

        IItemHandler outputHandler = blockEntity.outputHandler;
        addSlot(new SlotItemHandler(outputHandler, 0, 128, 55));

        addDataSlots(containerData);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotItemStack = slot.getItem();
            itemStack = slotItemStack.copy();

            if (index == 48) {
                if (!this.moveItemStackTo(slotItemStack, 0, 26 + 1, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(slotItemStack, itemStack);
            } else if (index >= 0 && index < 36) {
                if (Seasonings.INSTANCE.isSeasoning(slotItemStack)) {
                    if (!this.moveItemStackTo(slotItemStack, 45, 47 + 1, false) && !this.moveItemStackTo(slotItemStack, 36, 44 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotItemStack, 36, 44 + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 36 && index < 45 || index >= 45 && index < 48) {
                if (!this.moveItemStackTo(slotItemStack, 0, 35 + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotItemStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotItemStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotItemStack);
        }

        return itemStack;

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public float getBurnProgress() {
        int i = this.containerData.get(0);
        int j = this.containerData.get(1);
        if (j != 0 && i != 0) {
            return Mth.clamp(((float) i / (float) j), 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

}
