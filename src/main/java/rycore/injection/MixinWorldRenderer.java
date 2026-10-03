package rycore.injection;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.client.ModuleManager;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
   // WorldRenderer.setupTerrain da bi xoa (terrain setup nam trong
   // FrameGraph/SectionRenderDispatcher), nen FreeCam khong con buoc duoc che do
   // spectator cho viec dung frustum.

   @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
   private void renderWeatherHook(
      net.minecraft.client.render.FrameGraphBuilder builder,
      com.mojang.blaze3d.buffers.GpuBufferSlice fog,
      CallbackInfo ci
   ) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.noWeather.getValue()) {
         ci.cancel();
      }
   }
}
