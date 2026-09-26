package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.platform.Services;
import com.coolerpromc.bettercampfirepot.platform.util.PayloadContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;

public record UpdateValidItemPacket(BlockPos pos) implements HandledCustomPacketPayload {
    public static final Type<UpdateValidItemPacket> TYPE = new Type<>(BetterCampfirePot.id("update_valid_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateValidItemPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            UpdateValidItemPacket::pos,
            UpdateValidItemPacket::new
    );

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            BetterCampfireBlockEntity blockEntity = CookingPotMenu.getOpenBlockEntity(context.player(), this.pos);
            if (blockEntity != null){
                blockEntity.updateItemBySlot();
                Services.NETWORK.sendToPlayer((ServerPlayer) context.player(), new ValidItemSyncPacket(blockEntity.getValidInputItem(), blockEntity.getValidSeasoningItem()));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}