package com.coolerpromc.bettercampfirepot.platform.services;

import net.minecraft.world.item.ItemStack;

public interface IPlatformHelper {
    String getPlatformName();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }
    ItemStack getCraftingRemainingItem(ItemStack stack);
}
