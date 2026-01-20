package com.coolerpromc.bettercampfirepot.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public record IngredientKey(List<Item> items, Ingredient ingredient) {
    public static IngredientKey of(Ingredient ingredient) {
        return new IngredientKey(
                Arrays.stream(ingredient.getItems())
                        .map(ItemStack::getItem)
                        .distinct()
                        .sorted(Comparator.comparing(BuiltInRegistries.ITEM::getKey))
                        .toList(), ingredient
        );
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IngredientKey(List<Item> items1, Ingredient ingredient) && items.equals(items1);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }
}
