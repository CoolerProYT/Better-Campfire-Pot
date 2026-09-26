package com.coolerpromc.bettercampfirepot.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ICapabilityHelper {
    <T extends BlockEntity> void registerBlockEntityItemStorage(Supplier<BlockEntityType<T>> type, BiFunction<T, Direction, Container> provider, Function<T, Container[]> containers, BiPredicate<T, Container> evenDistribution);
    <T> void applyRegistrations(T event);
    void invalidateCapabilities(Level level, BlockPos pos);
    ItemStack insertItem(Level level, BlockPos pos, Direction side, ItemStack stack);
}
