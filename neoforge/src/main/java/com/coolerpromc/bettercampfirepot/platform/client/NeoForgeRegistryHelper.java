package com.coolerpromc.bettercampfirepot.platform.client;

import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
import com.coolerpromc.bettercampfirepot.platform.services.client.IRegistryHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    private final List<BlockEntityRendererEntry<?>> blockEntityRenders = new ArrayList<>();
    private final List<MenuScreenEntry<?, ?>> menuScreens = new ArrayList<>();
    private final List<BlockRenderTypeEntry> blockRenderTypes = new ArrayList<>();
    private final List<ClientPayloadReceiverEntry<?>> clientPayloadReceivers = new ArrayList<>();

    @Override
    public <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> provider) {
        this.blockEntityRenders.add(new BlockEntityRendererEntry<>(blockEntityType, provider));
    }

    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        this.menuScreens.add(new MenuScreenEntry<>(menuType, screenConstructor));
    }

    @Override
    public void registerBlockRenderType(Supplier<? extends Block> block, RenderType renderType) {
        this.blockRenderTypes.add(new BlockRenderTypeEntry(block, renderType));
    }

    @Override
    public <T extends HandledCustomPacketPayload> void registerClientPayloadReceiver(CustomPacketPayload.Type<T> type) {
        this.clientPayloadReceivers.add(new ClientPayloadReceiverEntry<>(type));
    }

    @Override
    public void applyBlockEntityRendererRegistrations(BlockEntityRendererRegistrar registrar) {
        for (BlockEntityRendererEntry<?> entry : blockEntityRenders) {
            entry.register(registrar);
        }
    }

    @Override
    public void applyMenuScreenRegistrations(MenuScreenRegistrar registrar) {
        for (MenuScreenEntry<?, ?> entry : menuScreens) {
            entry.register(registrar);
        }
    }

    @Override
    public void applyBlockRenderTypeRegistrations(BlockRenderTypeRegistrar registrar) {
        for (BlockRenderTypeEntry entry : blockRenderTypes) {
            entry.register(registrar);
        }
    }

    @Override
    public void applyClientPayloadReceiverRegistrations(ClientPayloadReceiverRegistrar registrar) {
        for (ClientPayloadReceiverEntry<?> entry : clientPayloadReceivers) {
            entry.register(registrar);
        }
    }

    private record BlockEntityRendererEntry<T extends BlockEntity>(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> provider) {
        private void register(BlockEntityRendererRegistrar registrar) {
            registrar.register(this.blockEntityType, this.provider);
        }
    }

    private record MenuScreenEntry<M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>>(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor){
        private void register(MenuScreenRegistrar registrar){
            registrar.register(this.menuType, this.screenConstructor);
        }
    }

    private record BlockRenderTypeEntry(Supplier<? extends Block> block, RenderType renderType){
        private void register(BlockRenderTypeRegistrar registrar){
            registrar.register(this.block.get(), this.renderType);
        }
    }

    private record ClientPayloadReceiverEntry<T extends HandledCustomPacketPayload>(CustomPacketPayload.Type<T> type){
        private void register(ClientPayloadReceiverRegistrar registrar){
            registrar.register(this.type);
        }
    }
}
