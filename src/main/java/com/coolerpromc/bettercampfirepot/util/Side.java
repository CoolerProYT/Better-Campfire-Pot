package com.coolerpromc.bettercampfirepot.util;

import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum Side implements StringRepresentable{
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    FRONT,
    BACK;

    public static final StringRepresentable.EnumCodec<Side> CODEC = StringRepresentable.fromEnum(Side::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, Side> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public String getName() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name();
    }

    public static Side fromDirection(Direction worldSide, Direction facing) {
        return switch (worldSide) {
            case UP -> Side.TOP;
            case DOWN -> Side.BOTTOM;

            case NORTH -> facing == Direction.NORTH ? Side.FRONT
                    : facing == Direction.SOUTH ? Side.BACK
                    : facing == Direction.EAST  ? Side.LEFT
                    : Side.RIGHT;

            case SOUTH -> facing == Direction.NORTH ? Side.BACK
                    : facing == Direction.SOUTH ? Side.FRONT
                    : facing == Direction.EAST  ? Side.RIGHT
                    : Side.LEFT;

            case WEST -> facing == Direction.NORTH ? Side.LEFT
                    : facing == Direction.SOUTH ? Side.RIGHT
                    : facing == Direction.EAST  ? Side.FRONT
                    : Side.BACK;

            case EAST -> facing == Direction.NORTH ? Side.RIGHT
                    : facing == Direction.SOUTH ? Side.LEFT
                    : facing == Direction.EAST  ? Side.BACK
                    : Side.FRONT;
        };
    }
}
