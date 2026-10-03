package rycore.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.EventKeyPress;
import rycore.events.impl.EventKeyRelease;
import rycore.events.impl.EventMouse;
import rycore.features.modules.Module;
import rycore.features.modules.combat.Aura;
import rycore.features.modules.movement.freelook.CameraOverriddenEntity;
import rycore.features.modules.movement.freelook.FreeLookState;
import rycore.setting.Setting;
import rycore.setting.impl.Bind;

public class FreeLook extends Module {
   private final Setting<Bind> bind = new Setting<>("Bind", new Bind(-1, false, false));
   private final Setting<Boolean> toggle = new Setting<>("Toggle", true);
   private boolean active;
   private Perspective previousPerspective;

   public FreeLook() {
      super("FreeLook", "Free look camera.", Module.Category.MOVEMENT);
   }

   @Override
   public void onDisable() {
      this.setActive(false);
   }

   @EventHandler
   public void onKeyPress(EventKeyPress event) {
      if (!fullNullCheck()) {
         if (this.isKeyMatch(event.getKey())) {
            if (!this.isAuraBlocking()) {
               if (this.toggle.getValue()) {
                  this.setActive(!this.active);
               } else {
                  this.setActive(true);
               }
            }
         }
      }
   }

   @EventHandler
   public void onKeyRelease(EventKeyRelease event) {
      if (!fullNullCheck()) {
         if (!this.toggle.getValue()) {
            if (this.isKeyMatch(event.getKey())) {
               this.setActive(false);
            }
         }
      }
   }

   @EventHandler
   public void onMouse(EventMouse event) {
      if (!fullNullCheck()) {
         if (this.isMouseMatch(event.getButton())) {
            if (event.getAction() != 1 || !this.isAuraBlocking()) {
               if (event.getAction() == 1) {
                  if (this.toggle.getValue()) {
                     this.setActive(!this.active);
                  } else {
                     this.setActive(true);
                  }
               } else if (event.getAction() == 0 && !this.toggle.getValue()) {
                  this.setActive(false);
               }
            }
         }
      }
   }

   private boolean isKeyMatch(int key) {
      Bind b = this.bind.getValue();
      return b != null && !b.isMouse() && b.getKey() == key && key != -1;
   }

   private boolean isMouseMatch(int button) {
      Bind b = this.bind.getValue();
      return b != null && b.isMouse() && b.getKey() == button && button != -1;
   }

   private void setActive(boolean value) {
      if (this.active != value) {
         this.active = value;
         FreeLookState.setManualActive(value);
         if (mc.options != null) {
            if (this.active) {
               if (mc.player instanceof ClientPlayerEntity && mc.player instanceof CameraOverriddenEntity camera) {
                  camera.setCameraYaw(mc.player.getYaw());
                  camera.setCameraPitch(mc.player.getPitch());
               }

               this.previousPerspective = mc.options.getPerspective();
               if (this.previousPerspective != Perspective.THIRD_PERSON_FRONT) {
                  mc.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
               }
            } else {
               mc.options.setPerspective(this.previousPerspective != null ? this.previousPerspective : Perspective.FIRST_PERSON);
               this.previousPerspective = null;
            }
         }
      }
   }

   private boolean isAuraBlocking() {
      if (ModuleManager.aura == null) {
         return false;
      } else if (ModuleManager.aura.isEnabled() && Aura.target != null) {
         this.sendMessage("Không thể sử dụng cùng Aura");
         return true;
      } else {
         return false;
      }
   }
}
