package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.block.entity.renderer.BetterCampfireBER;
import com.coolerpromc.bettercampfirepot.menu.CookingPotScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class BetterCampfirePotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(BetterCampfirePot.BETTER_CAMPFIRE_BLOCK, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(BetterCampfirePot.BETTER_SOUL_CAMPFIRE_BLOCK, RenderType.cutout());
        BetterCampfirePot.CAMPFIRE_POTS.forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));

        MenuScreens.register(BetterCampfirePot.BETTER_CAMPFIRE_MENU, CookingPotScreen::new);
        BlockEntityRenderers.register(BetterCampfirePot.BETTER_CAMPFIRE_BE, BetterCampfireBER::new);
    }
}
