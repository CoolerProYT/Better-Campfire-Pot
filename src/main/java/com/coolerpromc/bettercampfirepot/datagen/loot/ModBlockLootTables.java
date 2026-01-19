package com.coolerpromc.bettercampfirepot.datagen.loot;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        BetterCampfirePot.CAMPFIRE_POTS.stream().map(DeferredHolder::get).forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BetterCampfirePot.CAMPFIRE_POTS.stream().map(DeferredHolder::get).map(Block.class::cast).toList();
    }
}
