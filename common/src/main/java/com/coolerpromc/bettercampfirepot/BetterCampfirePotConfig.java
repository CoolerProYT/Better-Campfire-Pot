package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.util.Tiers;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class BetterCampfirePotConfig {
    public static final BetterCampfirePotConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    private static volatile Map<String, Integer> syncedTicks = null;

    public final ModConfigSpec.IntValue copper;
    public final ModConfigSpec.IntValue iron;
    public final ModConfigSpec.IntValue gold;
    public final ModConfigSpec.IntValue diamond;
    public final ModConfigSpec.IntValue emerald;
    public final ModConfigSpec.IntValue netherite;
    public final ModConfigSpec.BooleanValue autoOutput;

    static {
        Pair<BetterCampfirePotConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(BetterCampfirePotConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    private BetterCampfirePotConfig(ModConfigSpec.Builder builder) {
        builder.push("Better Campfire Pot Speed");
        copper = builder.comment("Speed of copper tier campfire pot per tick").defineInRange("copper", 5, 1, 200);
        iron = builder.comment("Speed of iron tier campfire pot per tick").defineInRange("iron", 10, 1, 200);
        gold = builder.comment("Speed of gold tier campfire pot per tick").defineInRange("gold", 20, 1, 200);
        diamond = builder.comment("Speed of diamond tier campfire pot per tick").defineInRange("diamond", 40, 1, 200);
        emerald = builder.comment("Speed of emerald tier campfire pot per tick").defineInRange("emerald", 60, 1, 200);
        netherite = builder.comment("Speed of netherite tier campfire pot per tick").defineInRange("netherite", 80, 1, 200);
        builder.pop();

        builder.push("Better Campfire Pot Automation");
        autoOutput = builder.comment("Push finished dishes into the container on the pot's item-facing side, the same side leftover items go to").define("autoOutput", false);
        builder.pop();
    }

    public int getTickByTier(String tier){
        return switch (tier){
            case Tiers.COPPER -> copper.get();
            case Tiers.IRON -> iron.get();
            case Tiers.GOLD -> gold.get();
            case Tiers.DIAMOND -> diamond.get();
            case Tiers.EMERALD -> emerald.get();
            case Tiers.NETHERITE -> netherite.get();
            default -> 2;
        };
    }

    public int getDisplayTickByTier(String tier){
        Map<String, Integer> synced = syncedTicks;
        if (synced != null && synced.containsKey(tier)) {
            return synced.get(tier);
        }
        return getTickByTier(tier);
    }

    public Map<String, Integer> getTicksByTier(){
        Map<String, Integer> ticks = new HashMap<>();
        for (String tier : Tiers.TIERS) {
            ticks.put(tier, getTickByTier(tier));
        }
        return ticks;
    }

    public static void setSyncedTicks(Map<String, Integer> ticks){
        syncedTicks = Map.copyOf(ticks);
    }
}
