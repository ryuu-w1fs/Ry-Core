package rycore.injection;

import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.utility.render.chunk.ChunkAnimations;

@Mixin(BuiltChunk.class)
public class MixinChunkBuilderBuiltChunk {
   // 1.21.11: BuiltChunk.setOrigin da bi xoa (origin la field final Mutable).
}
