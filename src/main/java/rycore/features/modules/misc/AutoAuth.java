package rycore.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import rycore.core.Managers;
import rycore.events.impl.PacketEvent;
import rycore.features.modules.Module;
import rycore.gui.notification.Notification;
import rycore.setting.Setting;

public final class AutoAuth extends Module {
   private final Setting<AutoAuth.Mode> mode = new Setting<>("Mode", AutoAuth.Mode.Default);
   private final Setting<String> cpass = new Setting<>("Password", "vudeptrai88", v -> this.mode.getValue() == AutoAuth.Mode.Custom);
   private final Setting<Boolean> show = new Setting<>("ShowPassword", true);

   public AutoAuth() {
      super("AutoAuth", "Auto logs into servers.", Module.Category.MISC);
   }

   @Override
   public void onEnable() {
      String warningMsg = Formatting.RED
         + "Attention! "
         + Formatting.RESET
         + "Passwords are stored in the config. Before sharing your configs, disable this module.";
      this.sendMessage(warningMsg);
   }

   @Override
   public void onDisable() {
      this.sendMessage("Resetting password...");
      this.cpass.setValue("none");
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.@NotNull Receive event) {
      if (event.getPacket() instanceof GameMessageS2CPacket pac && mc.getNetworkHandler() != null) {
         String password = "";
         switch ((AutoAuth.Mode)this.mode.getValue()) {
            case Default:
               password = "pass12345";
               break;
            case Random:
               password = RandomStringUtils.randomAlphabetic(5) + RandomStringUtils.randomPrint(5);
               break;
            case Custom:
               password = this.cpass.getValue();
               if (password.isEmpty()) {
                  this.sendMessage(Formatting.RED + "Registration error: Password is empty!");
                  return;
               }
         }

         String m = pac.content().getString().toLowerCase();
         if (m.contains("/register") || m.contains("/reg") || m.contains("dk")) {
            mc.getNetworkHandler().sendChatCommand("register " + password + " " + password);
            if (this.show.getValue()) {
               this.sendMessage("Your password: " + Formatting.RED + password);
            }

            Managers.NOTIFICATION.publicity("AutoAuth", "Registration completed!", 4, Notification.Type.SUCCESS);
         } else if (m.contains("/login") || m.contains("/l") || m.contains("/dn")) {
            mc.getNetworkHandler().sendChatCommand("login " + password);
            Managers.NOTIFICATION.publicity("AutoAuth", "Logged in!", 4, Notification.Type.SUCCESS);
         }
      }
   }

   private enum Mode {
      Default,
      Random,
      Custom;
   }
}
