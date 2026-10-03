package rycore.injection;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.Rycore;
import rycore.core.Managers;
import rycore.events.impl.EventMouse;
import rycore.features.modules.Module;

@Mixin(Mouse.class)
public class MixinMouse {
   @Inject(method = "onMouseButton", at = @At("HEAD"))
   public void onMouseButtonHook(long window, net.minecraft.client.input.MouseInput input, int action, CallbackInfo ci) {
      // button/mods gop vao record MouseInput.
      int button = input.button();
      int mods = input.modifiers();
      if (window == Module.mc.getWindow().getHandle()) {
         if (action == 0) {
            Managers.MODULE.onMoseKeyReleased(button);
         }

         if (action == 1) {
            Managers.MODULE.onMoseKeyPressed(button);
         }

         Rycore.EVENT_BUS.post(new EventMouse(button, action));
      }
   }

   @Inject(method = "onMouseScroll", at = @At("HEAD"))
   private void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
      if (window == Module.mc.getWindow().getHandle()) {
         Rycore.EVENT_BUS.post(new EventMouse((int)vertical, 2));
      }
   }
}
