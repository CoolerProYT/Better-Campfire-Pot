package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.platform.services.ICapabilityHelper;
import com.coolerpromc.bettercampfirepot.util.EvenDistributionStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricCapabilityHelper implements ICapabilityHelper {
    private final List<Entry<? extends BlockEntity>> entries = new ArrayList<>();

    @Override
    public <T extends BlockEntity> void registerBlockEntityItemStorage(Supplier<BlockEntityType<T>> type, BiFunction<T, Direction, Container> provider, Function<T, Container[]> containers, BiPredicate<T, Container> evenDistribution) {
        entries.add(new Entry<>(type, provider, containers, evenDistribution));
    }

    @Override
    public <T> void applyRegistrations(T event) {
        for (Entry<? extends BlockEntity> entry : entries) {
            register(entry);
        }
    }

    @Override
    public void invalidateCapabilities(Level level, BlockPos pos) {
        // Fabric's block API lookups are not cached, so there is nothing to invalidate.
    }

    @Override
    public ItemStack insertItem(Level level, BlockPos pos, Direction side, ItemStack stack) {
        Storage<ItemVariant> container = ItemStorage.SIDED.find(level, pos, side);
        ItemStack remainder = stack.copy();

        if (container != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                ItemVariant variant = ItemVariant.of(remainder);

                long inserted = container.insert(variant, remainder.getCount(), transaction);

                if (inserted > 0) {
                    remainder.shrink((int) inserted);
                    transaction.commit();
                }
            }
        }

        return remainder;
    }

    private static <T extends BlockEntity> void register(Entry<T> entry) {
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            if (direction == null) {
                return new CombinedSlottedStorage<>(Arrays.stream(entry.containers().apply(blockEntity)).map(container -> InventoryStorage.of(container, null)).toList());
            }

            Container container = entry.provider().apply(blockEntity, direction);
            if (container == null) {
                return null;
            }

            InventoryStorage storage = InventoryStorage.of(container, null);
            return entry.evenDistribution().test(blockEntity, container) ? new EvenDistributionStorage(storage) : storage;
        }, entry.type().get());
    }

    private record Entry<T extends BlockEntity>(Supplier<BlockEntityType<T>> type, BiFunction<T, Direction, Container> provider, Function<T, Container[]> containers, BiPredicate<T, Container> evenDistribution){
    }
}
