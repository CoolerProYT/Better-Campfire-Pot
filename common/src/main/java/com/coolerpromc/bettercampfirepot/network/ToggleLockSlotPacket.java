package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;

public record ToggleLockSlotPacket(BlockPos pos) implements HandledCustomPacketPayload {
    public static final Type<ToggleLockSlotPacket> TYPE = new Type<>(BetterCampfirePot.id("toggle_lock_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleLockSlotPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ToggleLockSlotPacket::pos,
            ToggleLockSlotPacket::new
    );

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            BetterCampfireBlockEntity blockEntity = CookingPotMenu.getOpenBlockEntity(context.player(), this.pos);
            if (blockEntity != null){
                blockEntity.toggleLockSlot();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}