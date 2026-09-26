package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.platform.services.ICapabilityHelper;
import com.coolerpromc.bettercampfirepot.platform.util.EvenDistributionItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeCapabilityHelper implements ICapabilityHelper {
    private final List<Entry<? extends BlockEntity>> entries = new ArrayList<>();

    @Override
    public <T extends BlockEntity> void registerBlockEntityItemStorage(Supplier<BlockEntityType<T>> type, BiFunction<T, Direction, Container> provider, Function<T, Container[]> containers, BiPredicate<T, Container> evenDistribution) {
        entries.add(new Entry<>(type, provider, containers, evenDistribution));
    }

    @Override
    public <T> void applyRegistrations(T event) {
        if (event instanceof RegisterCapabilitiesEvent capabilitiesEvent){
            for (Entry<? extends BlockEntity> entry : entries) {
                register(capabilitiesEvent, entry);
            }
        }
    }

    @Override
    public void invalidateCapabilities(Level level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    @Override
    public ItemStack insertItem(Level level, BlockPos pos, Direction side, ItemStack stack) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(handler, stack, false);
    }

    private static <T extends BlockEntity> void register(RegisterCapabilitiesEvent event, Entry<T> entry) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, entry.type().get(), (blockEntity, direction) -> {
            if (direction == null) {
                return new CombinedInvWrapper(Arrays.stream(entry.containers().apply(blockEntity)).map(container -> wrap(entry, blockEntity, container)).toArray(IItemHandlerModifiable[]::new));
            }

            Container container = entry.provider().apply(blockEntity, direction);
            return container == null ? null : wrap(entry, blockEntity, container);
        });
    }

    private static <T extends BlockEntity> IItemHandlerModifiable wrap(Entry<T> entry, T blockEntity, Container container) {
        return new EvenDistributionItemHandler(new InvWrapper(container), () -> entry.evenDistribution().test(blockEntity, container));
    }

    private record Entry<T extends BlockEntity>(Supplier<BlockEntityType<T>> type, BiFunction<T, Direction, Container> provider, Function<T, Container[]> containers, BiPredicate<T, Container> evenDistribution){
    }
}
