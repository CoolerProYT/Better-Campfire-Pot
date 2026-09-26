package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.block.entity.renderer.BetterCampfireBER;
import com.coolerpromc.bettercampfirepot.menu.CookingPotScreen;
import com.coolerpromc.bettercampfirepot.network.TierSpeedSyncPacket;
import com.coolerpromc.bettercampfirepot.network.ValidItemSyncPacket;
import com.coolerpromc.bettercampfirepot.platform.ServicesClient;
import net.minecraft.client.renderer.RenderType;

public class BetterCampfirePotClient {
    public static void initAll(){
        initRenderer();
        initMenuScreen();
        initBlockRenderType();
        initClientPayloadHandler();
    }

    public static void initRenderer(){
        ServicesClient.REGISTRY.registerBlockEntityRenderer(BetterCampfirePot.BETTER_CAMPFIRE_BE.get(), BetterCampfireBER::new);
    }

    public static void initMenuScreen(){
        ServicesClient.REGISTRY.registerMenuScreen(BetterCampfirePot.BETTER_CAMPFIRE_MENU.get(), CookingPotScreen::new);
    }

    public static void initBlockRenderType(){
        ServicesClient.REGISTRY.registerBlockRenderType(BetterCampfirePot.BETTER_CAMPFIRE_BLOCK, RenderType.cutout());
        ServicesClient.REGISTRY.registerBlockRenderType(BetterCampfirePot.BETTER_SOUL_CAMPFIRE_BLOCK, RenderType.cutout());
        BetterCampfirePot.CAMPFIRE_POTS.forEach(block -> ServicesClient.REGISTRY.registerBlockRenderType(block, RenderType.cutout()));
    }

    public static void initClientPayloadHandler(){
        ServicesClient.REGISTRY.registerClientPayloadReceiver(ValidItemSyncPacket.TYPE);
        ServicesClient.REGISTRY.registerClientPayloadReceiver(TierSpeedSyncPacket.TYPE);
    }
}
