package com.coolerpromc.bettercampfirepot.item;

import net.minecraft.world.item.Item;

public class TierUpgradeItem extends Item {
    public final String fromTier;
    public final String toTier;

    public TierUpgradeItem(String fromTier, String toTier, Properties properties) {
        super(properties);
        this.fromTier = fromTier;
        this.toTier = toTier;
    }
}
