package rycore.injection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.client.ModuleManager;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {
   @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
   private static void renderFireOverlayHook(
      MatrixStack matrixStack, net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
      net.minecraft.client.texture.Sprite sprite, CallbackInfo ci
   ) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.fireOverlay.getValue()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
   private static void renderUnderwaterOverlayHook(
      MinecraftClient minecraftClient, MatrixStack matrixStack,
      net.minecraft.client.render.VertexConsumerProvider vertexConsumers, CallbackInfo ci
   ) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.waterOverlay.getValue()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
   private static void renderInWallOverlayHook(
      net.minecraft.client.texture.Sprite sprite, MatrixStack matrixStack,
      net.minecraft.client.render.VertexConsumerProvider vertexConsumers, CallbackInfo ci
   ) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.blockOverlay.getValue()) {
         ci.cancel();
      }
   }
}
