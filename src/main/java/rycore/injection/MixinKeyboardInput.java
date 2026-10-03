package rycore.injection;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.Rycore;
import rycore.events.impl.EventKeyboardInput;
import rycore.features.modules.Module;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {
   @Unique
   private boolean rycore$clearMovementInput;

   // 1.21.11: field 'sneaking' da bi bo (gop vao PlayerInput) nen khong con
   // diem FIELD de chen; dung HEAD cua tick().
   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   private void onSneak(CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventKeyboardInput event = new EventKeyboardInput((Input)(Object)this);
         Rycore.EVENT_BUS.post(event);
         this.rycore$clearMovementInput = event.shouldClearMovementInput();
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "tick", at = @At("RETURN"))
   private void onTickReturn(CallbackInfo ci) {
      if (this.rycore$clearMovementInput) {
         EventKeyboardInput.clearMovementInput((Input)(Object)this);
         this.rycore$clearMovementInput = false;
      }
   }
}
