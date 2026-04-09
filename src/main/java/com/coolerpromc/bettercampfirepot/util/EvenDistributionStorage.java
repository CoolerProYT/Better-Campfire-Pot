package com.coolerpromc.bettercampfirepot.util;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A Storage wrapper that distributes inserted items evenly across all valid slots
 * instead of filling the first slot completely before moving to the next.
 */
public class EvenDistributionStorage implements Storage<ItemVariant> {
    private final InventoryStorage wrapped;

    public EvenDistributionStorage(InventoryStorage wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        List<SingleSlotStorage<ItemVariant>> candidateSlots = new ArrayList<>();
        for (SingleSlotStorage<ItemVariant> slot : wrapped.getSlots()) {
            if (slot.getAmount() < slot.getCapacity()) {
                if (slot.isResourceBlank() || slot.getResource().equals(resource)) {
                    candidateSlots.add(slot);
                }
            }
        }

        if (candidateSlots.isEmpty()) {
            return 0;
        }

        long totalInserted = 0;
        long remaining = maxAmount;


        while (remaining > 0) {
            SingleSlotStorage<ItemVariant> minSlot = null;
            long minAmount = Long.MAX_VALUE;

            for (SingleSlotStorage<ItemVariant> slot : candidateSlots) {
                long amount = slot.getAmount();
                if (amount < minAmount) {
                    minAmount = amount;
                    minSlot = slot;
                }
            }

            if (minSlot == null) break;

            long inserted = minSlot.insert(resource, 1, transaction);
            if (inserted <= 0) {
                candidateSlots.remove(minSlot);
                if (candidateSlots.isEmpty()) break;
                continue;
            }

            totalInserted += inserted;
            remaining -= inserted;

            // Remove slot if it's now full
            if (minSlot.getAmount() >= minSlot.getCapacity()) {
                candidateSlots.remove(minSlot);
                if (candidateSlots.isEmpty()) break;
            }
        }

        return totalInserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return wrapped.extract(resource, maxAmount, transaction);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return wrapped.iterator();
    }
}

