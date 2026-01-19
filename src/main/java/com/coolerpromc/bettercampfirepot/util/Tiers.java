package com.coolerpromc.bettercampfirepot.util;

import java.util.List;

public class Tiers {
    public static final String VANILLA = "vanilla";
    public static final String COPPER = "copper";
    public static final String IRON = "iron";
    public static final String GOLD = "gold";
    public static final String DIAMOND = "diamond";
    public static final String EMERALD = "emerald";
    public static final String NETHERITE = "netherite";

    private static final List<String> ORDER = List.of(VANILLA, COPPER, IRON, GOLD, DIAMOND, EMERALD, NETHERITE);

    public static String getPreviousTier(String tier) {
        int index = ORDER.indexOf(tier);
        if (index <= 0) {
            throw new IllegalArgumentException("No previous tier for " + tier);
        }
        return ORDER.get(index - 1);
    }
}
