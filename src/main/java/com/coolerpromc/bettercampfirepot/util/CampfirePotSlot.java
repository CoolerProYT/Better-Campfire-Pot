package com.coolerpromc.bettercampfirepot.util;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum CampfirePotSlot {
    INPUT(0xFF1fff26),
    SEASONING(0xFFdbbd0f),
    OUTPUT(0xFFde5d07);

    public static final Codec<CampfirePotSlot> CODEC = Codec.STRING.xmap(CampfirePotSlot::valueOf, CampfirePotSlot::name);
    public static final StreamCodec<RegistryFriendlyByteBuf, CampfirePotSlot> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final int color;

    CampfirePotSlot(int color){
        this.color = color;
    }

    public int color(){
        return color;
    }

    public CampfirePotSlot next(){
        return values()[(ordinal() + 1) % values().length];
    }
}
