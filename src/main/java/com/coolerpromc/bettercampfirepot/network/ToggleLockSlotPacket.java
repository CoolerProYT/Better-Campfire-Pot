package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleLockSlotPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleLockSlotPacket> TYPE = new Type<>(BetterCampfirePot.id("toggle_lock_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleLockSlotPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ToggleLockSlotPacket::pos,
            ToggleLockSlotPacket::new
    );

    public void handle(IPayloadContext context){
        context.enqueueWork(() -> {
            BlockEntity entity = context.player().level().getBlockEntity(this.pos);
            if (entity instanceof BetterCampfireBlockEntity blockEntity){
                blockEntity.toggleLockSlot();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
