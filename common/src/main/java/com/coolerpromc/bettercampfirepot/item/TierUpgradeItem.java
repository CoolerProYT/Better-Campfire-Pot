package com.coolerpromc.bettercampfirepot.item;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.block.campfirepot.CampfireBlock;
import com.cobblemon.mod.common.block.campfirepot.CampfirePotBlock;
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity;
import com.cobblemon.mod.common.item.CampfirePotItem;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.BetterCampfirePotConfig;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.block.BetterCampfirePotBlock;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

public class TierUpgradeItem extends Item {
    public final String fromTier;
    public final String toTier;

    public TierUpgradeItem(String fromTier, String toTier, Properties properties) {
        super(properties);
        this.fromTier = fromTier;
        this.toTier = toTier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = world.getBlockState(blockPos);
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        Player player = context.getPlayer();

        if (player != null && player.isCrouching()){
            // Handle use on better block entity
            if (blockEntity instanceof BetterCampfireBlockEntity be){
                ItemStack pot = be.getPotItem();
                if (pot != null && pot.getItem() instanceof BetterCampfirePotItem potItem){
                    String beTier = potItem.tier;
                    if (beTier.equals(fromTier)){
                        Optional<BetterCampfirePotItem> optional = BetterCampfirePot.findBetterPotByTierAndColor(potItem, toTier);
                        if (optional.isPresent()){
                            BetterCampfirePotItem newPotItem = optional.get();
                            ItemStack newPot = new ItemStack(newPotItem);
                            be.setPotItem(newPot);
                            be.progressPerTick = BetterCampfirePotConfig.CONFIG.getTickByTier(newPotItem.tier);
                            be.onItemUpdate(world);
                            context.getItemInHand().consume(1, player);
                            world.playSound(null, blockPos, CobblemonSounds.CAMPFIRE_POT_SET, SoundSource.BLOCKS, 1.0F, 1.0F);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
            // Handle use on vanilla block entity
            else if (blockEntity instanceof CampfireBlockEntity be && blockState.getBlock() instanceof CampfireBlock campfireBlock){
                ItemStack pot = be.getPotItem();
                if (pot != null && pot.getItem() instanceof CampfirePotItem potItem){
                    String beTier = Tiers.VANILLA;
                    if (beTier.equals(fromTier)){
                        Optional<BetterCampfirePotItem> optional = BetterCampfirePot.findBetterPotByTierAndColor(potItem.getColor(), toTier);
                        if (optional.isPresent()){
                            BetterCampfirePotItem newPotItem = optional.get();
                            ItemStack newPot = new ItemStack(newPotItem);
                            BlockState newBlockState = (campfireBlock.isSoul() ? BetterCampfirePot.BETTER_SOUL_CAMPFIRE_BLOCK.get() : BetterCampfirePot.BETTER_CAMPFIRE_BLOCK.get())
                                    .defaultBlockState()
                                    .setValue(HorizontalDirectionalBlock.FACING, blockState.getValue(HorizontalDirectionalBlock.FACING))
                                    .setValue(BetterCampfireBlock.ITEM_DIRECTION, blockState.getValue(CampfireBlock.Companion.getITEM_DIRECTION()))
                                    .setValue(BetterCampfireBlock.POWERED, blockState.getValue(CampfireBlock.Companion.getPOWERED()))
                                    .setValue(BetterCampfireBlock.COOKING, blockState.getValue(CampfireBlock.Companion.getCOOKING()))
                                    .setValue(BetterCampfireBlock.LID, blockState.getValue(CampfireBlock.Companion.getLID()));
                            Containers.dropContents(world, blockPos, be);
                            world.setBlockAndUpdate(blockPos, newBlockState);
                            be.setRemoved();

                            List<ItemEntity> itemEntity = world.getEntitiesOfClass(ItemEntity.class, AABB.ofSize(blockPos.getCenter(), 4, 4, 4));

                            for (ItemEntity entity : itemEntity){
                                if (entity.getItem().is(potItem)){
                                    entity.setRemoved(Entity.RemovalReason.KILLED);
                                    break;
                                }
                            }

                            BlockEntity newBlockEntity = world.getBlockEntity(blockPos);
                            if (newBlockEntity instanceof BetterCampfireBlockEntity customCampfire) {
                                if (customCampfire.getPotItem() == null || customCampfire.getPotItem().isEmpty()) {
                                    customCampfire.setPotItem(newPot);
                                    customCampfire.progressPerTick = BetterCampfirePotConfig.CONFIG.getTickByTier(newPotItem.tier);
                                    context.getItemInHand().consume(1, player);
                                    world.playSound(null, blockPos, CobblemonSounds.CAMPFIRE_POT_SET, SoundSource.BLOCKS, 1.0F, 1.0F);
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
            else if (blockEntity == null){
                if (blockState.getBlock() instanceof BetterCampfirePotBlock potBlock){
                    Item pot = potBlock.asItem();
                    if (pot instanceof BetterCampfirePotItem potItem){
                        String blockTier = potItem.tier;
                        if (blockTier.equals(fromTier)){
                            Optional<BetterCampfirePotItem> optional = BetterCampfirePot.findBetterPotByTierAndColor(potItem, toTier);
                            if (optional.isPresent()){
                                BetterCampfirePotItem newPotItem = optional.get();
                                BlockState newBlockState = newPotItem.getBlock()
                                        .defaultBlockState()
                                        .setValue(HorizontalDirectionalBlock.FACING, blockState.getValue(HorizontalDirectionalBlock.FACING))
                                        .setValue(BetterCampfirePotBlock.OCCUPIED, blockState.getValue(BetterCampfirePotBlock.OCCUPIED))
                                        .setValue(BetterCampfirePotBlock.OPEN, blockState.getValue(BetterCampfirePotBlock.OPEN))
                                        .setValue(BetterCampfirePotBlock.WATERLOGGED, blockState.getValue(BetterCampfirePotBlock.WATERLOGGED));
                                world.setBlockAndUpdate(blockPos, newBlockState);

                                context.getItemInHand().consume(1, player);
                                world.playSound(null, blockPos, CobblemonSounds.CAMPFIRE_POT_SET, SoundSource.BLOCKS, 1.0F, 1.0F);
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                }
                else if (blockState.getBlock() instanceof CampfirePotBlock potBlock){
                    Item pot = potBlock.asItem();
                    if (pot instanceof CampfirePotItem potItem){
                        String blockTier = Tiers.VANILLA;
                        if (blockTier.equals(fromTier)){
                            Optional<BetterCampfirePotItem> optional = BetterCampfirePot.findBetterPotByTierAndColor(potItem.getColor(), toTier);
                            if (optional.isPresent()){
                                BetterCampfirePotItem newPotItem = optional.get();
                                BlockState newBlockState = newPotItem.getBlock()
                                        .defaultBlockState()
                                        .setValue(HorizontalDirectionalBlock.FACING, blockState.getValue(HorizontalDirectionalBlock.FACING))
                                        .setValue(BetterCampfirePotBlock.OCCUPIED, blockState.getValue(CampfirePotBlock.Companion.getOCCUPIED()))
                                        .setValue(BetterCampfirePotBlock.OPEN, blockState.getValue(CampfirePotBlock.Companion.getOPEN()))
                                        .setValue(BetterCampfirePotBlock.WATERLOGGED, blockState.getValue(BlockStateProperties.WATERLOGGED));
                                world.setBlockAndUpdate(blockPos, newBlockState);

                                context.getItemInHand().consume(1, player);
                                world.playSound(null, blockPos, CobblemonSounds.CAMPFIRE_POT_SET, SoundSource.BLOCKS, 1.0F, 1.0F);
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.bettercampfirepot.tier_upgrade_item", BetterCampfirePot.tierName(fromTier), BetterCampfirePot.tierName(toTier));
    }
}
