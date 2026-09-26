package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.util.CampfirePotSlot;
import com.coolerpromc.bettercampfirepot.util.Side;
import com.coolerpromc.bettercampfirepot.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;

public record CapabilityChangeSyncC2SPacket(BlockPos pos, Side side, CampfirePotSlot slot) implements HandledCustomPacketPayload {
    public static final Type<CapabilityChangeSyncC2SPacket> TYPE = new Type<>(BetterCampfirePot.id("capability_change_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CapabilityChangeSyncC2SPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            CapabilityChangeSyncC2SPacket::pos,
            Side.STREAM_CODEC,
            CapabilityChangeSyncC2SPacket::side,
            CampfirePotSlot.STREAM_CODEC,
            CapabilityChangeSyncC2SPacket::slot,
            CapabilityChangeSyncC2SPacket::new
    );

    @Override
    public void handle(PayloadContext context){
        BetterCampfireBlockEntity be = CookingPotMenu.getOpenBlockEntity(context.player(), this.pos);
        if (be != null){
            be.setCapabilityBySide(this.side, this.slot);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}