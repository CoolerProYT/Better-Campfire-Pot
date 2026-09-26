package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
import com.coolerpromc.bettercampfirepot.platform.services.IRegistryHelper;
import com.coolerpromc.bettercampfirepot.platform.util.BlockEntityTypeFactory;
import com.coolerpromc.bettercampfirepot.platform.util.CreativeTabOutput;
import com.coolerpromc.bettercampfirepot.platform.util.MenuFactory;
import com.coolerpromc.bettercampfirepot.platform.util.RegistryHandler;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {
    private final List<PayloadEntry<?>> clientboundPayloads = new ArrayList<>();
    private final List<PayloadEntry<?>> serverboundPayloads = new ArrayList<>();

    @Override
    public <T extends Block> RegistryHandler.Blocks<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, BlockBehaviour.Properties p) {
        Holder<Block> holder = Registry.registerForHolder(BuiltInRegistries.BLOCK, BetterCampfirePot.id(name), func.apply(p));
        return () -> holder;
    }

    @Override
    public <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func, Item.Properties p) {
        Holder<Item> holder = Registry.registerForHolder(BuiltInRegistries.ITEM, BetterCampfirePot.id(name), func.apply(p));
        return () -> holder;
    }

    @Override
    public <T extends BlockEntity> RegistryHandler<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntityType(String name, BlockEntityTypeFactory<T> factory, List<Supplier<? extends Block>> blocks) {
        Holder<BlockEntityType<?>> holder = Registry.registerForHolder(BuiltInRegistries.BLOCK_ENTITY_TYPE, BetterCampfirePot.id(name), BlockEntityType.Builder.of(factory::create, blocks.stream().map(Supplier::get).toArray(Block[]::new)).build(null));
        return () -> holder;
    }

    @Override
    public RegistryHandler<CreativeModeTab, CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, Component title, BiConsumer<CreativeTabOutput, CreativeModeTab.ItemDisplayParameters> entries) {
        Holder<CreativeModeTab> holder = Registry.registerForHolder(BuiltInRegistries.CREATIVE_MODE_TAB, BetterCampfirePot.id(name), FabricItemGroup.builder().icon(icon).title(title).displayItems((p, o) -> entries.accept(o::accept, p)).build());
        return () -> holder;
    }

    @Override
    public <T extends AbstractContainerMenu> RegistryHandler<MenuType<?>, MenuType<T>> registerMenuType(String name, MenuFactory<T> factory) {
        Holder<MenuType<?>> holder = Registry.registerForHolder(BuiltInRegistries.MENU, BetterCampfirePot.id(name), new ExtendedScreenHandlerType<>(factory::create, BlockPos.STREAM_CODEC));
        return () -> holder;
    }

    @Override
    public <T extends HandledCustomPacketPayload> void registerClientboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this.clientboundPayloads.add(new PayloadEntry<>(type, streamCodec));
    }

    @Override
    public void applyClientboundPayloadRegistrations(PayloadRegistrar registrar) {
        for (PayloadEntry<?> entry : clientboundPayloads) {
            entry.register(registrar);
        }
    }

    @Override
    public <T extends HandledCustomPacketPayload> void registerServerboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this.serverboundPayloads.add(new PayloadEntry<>(type, streamCodec));
    }

    @Override
    public void applyServerboundPayloadRegistrations(PayloadRegistrar registrar) {
        for (PayloadEntry<?> entry : serverboundPayloads) {
            entry.register(registrar);
        }
    }

    private record PayloadEntry<T extends HandledCustomPacketPayload>(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec){
        private void register(PayloadRegistrar registrar){
            registrar.register(this.type, this.streamCodec);
        }
    }
}
