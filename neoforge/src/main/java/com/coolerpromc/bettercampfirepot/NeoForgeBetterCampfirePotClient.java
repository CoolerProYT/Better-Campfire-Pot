package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.platform.ServicesClient;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@SuppressWarnings("deprecation")
@Mod(value = BetterCampfirePot.MODID, dist = Dist.CLIENT)
public class NeoForgeBetterCampfirePotClient {
    public NeoForgeBetterCampfirePotClient(IEventBus eventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        eventBus.addListener(NeoForgeBetterCampfirePotClient::onFMLClientSetup);
        eventBus.addListener(NeoForgeBetterCampfirePotClient::onRegisterMenuScreens);
        eventBus.addListener(NeoForgeBetterCampfirePotClient::onEntityRenderers);
    }

    private static void onFMLClientSetup(FMLClientSetupEvent event) {
        BetterCampfirePotClient.initBlockRenderType();
        event.enqueueWork(() -> ServicesClient.REGISTRY.applyBlockRenderTypeRegistrations(ItemBlockRenderTypes::setRenderLayer));
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        BetterCampfirePotClient.initMenuScreen();
        ServicesClient.REGISTRY.applyMenuScreenRegistrations(event::register);
    }

    private static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BetterCampfirePotClient.initRenderer();
        ServicesClient.REGISTRY.applyBlockEntityRendererRegistrations(event::registerBlockEntityRenderer);
    }
}
