package com.coolerpromc.bettercampfirepot.datagen;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    protected ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        BetterCampfirePot.CAMPFIRE_POTS.forEach(this::dropSelf);
        this.add(BetterCampfirePot.BETTER_CAMPFIRE_BLOCK, this.createSilkTouchDispatchTable(Blocks.CAMPFIRE, this.applyExplosionCondition(Items.CAMPFIRE, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))));
        this.add(BetterCampfirePot.BETTER_SOUL_CAMPFIRE_BLOCK, this.createSilkTouchDispatchTable(Blocks.SOUL_CAMPFIRE, this.applyExplosionCondition(Items.SOUL_CAMPFIRE, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))));
    }
}
