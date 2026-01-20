package com.coolerpromc.bettercampfirepot.datagen;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).addAll(BetterCampfirePot.CAMPFIRE_POTS.stream().map(Block::builtInRegistryHolder).map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).toList());

        tag(BlockTags.NEEDS_STONE_TOOL).addAll(BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::needStoneTool).map(BlockItem::getBlock).map(Block::builtInRegistryHolder).map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).toList());
        tag(BlockTags.NEEDS_IRON_TOOL).addAll(BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::needIronTool).map(BlockItem::getBlock).map(Block::builtInRegistryHolder).map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).toList());
        tag(BlockTags.NEEDS_DIAMOND_TOOL).addAll(BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::needDiamondTool).map(BlockItem::getBlock).map(Block::builtInRegistryHolder).map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get).toList());
    }

    private boolean needStoneTool(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.COPPER) || Objects.equals(item.tier, Tiers.IRON);
    }

    private boolean needIronTool(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.GOLD) || Objects.equals(item.tier, Tiers.DIAMOND)  || Objects.equals(item.tier, Tiers.EMERALD);
    }

    private boolean needDiamondTool(BetterCampfirePotItem item){
        return Objects.equals(item.tier, Tiers.NETHERITE);
    }
}
