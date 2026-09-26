package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
import com.coolerpromc.bettercampfirepot.platform.Services;
import com.coolerpromc.bettercampfirepot.platform.util.FabricServerPayloadContext;
import com.coolerpromc.bettercampfirepot.recipe.BetterCampfirePotRecipe;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;

public class FabricBetterCampfirePot implements ModInitializer {
    @Override
    public void onInitialize() {
        BetterCampfirePot.init();
        BetterCampfirePot.initCapability();
        BetterCampfirePot.initPayloadType();

        Services.CAPABILITIES.applyRegistrations(null);

        Services.REGISTRY.applyServerboundPayloadRegistrations(FabricBetterCampfirePot::registerServerboundPayload);
        Services.REGISTRY.applyClientboundPayloadRegistrations(PayloadTypeRegistry.playS2C()::register);

        ServerLifecycleEvents.SERVER_STARTED.register(BetterCampfirePotRecipe::onServerStarted);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                BetterCampfirePotRecipe.onServerStarted(server);
            }
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> BetterCampfirePot.syncTierSpeeds(player));

        NeoForgeConfigRegistry.INSTANCE.register(BetterCampfirePot.MODID, ModConfig.Type.COMMON, BetterCampfirePotConfig.CONFIG_SPEC);

        ModContainer mod = FabricLoader.getInstance().getModContainer(BetterCampfirePot.MODID).get();
        ResourceLocation id = BetterCampfirePot.id("bettercampfirepotold");
        ResourceManagerHelperImpl.registerBuiltinResourcePack(id, "resourcepacks/" + id.getPath(), mod, Component.literal("Better Campfire Pot v0"), ResourcePackActivationType.NORMAL);
    }

    private static <T extends HandledCustomPacketPayload> void registerServerboundPayload(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        PayloadTypeRegistry.playC2S().register(type, streamCodec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> payload.handle(new FabricServerPayloadContext(context)));
    }
}
