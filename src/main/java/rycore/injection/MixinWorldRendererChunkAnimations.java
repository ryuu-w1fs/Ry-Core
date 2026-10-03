package rycore.injection;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import rycore.utility.render.chunk.ChunkAnimations;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRendererChunkAnimations {
   // 1.21.11: WorldRenderer.renderLayer va GlUniform.set(FFF) da bi xoa;
   // chunk animation can viet lai theo RenderPipeline/UBO.
}
