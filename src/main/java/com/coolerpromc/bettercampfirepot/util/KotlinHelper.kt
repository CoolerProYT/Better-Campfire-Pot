package com.coolerpromc.bettercampfirepot.util

import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.joml.Vector4f

class KotlinHelper {
    companion object{
        fun particleStorm(blockState: BlockState, effect: BedrockParticleOptions, wrapper: MatrixWrapper, level: ClientLevel, vector4f: Vector4f): ParticleStorm{
            return ParticleStorm(
                effect,
                wrapper,
                wrapper,
                level,
                { Vec3.ZERO },
                { blockState.getValue(BetterCampfireBlock.COOKING) },
                { blockState.getValue(BetterCampfireBlock.COOKING) },
                null,
                {},
                { vector4f },
                MoLangRuntime(),
                null
            )
        }
    }
}