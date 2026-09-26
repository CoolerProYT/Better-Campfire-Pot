package com.coolerpromc.bettercampfirepot.platform.services.client;

import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
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

import java.util.function.Supplier;

public interface IRegistryHelper {
    <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> provider);
    <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor);
    void registerBlockRenderType(Supplier<? extends Block> block, RenderType renderType);
    <T extends HandledCustomPacketPayload> void registerClientPayloadReceiver(CustomPacketPayload.Type<T> type);

    void applyBlockEntityRendererRegistrations(BlockEntityRendererRegistrar registrar);
    void applyMenuScreenRegistrations(MenuScreenRegistrar registrar);
    void applyBlockRenderTypeRegistrations(BlockRenderTypeRegistrar registrar);
    void applyClientPayloadReceiverRegistrations(ClientPayloadReceiverRegistrar registrar);

    interface BlockEntityRendererRegistrar{
        <T extends BlockEntity> void register(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> provider);
    }
    interface MenuScreenRegistrar{
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor);
    }
    interface BlockRenderTypeRegistrar{
        void register(Block block, RenderType renderType);
    }
    interface ClientPayloadReceiverRegistrar{
        <T extends HandledCustomPacketPayload> void register(CustomPacketPayload.Type<T> type);
    }
}
