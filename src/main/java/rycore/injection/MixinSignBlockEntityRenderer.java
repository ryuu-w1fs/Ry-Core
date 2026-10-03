package rycore.injection;

import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.SignBlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.client.ModuleManager;

/**
 * Tat chu tren bien (NoRender > signText).
 *
 * <p>1.21.11: {@code renderText} chuyen tu SignBlockEntityRenderer len
 * {@link AbstractSignBlockEntityRenderer} va nhan SignBlockEntityRenderState,
 * nen mixin phai nham vao lop cha voi chu ky moi.
 */
@Mixin(AbstractSignBlockEntityRenderer.class)
public class MixinSignBlockEntityRenderer {
   @Inject(method = "renderText", at = @At("HEAD"), cancellable = true)
   private void renderTextHook(
      SignBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, boolean front, CallbackInfo ci
   ) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.signText.getValue()) {
         ci.cancel();
      }
   }
}
