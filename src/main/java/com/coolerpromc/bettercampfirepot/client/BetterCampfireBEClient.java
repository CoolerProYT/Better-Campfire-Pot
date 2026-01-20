package com.coolerpromc.bettercampfirepot.client;

import com.cobblemon.mod.common.client.sound.BlockEntitySoundTracker;
import com.cobblemon.mod.common.client.sound.instances.CancellableSoundInstance;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity.BASE_BROTH_BUBBLE_COLOR;
import static com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity.BASE_BROTH_COLOR;

public class BetterCampfireBEClient {
    public static void clientTick(Level level, BlockPos pos, BlockState state, BetterCampfireBlockEntity campfireBlockEntity) {
        if (!level.isClientSide) return;

        boolean isCooking = state.getValue(BetterCampfireBlock.COOKING);
        boolean isRunningSoundActive = BlockEntitySoundTracker.INSTANCE.isActive(pos, campfireBlockEntity.runningSound.getLocation());
        boolean isAmbientSoundActive = BlockEntitySoundTracker.INSTANCE.isActive(pos, campfireBlockEntity.ambientSound.getLocation());
        boolean containsItems = !campfireBlockEntity.getSeasonings().isEmpty() || !campfireBlockEntity.getIngredients().isEmpty();

        if (containsItems) {
            if (isCooking) {
                BlockEntitySoundTracker.INSTANCE.stop(pos, campfireBlockEntity.ambientSound.getLocation());
                if (!isRunningSoundActive) {
                    BlockEntitySoundTracker.INSTANCE.play(pos, new CancellableSoundInstance(campfireBlockEntity.runningSound, pos, true, 1.0f, 1.0f));
                }
            } else {
                BlockEntitySoundTracker.INSTANCE.stop(pos, campfireBlockEntity.runningSound.getLocation());
                if (!isAmbientSoundActive) {
                    BlockEntitySoundTracker.INSTANCE.play(pos, new CancellableSoundInstance(campfireBlockEntity.ambientSound, pos, true, 1.0f, 1.0f));
                }
            }
        } else {
            BlockEntitySoundTracker.INSTANCE.stop(pos, campfireBlockEntity.runningSound.getLocation());
            BlockEntitySoundTracker.INSTANCE.stop(pos, campfireBlockEntity.ambientSound.getLocation());
        }

        List<ItemStack> seasonings = campfireBlockEntity.getSeasonings();
        if (seasonings.size() != campfireBlockEntity.lastSeasoningStacks.size() || !seasonings.stream().allMatch(s -> campfireBlockEntity.lastSeasoningStacks.contains(s))) {
            campfireBlockEntity.lastSeasoningStacks = new ArrayList<>(seasonings);

            Integer colorMix = getColourMixFromSeasonings(seasonings, false);
            campfireBlockEntity.brothColor = colorMix != null ? colorMix : BASE_BROTH_COLOR;

            Integer bubbleColorMix = getColourMixFromSeasonings(seasonings, true);
            campfireBlockEntity.bubbleColor = bubbleColorMix != null ? bubbleColorMix : BASE_BROTH_BUBBLE_COLOR;
        }

        if (campfireBlockEntity.particleCooldown > 0) {
            campfireBlockEntity.particleCooldown--;
        } else {
            if (isCooking) {
                Vec3 position = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5375, pos.getZ() + 0.5);
                campfireBlockEntity.particleEntityHandler(
                        position,
                        level,
                        ResourceLocation.fromNamespaceAndPath("cobblemon", "broth_bubbles")
                );
            }
            campfireBlockEntity.particleCooldown = 20;
        }

        campfireBlockEntity.time++;
    }

    private static Integer getColourMixFromSeasonings(List<ItemStack> seasonings, boolean isBubble) {
        // Implementation needed - this method should calculate color mix from seasonings
        return null;
    }

}
