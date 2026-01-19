package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleCookingPotLidPacket(boolean isLidClosed) implements CustomPacketPayload {
    public static final Type<ToggleCookingPotLidPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterCampfirePot.MODID, "toggle_cooking_pot_lid"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCookingPotLidPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ToggleCookingPotLidPacket::isLidClosed,
            ToggleCookingPotLidPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleCookingPotLidPacket packet, IPayloadContext context){
        if (context.player() instanceof ServerPlayer player){
            if (!(player.containerMenu instanceof CookingPotMenu menu)){
                BetterCampfirePot.LOGGER.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
                return;
            }

            if (menu.blockEntity instanceof BetterCampfireBlockEntity){
                menu.blockEntity.toggleLid(packet.isLidClosed());
            }
        }
    }
}
