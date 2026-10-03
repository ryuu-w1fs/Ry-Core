package rycore.injection;

import net.minecraft.client.render.block.entity.MobSpawnerBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.Module;

@Mixin(MobSpawnerBlockEntityRenderer.class)
public class MixinMobSpawnerBlockEntityRenderer {
   @Inject(
      method = "render(Lnet/minecraft/client/render/block/entity/state/MobSpawnerBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void renderHook(CallbackInfo ci) {
      if (!Module.fullNullCheck() && ModuleManager.noRender.isOn() && ModuleManager.noRender.spawnerEntity.getValue()) {
         ci.cancel();
      }
   }
}
