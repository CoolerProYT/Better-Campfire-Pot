package com.coolerpromc.bettercampfirepot.datagen;

import com.cobblemon.mod.common.item.CampfirePotItem;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.coolerpromc.bettercampfirepot.util.Tiers.*;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        // Vanilla to Copper
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(BetterCampfirePot::filterCopperTier).map(DeferredHolder::get).forEach(item -> {
            Optional<CampfirePotItem> campfirePotItem = BetterCampfirePot.findVanillaPotByColor(item);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.COPPER_INGOT, recipeOutput));
        });

        // Copper to Iron
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(BetterCampfirePot::filterIronTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = BetterCampfirePot.findBetterPotByTierAndColor(item, COPPER);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.IRON_INGOT, recipeOutput));
        });

        // Iron to Gold
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(BetterCampfirePot::filterGoldTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = BetterCampfirePot.findBetterPotByTierAndColor(item, IRON);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.GOLD_INGOT, recipeOutput));
        });

        // Gold to Diamond
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(BetterCampfirePot::filterDiamondTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = BetterCampfirePot.findBetterPotByTierAndColor(item, GOLD);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.DIAMOND, recipeOutput));
        });

        // Diamond to Emerald
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(BetterCampfirePot::filterEmeraldTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = BetterCampfirePot.findBetterPotByTierAndColor(item, DIAMOND);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.EMERALD, recipeOutput));
        });

        // Emerald to Netherite
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(BetterCampfirePot::filterNetheriteTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = BetterCampfirePot.findBetterPotByTierAndColor(item, EMERALD);
            campfirePotItem.ifPresent(potItem -> upgradeSmithingRecipe(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, potItem, item, Items.NETHERITE_INGOT, recipeOutput));
        });

        BetterCampfirePot.TIER_UPGRADES.stream().map(DeferredHolder::get).forEach(item -> {
            Item from;
            Item to = BuiltInRegistries.ITEM.get(BetterCampfirePot.id(item.fromTier + "_to_" + item.toTier + "_tier_upgrade"));
            String prevToTier = Tiers.getPreviousTier(item.toTier);

            if (prevToTier.equals(item.fromTier)) {
                from = Items.REDSTONE_TORCH;
            } else {
                from = BuiltInRegistries.ITEM.get(BetterCampfirePot.id(item.fromTier + "_to_" + prevToTier + "_tier_upgrade"));
            }

            Item ingredient = switch (item.toTier) {
                case COPPER -> Items.COPPER_INGOT;
                case IRON -> Items.IRON_INGOT;
                case GOLD -> Items.GOLD_INGOT;
                case DIAMOND -> Items.DIAMOND;
                case EMERALD -> Items.EMERALD;
                case NETHERITE -> Items.NETHERITE_INGOT;
                default -> throw new IllegalStateException("Unexpected tier: " + item.toTier);
            };

            if (!item.toTier.equals(Tiers.NETHERITE)) {
                upgradeCraftingRecipe(from, to, ingredient, recipeOutput);
            } else {
                upgradeSmithingRecipe(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, from, to, ingredient, recipeOutput);
            }
        });
    }

    private void upgradeCraftingRecipe(Item from, Item to, Item ingredient, RecipeOutput recipeOutput){
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, to)
                .pattern("I I")
                .pattern("IFI")
                .pattern("III")
                .define('I', ingredient)
                .define('F', from)
                .unlockedBy(getHasName(from), has(from))
                .unlockedBy(getHasName(ingredient), has(ingredient))
                .save(recipeOutput);
    }

    private void upgradeSmithingRecipe(Item template, Item from, Item to, Item ingredient, RecipeOutput recipeOutput){
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(template), Ingredient.of(from), Ingredient.of(ingredient), RecipeCategory.MISC, to)
                .unlocks(getHasName(from), has(from))
                .save(recipeOutput, BetterCampfirePot.id(getItemName(from) + "_to_" + getItemName(to) + "_smithing"));
    }
}
