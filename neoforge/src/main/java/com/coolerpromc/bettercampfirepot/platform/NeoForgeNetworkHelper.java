package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.platform.services.INetworkHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworkHelper implements INetworkHelper {
    @Override
    public <T extends CustomPacketPayload> void sendToPlayer(ServerPlayer player, T packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }
}
