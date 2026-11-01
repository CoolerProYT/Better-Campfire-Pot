package com.coolerpromc.bettercampfirepot.compat;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Optional block IDs are supplied by a data pack tag; no stove mod is required. */
public final class StoveHeatSources {
    public static final TagKey<Block> STOVES = TagKey.create(Registries.BLOCK, BetterCampfirePot.id("stoves"));

    private static final BooleanProperty HAS_POT = BooleanProperty.create("has_pot");

    private StoveHeatSources() {}

    public static boolean isStove(BlockState state) {
        return state.is(STOVES);
    }

    public static boolean hasAttachedPot(BlockState state) {
        return state.hasProperty(HAS_POT) && state.getValue(HAS_POT);
    }

    public static boolean isHot(BlockState state) {
        // CobbleFurnies stoves have no fuel/heat switch and cook without fuel.
        return isStove(state)
                && (!state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT))
                && (!state.hasProperty(BlockStateProperties.WATERLOGGED) || !state.getValue(BlockStateProperties.WATERLOGGED));
    }
}
