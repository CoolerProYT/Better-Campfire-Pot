package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = BetterCampfirePot.MODID)
public class BetterCampfirePotNetwork {
    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToServer(ToggleCookingPotLidPacket.TYPE, ToggleCookingPotLidPacket.STREAM_CODEC, ToggleCookingPotLidPacket::handle);
    }
}
