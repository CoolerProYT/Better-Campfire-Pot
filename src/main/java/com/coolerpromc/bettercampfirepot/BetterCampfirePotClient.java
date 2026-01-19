package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.block.entity.renderer.BetterCampfireBER;
import com.coolerpromc.bettercampfirepot.menu.CookingPotScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@SuppressWarnings("deprecation")
@Mod(value = BetterCampfirePot.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BetterCampfirePot.MODID, value = Dist.CLIENT)
public class BetterCampfirePotClient {
    public BetterCampfirePotClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(BetterCampfirePot.BETTER_CAMPFIRE_BLOCK.get(), RenderType.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(BetterCampfirePot.BETTER_SOUL_CAMPFIRE_BLOCK.get(), RenderType.CUTOUT);
            BetterCampfirePot.CAMPFIRE_POTS.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.CUTOUT));
        });
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(BetterCampfirePot.BETTER_CAMPFIRE_MENU.get(), CookingPotScreen::new);
    }

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BetterCampfirePot.BETTER_CAMPFIRE_BE.get(), BetterCampfireBER::new);
    }
}
