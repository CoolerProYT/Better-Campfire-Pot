package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateValidItemPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<UpdateValidItemPacket> TYPE = new Type<>(BetterCampfirePot.id("update_valid_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateValidItemPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateValidItemPacket::pos,
            UpdateValidItemPacket::new
    );

    public void handle(IPayloadContext context){
        context.enqueueWork(() -> {
            BlockEntity entity = context.player().level().getBlockEntity(this.pos);
            if (entity instanceof BetterCampfireBlockEntity blockEntity){
                blockEntity.updateItemBySlot();
                PacketDistributor.sendToPlayer((ServerPlayer) context.player(), new ValidItemSyncPacket(blockEntity.getValidInputItem(), blockEntity.getValidSeasoningItem()));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
