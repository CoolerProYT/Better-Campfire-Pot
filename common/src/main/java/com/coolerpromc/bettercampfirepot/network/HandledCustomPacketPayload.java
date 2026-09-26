package com.coolerpromc.bettercampfirepot.network;

import com.coolerpromc.bettercampfirepot.platform.util.PayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface HandledCustomPacketPayload extends CustomPacketPayload {
    void handle(PayloadContext context);
}
