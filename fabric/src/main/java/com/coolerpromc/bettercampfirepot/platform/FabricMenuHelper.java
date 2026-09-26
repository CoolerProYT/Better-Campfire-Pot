package com.coolerpromc.bettercampfirepot.platform;

import com.coolerpromc.bettercampfirepot.platform.services.IMenuHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class FabricMenuHelper implements IMenuHelper {
    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, BlockPos blockPos) {
        player.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override
            public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return provider.createMenu(containerId, inventory, player);
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public BlockPos getScreenOpeningData(ServerPlayer player) {
                return blockPos;
            }
        });
    }
}
