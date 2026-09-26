package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
import com.coolerpromc.bettercampfirepot.platform.ServicesClient;
import com.coolerpromc.bettercampfirepot.platform.util.FabricClientPayloadContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class FabricBetterCampfirePotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterCampfirePotClient.initAll();
        ServicesClient.REGISTRY.applyBlockRenderTypeRegistrations(BlockRenderLayerMap.INSTANCE::putBlock);
        ServicesClient.REGISTRY.applyMenuScreenRegistrations(MenuScreens::register);
        ServicesClient.REGISTRY.applyBlockEntityRendererRegistrations(BlockEntityRenderers::register);
        ServicesClient.REGISTRY.applyClientPayloadReceiverRegistrations(FabricBetterCampfirePotClient::registerPayloadReceiver);
    }

    private static <T extends HandledCustomPacketPayload> void registerPayloadReceiver(CustomPacketPayload.Type<T> type) {
        ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handle(new FabricClientPayloadContext(context)));
    }
}
