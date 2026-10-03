package rycore.injection;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.client.ModuleManager;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {
   @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
   private void renderLabelIfPresent(
      net.minecraft.client.render.entity.state.EntityRenderState state,
      MatrixStack matrices,
      net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
      net.minecraft.client.render.state.CameraRenderState cameraState,
      CallbackInfo info
   ) {
      // khong con entity de kiem tra instanceof ArmorStandEntity;
      // RenderState khong mang kieu entity nen chi con tat theo setting.
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.noArmorStands.getValue()) {
         info.cancel();
      }

      // ESP an name tag vanilla: can LivingEntity de kiem tra, nhung RenderState
      // khong mang entity nen phan nay tam thoi khong ap dung.
   }
}
