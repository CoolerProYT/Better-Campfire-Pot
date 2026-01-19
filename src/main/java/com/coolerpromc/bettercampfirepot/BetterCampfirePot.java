package com.coolerpromc.bettercampfirepot;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.block.campfirepot.CampfirePotColor;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.block.BetterCampfirePotBlock;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(BetterCampfirePot.MODID)
public class BetterCampfirePot {
    public static final String MODID = "bettercampfirepot";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);

    public static final DeferredBlock<BetterCampfireBlock> BETTER_CAMPFIRE_BLOCK = BLOCKS.registerBlock("better_campfire_block", properties -> new BetterCampfireBlock(properties.sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel(s -> 14), false));
    public static final DeferredBlock<BetterCampfireBlock> BETTER_SOUL_CAMPFIRE_BLOCK = BLOCKS.registerBlock("better_soul_campfire_block", properties -> new BetterCampfireBlock(properties.sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK).mapColor(MapColor.PODZOL).strength(2.0F).lightLevel(s -> 9), true));
    public static final Supplier<BlockEntityType<BetterCampfireBlockEntity>> BETTER_CAMPFIRE_BE = BLOCK_ENTITIES.register("better_campfire_be", () -> BlockEntityType.Builder.of(BetterCampfireBlockEntity::new, BETTER_CAMPFIRE_BLOCK.get(), BETTER_SOUL_CAMPFIRE_BLOCK.get()).build(null));
    public static final Supplier<MenuType<CookingPotMenu>> BETTER_CAMPFIRE_MENU = MENU_TYPES.register("better_campfire_menu", () -> IMenuTypeExtension.create(CookingPotMenu::new));

    public static List<DeferredBlock<BetterCampfirePotBlock>> CAMPFIRE_POTS = new ArrayList<>();
    public static List<DeferredItem<BetterCampfirePotItem>> CAMPFIRE_POT_ITEMS = new ArrayList<>();

    public static final Supplier<CreativeModeTab> BETTER_CAMPFIRE_POT_TAB = CREATIVE_TABS.register("better_campfire_pot_tab", () -> CreativeModeTab.builder()
            .icon(BuiltInRegistries.ITEM.get(id("copper_red_campfire_pot"))::getDefaultInstance)
            .title(Component.translatable("itemGroup.bettercampfirepot"))
            .displayItems((parameters,output) -> output.acceptAll(CAMPFIRE_POTS.stream().map(DeferredBlock::toStack).toList()))
            .build());

    private static void registerCampfirePots(){
        List<Pair<String, Integer>> tiers = List.of(Pair.of(Tiers.COPPER, 5), Pair.of(Tiers.IRON, 10), Pair.of(Tiers.GOLD, 20), Pair.of(Tiers.DIAMOND, 40), Pair.of(Tiers.EMERALD, 60), Pair.of(Tiers.NETHERITE, 80));

        for (Pair<String, Integer> tier : tiers){
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
                CAMPFIRE_POTS.add(registerBlockWithItem(tier.getFirst(), properties -> new BetterCampfirePotBlock(properties.mapColor(mapColor).requiresCorrectToolForDrops().sound(CobblemonSounds.CAMPFIRE_POT_SOUNDS).strength(0.5F).pushReaction(PushReaction.BLOCK).noOcclusion()), color, tier.getSecond()));
            }
        }
    }

    public BetterCampfirePot(IEventBus modEventBus, ModContainer modContainer) {
        registerCampfirePots();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
    }

    public static DeferredBlock<BetterCampfirePotBlock> registerBlockWithItem(String tier, Function<BlockBehaviour.Properties, BetterCampfirePotBlock> func, CampfirePotColor color, int progressPerTick){
        String name = tier + "_" + color.getSuffix() + "_campfire_pot";
        DeferredBlock<BetterCampfirePotBlock> toReturn = BLOCKS.registerBlock(name, func);
        CAMPFIRE_POT_ITEMS.add(ITEMS.registerItem(name, properties -> new BetterCampfirePotItem(toReturn.get(), tier, color, progressPerTick, properties)));
        return toReturn;
    }

    public static ResourceLocation id(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
