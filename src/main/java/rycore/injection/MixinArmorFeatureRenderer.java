package rycore.injection;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.client.ModuleManager;

@Mixin(ArmorFeatureRenderer.class)
public class MixinArmorFeatureRenderer<S extends BipedEntityRenderState, M extends BipedEntityModel<S>, A extends BipedEntityModel<S>> {
   @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
   private void onRenderArmor(
      MatrixStack matrices,
      net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
      net.minecraft.item.ItemStack stack,
      EquipmentSlot equipmentSlot,
      int i,
      S renderState,
      CallbackInfo ci
   ) {
      if (ModuleManager.customModel != null && ModuleManager.customModel.shouldHideArmorSlotRender(null)) {
         ci.cancel();
      } else {
         if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.armor.getValue()) {
            ci.cancel();
         }
      }
   }
}
