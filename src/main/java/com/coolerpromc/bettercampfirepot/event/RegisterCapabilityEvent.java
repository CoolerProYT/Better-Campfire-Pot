package com.coolerpromc.bettercampfirepot.event;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = BetterCampfirePot.MODID)
public class RegisterCapabilityEvent {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BetterCampfirePot.BETTER_CAMPFIRE_BE.get(), BetterCampfireBlockEntity::getCapability);
    }
}
