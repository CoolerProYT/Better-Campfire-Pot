package com.coolerpromc.bettercampfirepot.datagen;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BetterCampfirePot.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        BetterCampfirePot.CAMPFIRE_POT_ITEMS.forEach(item -> {
            getBuilder(item.getRegisteredName())
                    .parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
                    .texture("layer0", modLoc("item/campfire_pot/campfire_pot_" + item.get().color.getSuffix()))
                    .texture("layer1", modLoc("item/tier/" + item.get().tier));
        });

        BetterCampfirePot.TIER_UPGRADES.forEach(item -> {
            ItemModelBuilder builder = getBuilder(item.getRegisteredName()).parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
                    .texture("layer0", modLoc("item/campfire_pot/campfire_pot_red"));

            String nextLayer = "layer1";

            if (!item.get().fromTier.equals(Tiers.VANILLA)){
                builder.texture(nextLayer, modLoc("item/tier/" + item.get().fromTier));
                nextLayer = "layer2";
            }

            builder.texture(nextLayer, modLoc("item/upgrade_arrow/" + item.get().toTier));
        });
    }
}
