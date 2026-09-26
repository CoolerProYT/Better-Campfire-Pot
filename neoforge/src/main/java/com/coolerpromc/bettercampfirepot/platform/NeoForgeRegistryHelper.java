package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
import com.coolerpromc.bettercampfirepot.platform.services.IRegistryHelper;
import com.coolerpromc.bettercampfirepot.platform.util.BlockEntityTypeFactory;
import com.coolerpromc.bettercampfirepot.platform.util.CreativeTabOutput;
import com.coolerpromc.bettercampfirepot.platform.util.MenuFactory;
import com.coolerpromc.bettercampfirepot.platform.util.RegistryHandler;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BetterCampfirePot.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BetterCampfirePot.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BetterCampfirePot.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, BetterCampfirePot.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, BetterCampfirePot.MODID);

    private final List<PayloadEntry<?>> clientboundPayloads = new ArrayList<>();
    private final List<PayloadEntry<?>> serverboundPayloads = new ArrayList<>();

    @Override
    public <T extends Block> RegistryHandler.Blocks<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> func, BlockBehaviour.Properties p) {
        DeferredBlock<T> deferredBlock = BLOCKS.registerBlock(name, func, p);
        return () -> deferredBlock;
    }

    @Override
    public <T extends Item> RegistryHandler.Items<T> registerItem(String name, Function<Item.Properties, T> func, Item.Properties p) {
        DeferredItem<T> deferredItem = ITEMS.registerItem(name, func, p);
        return () -> deferredItem;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public <T extends BlockEntity> RegistryHandler<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntityType(String name, BlockEntityTypeFactory<T> factory, List<Supplier<? extends Block>> blocks) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> deferredHolder = BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(factory::create, blocks.stream().map(Supplier::get).toArray(Block[]::new)).build(null));
        return () -> deferredHolder;
    }

    @Override
    public RegistryHandler<CreativeModeTab, CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> icon, Component title, BiConsumer<CreativeTabOutput, CreativeModeTab.ItemDisplayParameters> entries) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> deferredHolder = CREATIVE_TABS.register(name, () -> CreativeModeTab.builder().icon(icon).title(title).displayItems((p, o) -> entries.accept(o::accept, p)).build());
        return () -> deferredHolder;
    }

    @Override
    public <T extends AbstractContainerMenu> RegistryHandler<MenuType<?>, MenuType<T>> registerMenuType(String name, MenuFactory<T> factory) {
        DeferredHolder<MenuType<?>, MenuType<T>> deferredHolder = MENUS.register(name, () -> IMenuTypeExtension.create((id, inv, buf) -> factory.create(id, inv, buf.readBlockPos())));
        return () -> deferredHolder;
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

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        MENUS.register(eventBus);
    }
}
