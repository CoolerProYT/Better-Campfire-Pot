package com.coolerpromc.bettercampfirepot.datagen;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.BetterCampfirePotBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BetterCampfirePot.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        BetterCampfirePot.CAMPFIRE_POTS.forEach(this::registerBetterCampfirePotModel);
    }

    private void registerBetterCampfirePotModel(DeferredBlock<BetterCampfirePotBlock> block){
        ModelFile campfirePot = models().getBuilder(block.getRegisteredName())
                .parent(new ModelFile.UncheckedModelFile("cobblemon:block/campfire_pot"))
                .texture("pot", block.getId().withPrefix("block/"))
                .texture("particle", block.getId().withPrefix("block/"));

        ModelFile campfirePotBroth = models().getBuilder(block.getRegisteredName() + "_broth")
                .parent(new ModelFile.UncheckedModelFile("cobblemon:block/campfire_pot_broth"))
                .texture("pot", block.getId().withPrefix("block/"))
                .texture("particle", block.getId().withPrefix("block/"));

        ModelFile campfirePotOpen = models().getBuilder(block.getRegisteredName() + "_open")
                .parent(new ModelFile.UncheckedModelFile("cobblemon:block/campfire_pot_open"))
                .texture("pot", block.getId().withPrefix("block/"))
                .texture("particle", block.getId().withPrefix("block/"));

        ModelFile campfirePotOpenBroth = models().getBuilder(block.getRegisteredName() + "_open_broth")
                .parent(new ModelFile.UncheckedModelFile("cobblemon:block/campfire_pot_open_broth"))
                .texture("pot", block.getId().withPrefix("block/"))
                .texture("particle", block.getId().withPrefix("block/"));

        getVariantBuilder(block.get())
                .forAllStatesExcept(state -> {
                    Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
                    boolean open = state.getValue(BetterCampfirePotBlock.OPEN);
                    boolean occupied = state.getValue(BetterCampfirePotBlock.OCCUPIED);

                    int yRot = switch (facing) {
                        case WEST  -> 90;
                        case NORTH -> 180;
                        case EAST  -> 270;
                        default    -> 0;
                    };

                    ModelFile model;
                    if (open && occupied) {
                        model = campfirePotOpenBroth;
                    } else if (open) {
                        model = campfirePotOpen;
                    } else if (occupied) {
                        model = campfirePotBroth;
                    } else {
                        model = campfirePot;
                    }

                    return ConfiguredModel.builder().modelFile(model).rotationY(yRot).build();
                }, BlockStateProperties.WATERLOGGED);
    }
}
