package com.coolerpromc.bettercampfirepot.platform.services;

import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
import com.coolerpromc.bettercampfirepot.platform.util.BlockEntityTypeFactory;
import com.coolerpromc.bettercampfirepot.platform.util.CreativeTabOutput;
import com.coolerpromc.bettercampfirepot.platform.util.MenuFactory;
import com.coolerpromc.bettercampfirepot.platform.util.RegistryHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IRegistryHelper {
    default <T extends Block> RegistryHandler.Blocks<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func){
        return registerBlock(name, func, BlockBehaviour.Properties.of());
    }
    <T extends Block> RegistryHandler.Blocks<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, BlockBehaviour.Properties p);
    default <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func){
        return registerItem(name, func, new Item.Properties());
    }
    <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func, Item.Properties p);
    <T extends BlockEntity> RegistryHandler<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntityType(String name, BlockEntityTypeFactory<T> factory, List<Supplier<? extends Block>> blocks);
    RegistryHandler<CreativeModeTab, CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, Component title, BiConsumer<CreativeTabOutput, CreativeModeTab.ItemDisplayParameters> entries);
    <T extends AbstractContainerMenu> RegistryHandler<MenuType<?>, MenuType<T>> registerMenuType(String name, MenuFactory<T> factory);

    <T extends HandledCustomPacketPayload> void registerClientboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec);
    void applyClientboundPayloadRegistrations(PayloadRegistrar registrar);

    <T extends HandledCustomPacketPayload> void registerServerboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec);
    void applyServerboundPayloadRegistrations(PayloadRegistrar registrar);

    interface PayloadRegistrar {
        <T extends HandledCustomPacketPayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec);
    }
}
