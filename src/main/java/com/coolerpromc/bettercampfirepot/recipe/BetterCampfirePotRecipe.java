package com.coolerpromc.bettercampfirepot.recipe;

import com.cobblemon.mod.common.CobblemonRecipeTypes;
import com.cobblemon.mod.common.item.crafting.CookingPotRecipe;
import com.cobblemon.mod.common.item.crafting.CookingPotShapelessRecipe;
import com.cobblemon.mod.common.item.crafting.SeasoningProcessor;
import com.coolerpromc.bettercampfirepot.util.IngredientKey;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.*;

import static com.cobblemon.mod.common.util.codec.CodecUtils.createByStringCodec;

public record BetterCampfirePotRecipe(List<Pair<Ingredient, Integer>> ingredients, ItemStack result, TagKey<Item> seasoningTag, List<SeasoningProcessor> seasoningProcessors) {
    private static final List<BetterCampfirePotRecipe> RECIPES = new ArrayList<>();

    public static final Codec<Pair<Ingredient, Integer>> INGREDIENT_COUNT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Pair::getFirst),
                    Codec.INT.fieldOf("count").forGetter(Pair::getSecond)
            ).apply(instance, Pair::new)
    );

    public static final Codec<BetterCampfirePotRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            INGREDIENT_COUNT_CODEC.listOf().fieldOf("ingredients").forGetter(BetterCampfirePotRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(BetterCampfirePotRecipe::result),
            TagKey.codec(Registries.ITEM).fieldOf("seasoningTag").forGetter(BetterCampfirePotRecipe::seasoningTag),
            createByStringCodec(s -> SeasoningProcessor.Companion.getProcessors().get(s), SeasoningProcessor::getType, s -> "Unknown seasoning processor: " + s).listOf().fieldOf("seasoningProcessors").forGetter(BetterCampfirePotRecipe::seasoningProcessors)
    ).apply(instance, BetterCampfirePotRecipe::new));

    public void applySeasoning(ItemStack stack, List<ItemStack> seasoning) {
        for (SeasoningProcessor processor : seasoningProcessors) {
            processor.apply(stack, seasoning);
        }
    }

    public boolean matches(List<ItemStack> inputs) {
        List<Pair<Ingredient, Integer>> remaining = new ArrayList<>(this.ingredients);

        for (Pair<Ingredient, Integer> pair : remaining) {
            int neededCount = pair.getSecond();

            for (ItemStack input : inputs) {
                if (input.isEmpty()) continue;

                if (pair.getFirst().test(input)) {

                    neededCount -= input.getCount();
                    if (neededCount <= 0) break;
                }
            }

            if (neededCount > 0) {
                return false;
            }
        }

        return true;
    }

    public static List<BetterCampfirePotRecipe> getRecipes(){
        return RECIPES;
    }

    public static Optional<BetterCampfirePotRecipe> getRecipeFor(List<ItemStack> inputs){
        return RECIPES.stream().filter(recipe -> recipe.matches(inputs)).findFirst();
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    public static void onServerStarted(MinecraftServer server) {
        List<RecipeHolder<CookingPotRecipe>> cookingPotRecipes = server.getRecipeManager().getAllRecipesFor(CobblemonRecipeTypes.INSTANCE.getCOOKING_POT_COOKING());
        List<RecipeHolder<CookingPotShapelessRecipe>> cookingPotShapelessRecipes = server.getRecipeManager().getAllRecipesFor(CobblemonRecipeTypes.INSTANCE.getCOOKING_POT_SHAPELESS());
        RECIPES.clear();

        cookingPotRecipes.forEach(holder -> {
            CookingPotRecipe recipe = holder.value();
            Map<IngredientKey, Integer> ingredients = new HashMap<>();
            for (Ingredient ingredient : recipe.getIngredients()){
                if (ingredient.isEmpty()) continue;
                IngredientKey key = IngredientKey.of(ingredient);
                ingredients.merge(key, 1, Integer::sum);
            }
            List<Pair<Ingredient, Integer>> ing = new ArrayList<>();
            ingredients.forEach((ingredient, integer) -> ing.add(new Pair<>(ingredient.ingredient(), integer)));
            RECIPES.add(new BetterCampfirePotRecipe(ing, recipe.getResultItem(server.registryAccess()), recipe.getSeasoningTag(), recipe.getSeasoningProcessors()));
        });

        cookingPotShapelessRecipes.forEach(holder -> {
            CookingPotShapelessRecipe recipe = holder.value();
            Map<IngredientKey, Integer> ingredients = new HashMap<>();
            for (Ingredient ingredient : recipe.getIngredients()){
                if (ingredient.isEmpty()) continue;
                IngredientKey key = IngredientKey.of(ingredient);
                ingredients.merge(key, 1, Integer::sum);
            }
            List<Pair<Ingredient, Integer>> ing = new ArrayList<>();
            ingredients.forEach((ingredient, integer) -> ing.add(new Pair<>(ingredient.ingredient(), integer)));
            RECIPES.add(new BetterCampfirePotRecipe(ing, recipe.getResultItem(server.registryAccess()), recipe.getSeasoningTag(), recipe.getSeasoningProcessors()));
        });
    }
}
