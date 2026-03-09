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
import com.coolerpromc.bettercampfirepot.recipe.BetterCampfirePotRecipe;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import com.mojang.datafixers.util.Pair;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class BetterCampfirePot implements ModInitializer {
	public static final String MODID = "bettercampfirepot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final BetterCampfireBlock BETTER_CAMPFIRE_BLOCK = Registry.register(BuiltInRegistries.BLOCK, id("better_campfire_block"), new BetterCampfireBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel(s -> 14), false));
    public static final BetterCampfireBlock BETTER_SOUL_CAMPFIRE_BLOCK = Registry.register(BuiltInRegistries.BLOCK, id("better_soul_campfire_block"), new BetterCampfireBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel(s -> 9), true));
    public static final BlockEntityType<BetterCampfireBlockEntity> BETTER_CAMPFIRE_BE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("better_campfire_be"), BlockEntityType.Builder.of(BetterCampfireBlockEntity::new, BETTER_CAMPFIRE_BLOCK, BETTER_SOUL_CAMPFIRE_BLOCK).build(null));
    public static final MenuType<CookingPotMenu> BETTER_CAMPFIRE_MENU = Registry.register(BuiltInRegistries.MENU, id("better_campfire_menu"), new ExtendedScreenHandlerType<>(CookingPotMenu::new, BlockPos.STREAM_CODEC));

    public static List<BetterCampfirePotBlock> CAMPFIRE_POTS = new ArrayList<>();
    public static List<BetterCampfirePotItem> CAMPFIRE_POT_ITEMS = new ArrayList<>();
    public static List<TierUpgradeItem> TIER_UPGRADES = new ArrayList<>();

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
            TIER_UPGRADES.add(Registry.register(BuiltInRegistries.ITEM, id(tier.getFirst() + "_to_" + tier.getSecond() + "_tier_upgrade"), new TierUpgradeItem(tier.getFirst(), tier.getSecond(), new Item.Properties())));
        }
    }

	@Override
	public void onInitialize() {
        registerCampfirePots();
        registerTierUpgrades();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("better_campfire_pot_tab"), FabricItemGroup.builder()
                .icon(BuiltInRegistries.ITEM.get(id("netherite_red_campfire_pot"))::getDefaultInstance)
                .title(Component.translatable("itemGroup.bettercampfirepot.pot"))
                .displayItems((parameters,output) -> output.acceptAll(CAMPFIRE_POT_ITEMS.stream().map(ItemStack::new).toList()))
                .build());

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("better_campfire_pot_upgrade_tab"), FabricItemGroup.builder()
                .icon(BuiltInRegistries.ITEM.get(id("vanilla_to_netherite_tier_upgrade"))::getDefaultInstance)
                .title(Component.translatable("itemGroup.bettercampfirepot.upgrade"))
                .displayItems((parameters,output) -> output.acceptAll(TIER_UPGRADES.stream().map(ItemStack::new).toList()))
                .build());

        ItemStorage.SIDED.registerForBlockEntity(BetterCampfireBlockEntity::getCapability, BetterCampfirePot.BETTER_CAMPFIRE_BE);

        PayloadTypeRegistry.playC2S().register(ToggleCookingPotLidPacket.TYPE, ToggleCookingPotLidPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CapabilityChangeSyncC2SPacket.TYPE, CapabilityChangeSyncC2SPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleLockSlotPacket.TYPE, ToggleLockSlotPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateValidItemPacket.TYPE, UpdateValidItemPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ValidItemSyncPacket.TYPE, ValidItemSyncPacket.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ToggleCookingPotLidPacket.TYPE, ToggleCookingPotLidPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(CapabilityChangeSyncC2SPacket.TYPE, CapabilityChangeSyncC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ToggleLockSlotPacket.TYPE, ToggleLockSlotPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(UpdateValidItemPacket.TYPE, UpdateValidItemPacket::handle);
        ServerLifecycleEvents.SERVER_STARTED.register(BetterCampfirePotRecipe::onServerStarted);

        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, BetterCampfirePotConfig.CONFIG_SPEC);
	}

    public static BetterCampfirePotBlock registerBlockWithItem(String tier, Function<BlockBehaviour.Properties, BetterCampfirePotBlock> func, CampfirePotColor color){
        String name = tier + "_" + color.getSuffix() + "_campfire_pot";
        BetterCampfirePotBlock toReturn = Registry.register(BuiltInRegistries.BLOCK, id(name), func.apply(BlockBehaviour.Properties.of()));
        CAMPFIRE_POT_ITEMS.add(Registry.register(BuiltInRegistries.ITEM, id(name), new BetterCampfirePotItem(toReturn, tier, color, new Item.Properties())));
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
        return BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(betterItem -> betterItem.color.equals(color) && Objects.equals(betterItem.tier, tier)).findFirst();
    }

    public static boolean filterCopperTier(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.COPPER);
    }

    public static boolean filterIronTier(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.IRON);
    }

    public static boolean filterGoldTier(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.GOLD);
    }

    public static boolean filterDiamondTier(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.DIAMOND);
    }

    public static boolean filterEmeraldTier(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.EMERALD);
    }

    public static boolean filterNetheriteTier(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.NETHERITE);
    }
}