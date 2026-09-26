package com.coolerpromc.bettercampfirepot;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.block.campfirepot.CampfirePotColor;
import com.cobblemon.mod.common.item.CampfirePotItem;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.block.BetterCampfirePotBlock;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.item.TierUpgradeItem;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;
import com.coolerpromc.bettercampfirepot.network.*;
import com.coolerpromc.bettercampfirepot.platform.Services;
import com.coolerpromc.bettercampfirepot.platform.util.RegistryHandler;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class BetterCampfirePot {
    public static final String MODID = "bettercampfirepot";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final RegistryHandler.Blocks<BetterCampfireBlock> BETTER_CAMPFIRE_BLOCK = Services.REGISTRY.registerBlock("better_campfire_block", properties -> new BetterCampfireBlock(properties.sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel(s -> 14), false));
    public static final RegistryHandler.Blocks<BetterCampfireBlock> BETTER_SOUL_CAMPFIRE_BLOCK = Services.REGISTRY.registerBlock("better_soul_campfire_block", properties -> new BetterCampfireBlock(properties.sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel(s -> 9), true));
    public static final RegistryHandler<BlockEntityType<?>, BlockEntityType<BetterCampfireBlockEntity>> BETTER_CAMPFIRE_BE = Services.REGISTRY.registerBlockEntityType("better_campfire_be", BetterCampfireBlockEntity::new, List.of(BETTER_CAMPFIRE_BLOCK, BETTER_SOUL_CAMPFIRE_BLOCK));
    public static final RegistryHandler<MenuType<?>, MenuType<CookingPotMenu>> BETTER_CAMPFIRE_MENU = Services.REGISTRY.registerMenuType("better_campfire_menu", CookingPotMenu::new);

    public static List<RegistryHandler.Blocks<BetterCampfirePotBlock>> CAMPFIRE_POTS = new ArrayList<>();
    public static List<RegistryHandler.Items<BetterCampfirePotItem>> CAMPFIRE_POT_ITEMS = new ArrayList<>();
    public static List<RegistryHandler.Items<TierUpgradeItem>> TIER_UPGRADES = new ArrayList<>();

    public static RegistryHandler<CreativeModeTab, CreativeModeTab> BETTER_CAMPFIRE_POT_TAB;
    public static RegistryHandler<CreativeModeTab, CreativeModeTab> BETTER_CAMPFIRE_POT_UPGRADE_TAB;

    public static void init() {
        registerCampfirePots();
        registerTierUpgrades();

        BETTER_CAMPFIRE_POT_TAB = Services.REGISTRY.registerCreativeTab("better_campfire_pot_tab",
                () -> BuiltInRegistries.ITEM.get(id("netherite_red_campfire_pot")).getDefaultInstance(),
                Component.translatable("itemGroup.bettercampfirepot.pot"),
                (output, parameters) -> output.acceptAll(CAMPFIRE_POTS.stream().map(RegistryHandler.Blocks::toStack).toList()));

        BETTER_CAMPFIRE_POT_UPGRADE_TAB = Services.REGISTRY.registerCreativeTab("better_campfire_pot_upgrade_tab",
                () -> BuiltInRegistries.ITEM.get(id("vanilla_to_netherite_tier_upgrade")).getDefaultInstance(),
                Component.translatable("itemGroup.bettercampfirepot.upgrade"),
                (output, parameters) -> output.acceptAll(TIER_UPGRADES.stream().map(RegistryHandler.Items::toStack).toList()));
    }

    public static void initCapability() {
        Services.CAPABILITIES.registerBlockEntityItemStorage(BETTER_CAMPFIRE_BE, BetterCampfireBlockEntity::getContainer, BetterCampfireBlockEntity::getContainers, BetterCampfireBlockEntity::shouldDistributeEvenly);
    }

    public static void initPayloadType() {
        Services.REGISTRY.registerServerboundPayload(ToggleCookingPotLidPacket.TYPE, ToggleCookingPotLidPacket.STREAM_CODEC);
        Services.REGISTRY.registerServerboundPayload(CapabilityChangeSyncC2SPacket.TYPE, CapabilityChangeSyncC2SPacket.STREAM_CODEC);
        Services.REGISTRY.registerServerboundPayload(ToggleLockSlotPacket.TYPE, ToggleLockSlotPacket.STREAM_CODEC);
        Services.REGISTRY.registerServerboundPayload(UpdateValidItemPacket.TYPE, UpdateValidItemPacket.STREAM_CODEC);
        Services.REGISTRY.registerClientboundPayload(ValidItemSyncPacket.TYPE, ValidItemSyncPacket.STREAM_CODEC);
        Services.REGISTRY.registerClientboundPayload(TierSpeedSyncPacket.TYPE, TierSpeedSyncPacket.STREAM_CODEC);
    }

    public static void syncTierSpeeds(ServerPlayer player) {
        Services.NETWORK.sendToPlayer(player, new TierSpeedSyncPacket(BetterCampfirePotConfig.CONFIG.getTicksByTier()));
    }

    private static void registerCampfirePots(){
        List<String> tiers = new ArrayList<>(Tiers.TIERS);
        tiers.removeFirst();

        for (String tier : tiers){
            for (CampfirePotColor color : CampfirePotColor.getEntries()){
                MapColor mapColor = switch (color){
                    case RED -> MapColor.COLOR_RED;
                    case BLACK -> MapColor.COLOR_BLACK;
                    case YELLOW -> MapColor.COLOR_YELLOW;
                    case GREEN -> MapColor.COLOR_GREEN;
                    case BLUE -> MapColor.COLOR_BLUE;
                    case WHITE -> MapColor.COLOR_LIGHT_GRAY;
                    case PINK -> MapColor.COLOR_PINK;
                };
                CAMPFIRE_POTS.add(registerBlockWithItem(tier, properties -> new BetterCampfirePotBlock(properties.mapColor(mapColor).requiresCorrectToolForDrops().sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS).strength(0.5F).pushReaction(PushReaction.BLOCK).noOcclusion()), color));
            }
        }
    }

    private static void registerTierUpgrades(){
        List<Pair<String, String>> tiers = List.of(
                Pair.of(Tiers.VANILLA, Tiers.COPPER),
                Pair.of(Tiers.VANILLA, Tiers.IRON),
                Pair.of(Tiers.VANILLA, Tiers.GOLD),
                Pair.of(Tiers.VANILLA, Tiers.DIAMOND),
                Pair.of(Tiers.VANILLA, Tiers.EMERALD),
                Pair.of(Tiers.VANILLA, Tiers.NETHERITE),
                Pair.of(Tiers.COPPER, Tiers.IRON),
                Pair.of(Tiers.COPPER, Tiers.GOLD),
                Pair.of(Tiers.COPPER, Tiers.DIAMOND),
                Pair.of(Tiers.COPPER, Tiers.EMERALD),
                Pair.of(Tiers.COPPER, Tiers.NETHERITE),
                Pair.of(Tiers.IRON, Tiers.GOLD),
                Pair.of(Tiers.IRON, Tiers.DIAMOND),
                Pair.of(Tiers.IRON, Tiers.EMERALD),
                Pair.of(Tiers.IRON, Tiers.NETHERITE),
                Pair.of(Tiers.GOLD, Tiers.DIAMOND),
                Pair.of(Tiers.GOLD, Tiers.EMERALD),
                Pair.of(Tiers.GOLD, Tiers.NETHERITE),
                Pair.of(Tiers.DIAMOND, Tiers.EMERALD),
                Pair.of(Tiers.DIAMOND, Tiers.NETHERITE),
                Pair.of(Tiers.EMERALD, Tiers.NETHERITE)
        );

        for (Pair<String, String> tier : tiers){
            TIER_UPGRADES.add(Services.REGISTRY.registerItem(tier.getFirst() + "_to_" + tier.getSecond() + "_tier_upgrade", properties -> new TierUpgradeItem(tier.getFirst(), tier.getSecond(), properties)));
        }
    }

    public static RegistryHandler.Blocks<BetterCampfirePotBlock> registerBlockWithItem(String tier, Function<BlockBehaviour.Properties, BetterCampfirePotBlock> func, CampfirePotColor color){
        String name = tier + "_" + color.getSuffix() + "_campfire_pot";
        RegistryHandler.Blocks<BetterCampfirePotBlock> toReturn = Services.REGISTRY.registerBlock(name, func);
        CAMPFIRE_POT_ITEMS.add(Services.REGISTRY.registerItem(name, properties -> new BetterCampfirePotItem(toReturn.get(), tier, color, properties)));
        return toReturn;
    }

    public static ResourceLocation id(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static Component tierName(String tier){
        return Component.translatable("tier.bettercampfirepot." + tier);
    }

    public static Optional<CampfirePotItem> findVanillaPotByColor(BetterCampfirePotItem item){
        return CobblemonItems.INSTANCE.getCampfire_pots().stream().filter(cobblemonItem -> cobblemonItem.getColor().equals(item.color)).findFirst();
    }

    public static Optional<BetterCampfirePotItem> findBetterPotByTierAndColor(BetterCampfirePotItem item, String tier){
        return findBetterPotByTierAndColor(item.color, tier);
    }

    public static Optional<BetterCampfirePotItem> findBetterPotByTierAndColor(CampfirePotColor color, String tier){
        return BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().map(RegistryHandler.Items::get).filter(betterItem -> betterItem.color.equals(color) && Objects.equals(betterItem.tier, tier)).findFirst();
    }

    public static boolean filterCopperTier(RegistryHandler.Items<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.COPPER);
    }

    public static boolean filterIronTier(RegistryHandler.Items<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.IRON);
    }

    public static boolean filterGoldTier(RegistryHandler.Items<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.GOLD);
    }

    public static boolean filterDiamondTier(RegistryHandler.Items<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.DIAMOND);
    }

    public static boolean filterEmeraldTier(RegistryHandler.Items<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.EMERALD);
    }

    public static boolean filterNetheriteTier(RegistryHandler.Items<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.NETHERITE);
    }
}
