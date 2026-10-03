package rycore.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import rycore.core.Managers;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.TotemPopEvent;
import rycore.features.modules.Module;
import rycore.features.modules.combat.AntiBot;
import rycore.gui.notification.Notification;
import rycore.setting.Setting;

public class TotemPopCounter extends Module {
   public Setting<Boolean> notification = new Setting<>("Notification", true);

   public TotemPopCounter() {
      super("TotemPopCounter", "Counts players' totem pops.", Module.Category.MISC);
   }

   @EventHandler
   public void onTotemPop(@NotNull TotemPopEvent event) {
      if (event.getEntity() != mc.player) {
         String s = Formatting.GREEN
            + event.getEntity().getName().getString()
            + Formatting.WHITE
            + " popped "
            + Formatting.AQUA
            + (event.getPops() > 1 ? "" + event.getPops() + Formatting.WHITE + " totems!" : Formatting.WHITE + " a totem!");
         Managers.NOTIFICATION.publicity("TotemPopCounter", s, 2, Notification.Type.INFO);
      }
   }

   @Override
   public void onUpdate() {
      for (PlayerEntity player : mc.world.getPlayers()) {
         if (player != mc.player
            && (!ModuleManager.antiBot.isEnabled() || ModuleManager.antiBot.mode.getValue() != AntiBot.Mode.Matrix || !AntiBot.isBot(player))
            && !(player.getHealth() > 0.0F)
            && Managers.COMBAT.popList.containsKey(player.getName().getString())) {
            String s = Formatting.GREEN
               + player.getName().getString()
               + Formatting.WHITE
               + " popped "
               + (
                  Managers.COMBAT.popList.get(player.getName().getString()) > 1
                     ? "" + Managers.COMBAT.popList.get(player.getName().getString()) + Formatting.WHITE + " totems and died EZ LMAO!"
                     : Formatting.WHITE + " totem and died EZ LMAO!"
               );
            Managers.NOTIFICATION.publicity("TotemPopCounter", s, 2, Notification.Type.INFO);
         }
      }
   }
}
