package com.coolerpromc.bettercampfirepot.datagen;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BetterCampfirePot.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).addAll(BetterCampfirePot.CAMPFIRE_POTS.stream().map(DeferredHolder::getKey).toList());

        tag(BlockTags.NEEDS_STONE_TOOL).addAll(BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::needStoneTool).map(DeferredItem::get).map(BlockItem::getBlock).map(Block::builtInRegistryHolder).map(Holder::getKey).toList());
        tag(BlockTags.NEEDS_IRON_TOOL).addAll(BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::needIronTool).map(DeferredItem::get).map(BlockItem::getBlock).map(Block::builtInRegistryHolder).map(Holder::getKey).toList());
        tag(BlockTags.NEEDS_DIAMOND_TOOL).addAll(BetterCampfirePot.CAMPFIRE_POT_ITEMS.stream().filter(this::needDiamondTool).map(DeferredItem::get).map(BlockItem::getBlock).map(Block::builtInRegistryHolder).map(Holder::getKey).toList());
    }

    private boolean needStoneTool(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.COPPER) || Objects.equals(item.get().tier, Tiers.IRON);
    }

    private boolean needIronTool(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.GOLD) || Objects.equals(item.get().tier, Tiers.DIAMOND)  || Objects.equals(item.get().tier, Tiers.EMERALD);
    }

    private boolean needDiamondTool(DeferredItem<BetterCampfirePotItem> item){
        return Objects.equals(item.get().tier, Tiers.NETHERITE);
    }
}
