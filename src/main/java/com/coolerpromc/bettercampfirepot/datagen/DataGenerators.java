package com.coolerpromc.bettercampfirepot.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGenerators implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack packOutput = generator.createPack();

        packOutput.addProvider(ModLootTableProvider::new);
        packOutput.addProvider(ModRecipeProvider::new);
        packOutput.addProvider(ModBlockTagProvider::new);
    }
}
