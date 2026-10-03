package rycore.injection;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.Rycore;
import rycore.core.Managers;
import rycore.events.impl.EventKeyPress;
import rycore.events.impl.EventKeyRelease;
import rycore.features.modules.Module;
import rycore.gui.clickui.ClickGUI;

@Mixin(Keyboard.class)
public class MixinKeyboard {
   @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
   private void onKey(long windowPointer, int action, net.minecraft.client.input.KeyInput input, CallbackInfo ci) {
      // key/scanCode/modifiers gop vao record KeyInput.
      int key = input.key();
      int scanCode = input.scancode();
      int modifiers = input.modifiers();
      if (!Module.fullNullCheck()) {
         boolean whitelist = Module.mc.currentScreen == null || Module.mc.currentScreen instanceof ClickGUI;
         if (whitelist) {
            if (action == 0) {
               Managers.MODULE.onKeyReleased(key);
            }

            if (action == 1) {
               Managers.MODULE.onKeyPressed(key);
            }

            if (action == 2) {
               action = 1;
            }

            switch (action) {
               case 0:
                  EventKeyRelease eventx = new EventKeyRelease(key, scanCode);
                  Rycore.EVENT_BUS.post(eventx);
                  if (eventx.isCancelled()) {
                     ci.cancel();
                  }
                  break;
               case 1:
                  EventKeyPress event = new EventKeyPress(key, scanCode);
                  Rycore.EVENT_BUS.post(event);
                  if (event.isCancelled()) {
                     ci.cancel();
                  }
            }
         }
      }
   }
}
