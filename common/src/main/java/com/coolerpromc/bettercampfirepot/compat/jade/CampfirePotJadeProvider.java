package com.coolerpromc.bettercampfirepot.compat.jade;

import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CampfirePotJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = BetterCampfirePot.id("campfire_pot");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof BetterCampfireBlockEntity blockEntity)) return;

        ItemStack potItem = blockEntity.getPotItem();
        if (potItem == null || !(potItem.getItem() instanceof BetterCampfirePotItem campfirePotItem)) return;

        CompoundTag data = accessor.getServerData();

        tooltip.add(Component.translatable("jade.bettercampfirepot.tier", BetterCampfirePot.tierName(campfirePotItem.tier)).withStyle(ChatFormatting.GRAY));

        int speed = data.contains("Speed") ? data.getInt("Speed") : blockEntity.progressPerTick;
        tooltip.add(Component.translatable("tooltip.bettercampfirepot.speed", (float) speed / 2f).withStyle(ChatFormatting.GRAY));

        if (blockEntity.currentRecipe != null) {
            tooltip.add(Component.translatable("jade.bettercampfirepot.cooking", blockEntity.currentRecipe.result().getHoverName()));
        }

        int progress = data.getInt("Progress");
        int total = data.getInt("TotalTime");
        if (progress > 0 && total > 0) {
            tooltip.add(Component.translatable("jade.bettercampfirepot.progress", progress * 100 / total));
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof BetterCampfireBlockEntity blockEntity) {
            data.putInt("Speed", blockEntity.progressPerTick);
            data.putInt("Progress", blockEntity.getCookingProgress());
            data.putInt("TotalTime", blockEntity.getCookingTotalTime());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
