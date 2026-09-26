package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.menu.CookingPotScreen;
import com.coolerpromc.bettercampfirepot.platform.util.PayloadContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;

public record ValidItemSyncPacket(NonNullList<Item> validInputItem, NonNullList<Item> validSeasoningItem) implements HandledCustomPacketPayload {
    public static final Type<ValidItemSyncPacket> TYPE = new Type<>(BetterCampfirePot.id("valid_item_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ValidItemSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(NonNullList::createWithCapacity, ByteBufCodecs.registry(Registries.ITEM)),
            ValidItemSyncPacket::validInputItem,
            ByteBufCodecs.collection(NonNullList::createWithCapacity, ByteBufCodecs.registry(Registries.ITEM)),
            ValidItemSyncPacket::validSeasoningItem,
            ValidItemSyncPacket::new
    );

    @Override
    public void handle(PayloadContext context){
        context.execute(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof CookingPotScreen screen){
                screen.setValidInputItem(this.validInputItem);
                screen.setValidSeasoningItem(this.validSeasoningItem);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}