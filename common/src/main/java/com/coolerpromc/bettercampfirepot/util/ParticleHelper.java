package com.coolerpromc.bettercampfirepot.util;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions;
import com.cobblemon.mod.common.client.particle.ParticleStorm;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import kotlin.Unit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public class ParticleHelper {
    public static ParticleStorm particleStorm(BlockState blockState, BedrockParticleOptions effect, MatrixWrapper wrapper, ClientLevel level, Vector4f vector4f) {
        return new ParticleStorm(
                effect,
                wrapper,
                wrapper,
                level,
                () -> Vec3.ZERO,
                () -> blockState.getValue(BetterCampfireBlock.COOKING),
                () -> blockState.getValue(BetterCampfireBlock.COOKING),
                null,
                () -> Unit.INSTANCE,
                () -> vector4f,
                new MoLangRuntime(),
                null
        );
    }
}
