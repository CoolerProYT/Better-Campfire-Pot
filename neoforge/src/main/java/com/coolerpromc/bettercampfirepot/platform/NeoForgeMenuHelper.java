package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.platform.services.IMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public class NeoForgeMenuHelper implements IMenuHelper {

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos blockPos) {
        player.openMenu(provider, blockPos);
    }
}
