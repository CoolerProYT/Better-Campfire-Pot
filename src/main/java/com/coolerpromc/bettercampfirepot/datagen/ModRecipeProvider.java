package com.coolerpromc.bettercampfirepot.datagen;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.item.CampfirePotItem;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        // Vanilla to Copper
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::filterCopperTier).map(DeferredHolder::get).forEach(item -> {
            Optional<CampfirePotItem> campfirePotItem = findVanillaPotByColor(item);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.COPPER_INGOT, recipeOutput));
        });

        // Copper to Iron
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::filterIronTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = findBetterPotByTierAndColor(item, Tiers.COPPER);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.IRON_INGOT, recipeOutput));
        });

        // Iron to Gold
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::filterGoldTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = findBetterPotByTierAndColor(item, Tiers.IRON);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.GOLD_INGOT, recipeOutput));
        });

        // Gold to Diamond
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::filterDiamondTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = findBetterPotByTierAndColor(item, Tiers.GOLD);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.DIAMOND, recipeOutput));
        });

        // Diamond to Emerald
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::filterEmeraldTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = findBetterPotByTierAndColor(item, Tiers.DIAMOND);
            campfirePotItem.ifPresent(potItem -> upgradeCraftingRecipe(potItem, item, Items.EMERALD, recipeOutput));
        });

        // Emerald to Netherite
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::filterNetheriteTier).map(DeferredHolder::get).forEach(item -> {
            Optional<BetterCampfirePotItem> campfirePotItem = findBetterPotByTierAndColor(item, Tiers.EMERALD);
            campfirePotItem.ifPresent(potItem -> upgradeSmithingRecipe(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, potItem, item, Items.NETHERITE_INGOT, recipeOutput));
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

    private Optional<CampfirePotItem> findVanillaPotByColor(BetterCampfirePotItem item){
        return CobblemonItems.INSTANCE.getCampfire_pots().stream().filter(cobblemonItem -> cobblemonItem.getColor().equals(item.color)).findFirst();
    }

    private Optional<BetterCampfirePotItem> findBetterPotByTierAndColor(BetterCampfirePotItem item, String tier){
        return BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().map(DeferredHolder::get).filter(betterItem -> betterItem.color.equals(item.color) && Objects.equals(betterItem.tier, tier)).findFirst();
    }

    private boolean filterCopperTier(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.COPPER);
    }

    private boolean filterIronTier(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.IRON);
    }

    private boolean filterGoldTier(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.GOLD);
    }

    private boolean filterDiamondTier(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.DIAMOND);
    }

    private boolean filterEmeraldTier(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.EMERALD);
    }

    private boolean filterNetheriteTier(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.NETHERITE);
    }
}
