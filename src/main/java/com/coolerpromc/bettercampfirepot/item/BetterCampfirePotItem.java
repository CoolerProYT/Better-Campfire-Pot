package com.coolerpromc.bettercampfirepot.item;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.block.campfirepot.CampfirePotColor;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.BetterCampfirePotConfig;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BetterCampfirePotItem extends BlockItem {
    public final String tier;
    public final CampfirePotColor color;

    public BetterCampfirePotItem(Block block, String tier, CampfirePotColor color, Properties properties) {
        super(block, properties);
        this.tier = tier;
        this.color = color;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = world.getBlockState(blockPos);
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        Player player = context.getPlayer();

        if (player != null && blockState.getBlock() instanceof CampfireBlock && blockEntity instanceof CampfireBlockEntity) {
            if (blockState.getValue(CampfireBlock.LIT)) {
                Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
                Direction itemFacing = Direction.fromYRot(player.getYHeadRot());
                boolean isSoul = blockState.getBlock().asItem().toString().equals("minecraft:soul_campfire");

                CampfireBlockEntity campfire = (CampfireBlockEntity) blockEntity;
                for (ItemStack item : campfire.getItems()) {
                    if (!item.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(
                                world,
                                blockPos.getX() + 0.5,
                                blockPos.getY() + 1.0,
                                blockPos.getZ() + 0.5,
                                item
                        );
                        itemEntity.setDefaultPickUpDelay();
                        world.addFreshEntity(itemEntity);
                    }
                }

                blockEntity.setRemoved();

                BlockState newBlockState = (isSoul ? BetterCampfirePot.BETTER_SOUL_CAMPFIRE_BLOCK : BetterCampfirePot.BETTER_CAMPFIRE_BLOCK)
                        .defaultBlockState()
                        .setValue(HorizontalDirectionalBlock.FACING, facing)
                        .setValue(BetterCampfireBlock.ITEM_DIRECTION, itemFacing.getOpposite());
                world.setBlockAndUpdate(blockPos, newBlockState);

                BlockEntity newBlockEntity = world.getBlockEntity(blockPos);
                if (newBlockEntity instanceof BetterCampfireBlockEntity customCampfire) {
                    if (customCampfire.getPotItem() == null || customCampfire.getPotItem().isEmpty()) {
                        customCampfire.setPotItem(context.getItemInHand().split(1));
                        customCampfire.progressPerTick = BetterCampfirePotConfig.CONFIG.getTickByTier(this.tier);
                        world.playSound(null, blockPos, CobblemonSounds.CAMPFIRE_POT_SET, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            else if (!player.isCrouching()) {
                return InteractionResult.FAIL;
            }
        }

        return super.useOn(context);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.bettercampfirepot.pot", Component.translatable("tier.bettercampfirepot." + this.tier), Component.translatable("block.cobblemon.campfire_pot_" + this.color.getSuffix()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.bettercampfirepot.speed", (float) BetterCampfirePotConfig.CONFIG.getTickByTier(this.tier) / 2f).withStyle(ChatFormatting.GRAY));
    }
}
