package com.coolerpromc.bettercampfirepot.datagen.loot;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        BetterCampfirePot.CAMPFIRE_POTS.stream().map(DeferredHolder::get).forEach(this::dropSelf);

        this.add(BetterCampfirePot.BETTER_CAMPFIRE_BLOCK.get(), this.createSilkTouchDispatchTable(Blocks.CAMPFIRE, this.applyExplosionCondition(Items.CAMPFIRE, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))));
        this.add(BetterCampfirePot.BETTER_SOUL_CAMPFIRE_BLOCK.get(), this.createSilkTouchDispatchTable(Blocks.SOUL_CAMPFIRE, this.applyExplosionCondition(Items.SOUL_CAMPFIRE, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK.stream().filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(BetterCampfirePot.MODID)).toList();
    }
}
