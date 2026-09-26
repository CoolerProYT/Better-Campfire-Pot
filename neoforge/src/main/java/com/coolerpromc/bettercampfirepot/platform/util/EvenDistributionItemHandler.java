package com.coolerpromc.bettercampfirepot.platform.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

public class EvenDistributionItemHandler implements IItemHandlerModifiable {
    private final InvWrapper wrapped;
    private final BooleanSupplier evenDistribution;

    public EvenDistributionItemHandler(InvWrapper wrapped, BooleanSupplier evenDistribution) {
        this.wrapped = wrapped;
        this.evenDistribution = evenDistribution;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!evenDistribution.getAsBoolean()) {
            return wrapped.insertItem(slot, stack, simulate);
        }

        if (stack.isEmpty() || !isItemValid(slot, stack)) {
            return stack;
        }

        List<Integer> validSlots = new ArrayList<>();
        for (int i = 0; i < this.getSlots(); i++) {
            if (isItemValid(i, stack)) {
                validSlots.add(i);
            }
        }

        if (validSlots.size() <= 1) {
            return wrapped.insertItem(slot, stack, simulate);
        }

        // Sort valid slots by current count (ascending) so lowest-filled slots are tried first
        validSlots.sort(Comparator.comparingInt(s -> {
            ItemStack slotStack = this.getStackInSlot(s);
            return slotStack.isEmpty() ? 0 : slotStack.getCount();
        }));

        // Try inserting into each valid slot in order, carrying over the remainder
        ItemStack remaining = stack;
        for (int targetSlot : validSlots) {
            if (remaining.isEmpty()) break;
            remaining = wrapped.insertItem(targetSlot, remaining, simulate);
        }

        return remaining;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        wrapped.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return wrapped.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return wrapped.getStackInSlot(slot);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return wrapped.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return wrapped.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return wrapped.isItemValid(slot, stack);
    }
}
