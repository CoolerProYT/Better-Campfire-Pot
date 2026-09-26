package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.datagen.DataGenerators;
import com.coolerpromc.bettercampfirepot.network.HandledCustomPacketPayload;
import com.coolerpromc.bettercampfirepot.platform.NeoForgeRegistryHelper;
import com.coolerpromc.bettercampfirepot.platform.Services;
import com.coolerpromc.bettercampfirepot.platform.util.NeoForgePayloadContext;
import com.coolerpromc.bettercampfirepot.recipe.BetterCampfirePotRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.nio.file.Path;
import java.util.Optional;

@Mod(BetterCampfirePot.MODID)
public class NeoForgeBetterCampfirePot {
    public NeoForgeBetterCampfirePot(IEventBus eventBus, ModContainer modContainer) {
        BetterCampfirePot.init();
        NeoForgeRegistryHelper.register(eventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, BetterCampfirePotConfig.CONFIG_SPEC);

        eventBus.addListener(NeoForgeBetterCampfirePot::onRegisterCapabilities);
        eventBus.addListener(NeoForgeBetterCampfirePot::onRegisterPayloadHandlers);
        eventBus.addListener(NeoForgeBetterCampfirePot::onAddPackFinders);
        eventBus.addListener(DataGenerators::onGatherData);
        NeoForge.EVENT_BUS.addListener(NeoForgeBetterCampfirePot::onServerStarted);
        NeoForge.EVENT_BUS.addListener(NeoForgeBetterCampfirePot::onDatapackSync);
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            BetterCampfirePotRecipe.onServerStarted(event.getPlayerList().getServer());
        }
        event.getRelevantPlayers().forEach(BetterCampfirePot::syncTierSpeeds);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        BetterCampfirePot.initCapability();
        Services.CAPABILITIES.applyRegistrations(event);
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        BetterCampfirePot.initPayloadType();
        NeoForgePayloadRegistrar registrar = new NeoForgePayloadRegistrar(event.registrar("1"));
        Services.REGISTRY.applyServerboundPayloadRegistrations(registrar::registerServerbound);
        Services.REGISTRY.applyClientboundPayloadRegistrations(registrar::registerClientbound);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        BetterCampfirePotRecipe.onServerStarted(event.getServer());
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }

        IModInfo modFile = ModList.get().getModContainerById(BetterCampfirePot.MODID).get().getModInfo();
        ResourceLocation packLocation = BetterCampfirePot.id("resourcepacks/bettercampfirepotold");
        Path resourcePath = modFile.getOwningFile().getFile().findResource(packLocation.getPath());

        ArtifactVersion version = modFile.getVersion();

        Pack pack = Pack.readMetaAndCreate(new PackLocationInfo("mod/" + packLocation, Component.literal("Better Campfire Pot v0"), PackSource.BUILT_IN, Optional.of(new KnownPack("neoforge", "mod/" + packLocation, version.toString()))),
                BuiltInPackSource.fromName(info -> new PathPackResources(info, resourcePath)), PackType.CLIENT_RESOURCES, new PackSelectionConfig(false, Pack.Position.TOP, false));

        if (pack != null){
            event.addRepositorySource(c -> c.accept(pack));
        }
    }

    private record NeoForgePayloadRegistrar(PayloadRegistrar registrar) {
        private <T extends HandledCustomPacketPayload> void registerServerbound(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            registrar.playToServer(type, streamCodec, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        }

        private <T extends HandledCustomPacketPayload> void registerClientbound(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            registrar.playToClient(type, streamCodec, (payload, context) -> payload.handle(new NeoForgePayloadContext(context)));
        }
    }
}
