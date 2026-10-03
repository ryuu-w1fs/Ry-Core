package rycore.features.modules.misc;

import rycore.features.modules.Module;
import rycore.setting.Setting;

public final class Notifications extends Module {
   public final Setting<Notifications.Mode> mode = new Setting<>("Mode", Notifications.Mode.Default);

   public Notifications() {
      super("Notifications", "Client notifications.", Module.Category.MISC);
   }

   public enum Mode {
      Default,
      CrossHair;
   }
}
