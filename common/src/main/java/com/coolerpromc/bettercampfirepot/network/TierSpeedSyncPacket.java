package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.BetterCampfirePotConfig;
import com.coolerpromc.bettercampfirepot.platform.util.PayloadContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;

public record TierSpeedSyncPacket(Map<String, Integer> ticksByTier) implements HandledCustomPacketPayload {
    public static final Type<TierSpeedSyncPacket> TYPE = new Type<>(BetterCampfirePot.id("tier_speed_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TierSpeedSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.<ByteBuf, String, Integer, Map<String, Integer>>map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
            TierSpeedSyncPacket::ticksByTier,
            TierSpeedSyncPacket::new
    );

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> BetterCampfirePotConfig.setSyncedTicks(this.ticksByTier));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
