package rycore.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import rycore.events.impl.EventKeyboardInput;
import rycore.features.modules.Module;
import rycore.setting.Setting;

import rycore.utility.player.InputCompat;

public class AutoWalk extends Module {
   private final Setting<AutoWalk.Mode> mode = new Setting<>("Mode", AutoWalk.Mode.Simple);

   public AutoWalk() {
      super("AutoWalk", "Auto walks forward.", Module.Category.MOVEMENT);
   }

   @Override
   public void onEnable() {
      if (this.mode.getValue() == AutoWalk.Mode.Baritone) {
         mc.player
            .networkHandler
            .sendChatMessage(
               "#goto "
                  + 3000000.0 * Math.cos(Math.toRadians(mc.player.getYaw() + 90.0F))
                  + " "
                  + 3000000.0 * Math.sin(Math.toRadians(mc.player.getYaw() + 90.0F))
            );
      }
   }

   @Override
   public void onDisable() {
      if (this.mode.getValue() == AutoWalk.Mode.Baritone) {
         mc.player.networkHandler.sendChatMessage("#stop");
      }
   }

   @EventHandler
   public void onKey(EventKeyboardInput e) {
      if (this.mode.getValue() == AutoWalk.Mode.Simple) {
         InputCompat.setForward(mc.player.input, 1.0F);
      }
   }

   public enum Mode {
      Simple,
      Baritone;
   }
}
