package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.platform.services.client.IRegistryHelper;

import java.util.ServiceLoader;

public class ServicesClient {
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, ServicesClient.class.getClassLoader()).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        BetterCampfirePot.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
