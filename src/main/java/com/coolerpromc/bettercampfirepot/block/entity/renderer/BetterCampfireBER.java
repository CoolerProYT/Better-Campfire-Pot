package com.coolerpromc.bettercampfirepot.block.entity.renderer;

import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.block.BetterCampfirePotBlock;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.List;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public record BetterCampfireBER(BlockEntityRendererProvider.Context context) implements BlockEntityRenderer<BetterCampfireBlockEntity> {
    public static final float CIRCLE_RADIUS = 0.4F;
    public static final float ROTATION_SPEED = 1.5F;
    public static final float JUMP_AMPLITUDE = 0.025F;
    public static final float JUMP_SPEED = 0.1F;
            
    @Override
    public void render(BetterCampfireBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var campfirePotItem = blockEntity.getPotItem() != null && blockEntity.getPotItem().getItem() instanceof BetterCampfirePotItem item ? item : null;
        if (campfirePotItem == null) return;

        poseStack.pushPose();

        Direction facing = blockEntity.getBlockState().getValue(FACING);
        float rotationAngle = switch (facing) {
            case Direction.NORTH -> 180F;
            case Direction.SOUTH -> 0F;
            case Direction.WEST -> 90F;
            case Direction.EAST -> -90F;
            default -> 0f;
        };

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
        poseStack.translate(-0.5, -0.5, -0.5);

        renderPot(campfirePotItem, blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        renderSeasonings(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderPot(BetterCampfirePotItem campfirePotItem, BetterCampfireBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        boolean isLidOpen = !blockEntity.getBlockState().getValue(BetterCampfireBlock.LID);

        float yRot = (blockEntity.getBlockState().getValue(BetterCampfireBlock.ITEM_DIRECTION).getOpposite().toYRot() +
                blockEntity.getBlockState().getValue(FACING).toYRot()) % 360;

        poseStack.pushPose();
        poseStack.translate(0.0, 0.4375, 0.0);

        BlockRenderDispatcher blockRenderer = context.getBlockRenderDispatcher();
        BlockState state = campfirePotItem.getBlock().defaultBlockState()
                .setValue(BetterCampfirePotBlock.OPEN, isLidOpen)
                .setValue(FACING, Direction.fromYRot(yRot))
                .setValue(BetterCampfirePotBlock.OCCUPIED, (!blockEntity.getSeasonings().isEmpty() || !blockEntity.getIngredients().isEmpty()));
        BakedModel bakedModel = blockRenderer.getBlockModel(state);

        float red = FastColor.ARGB32.red(blockEntity.brothColor) / 255F;
        float green = FastColor.ARGB32.green(blockEntity.brothColor) / 255F;
        float blue = FastColor.ARGB32.blue(blockEntity.brothColor) / 255F;

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)),
                state,
                bakedModel,
                red,
                green,
                blue,
                light,
                overlay
        );

        poseStack.popPose();
    }

    private void renderSeasonings(BetterCampfireBlockEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, int overlay) {
        List<ItemStack> seasonings = blockEntity.getSeasonings();

        float gameTime = blockEntity.time + tickDelta;
        float rotationAngle = (gameTime * ROTATION_SPEED) % 360;

        for (int index = 0; index < seasonings.size(); index++) {
            ItemStack seasoning = seasonings.get(index);

            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);

            float angleOffset = index * (360f / seasonings.size());
            double angleInRadians = Math.toRadians(rotationAngle + angleOffset);

            float xOffset = (float) (Math.cos(angleInRadians) * CIRCLE_RADIUS);
            float zOffset = (float) (Math.sin(angleInRadians) * CIRCLE_RADIUS);
            float jumpOffset = (float) (Math.sin(gameTime * JUMP_SPEED + index * 2) * JUMP_AMPLITUDE);

            poseStack.translate(1F + xOffset, 1.24F + jumpOffset, 1F + zOffset);

            Vector3f lookAtDirection = new Vector3f(1f + xOffset - 1F, 0F, 1F + zOffset - 1F);
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    (float) (-Math.toDegrees(Math.atan2(lookAtDirection.z(), lookAtDirection.x()))) + 90
            ));

            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(22.5F));

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    seasoning,
                    ItemDisplayContext.GROUND,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    multiBufferSource,
                    blockEntity.getLevel(),
                    0
            );

            poseStack.popPose();
            poseStack.popPose();
        }
    }

    private void drawQuad(
            VertexConsumer builder,
            PoseStack poseStack,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float u0, float v0, float u1, float v1,
            int packedLight, int color
    ) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, color);
        drawVertex(builder, poseStack, x0, y1, z1, u0, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y0, z0, u1, v0, packedLight, color);
    }

    private void drawVertex(
            VertexConsumer builder,
            PoseStack poseStack,
            float x, float y, float z,
            float u, float v,
            int packedLight, int color
    ) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(0f, 1f, 0f);
    }
}
