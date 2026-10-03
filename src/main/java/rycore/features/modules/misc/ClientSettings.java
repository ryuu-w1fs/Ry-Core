package rycore.features.modules.misc;

import rycore.features.modules.Module;
import rycore.setting.Setting;

public final class ClientSettings extends Module {
   public static Setting<Boolean> renderRotations = new Setting<>("RenderRotations", true);
   public static Setting<Boolean> clientMessages = new Setting<>("ClientMessages", false);
   public static Setting<Boolean> debug = new Setting<>("Debug", false);
   public static Setting<ClientSettings.FireWorkMode> fireWorkMode = new Setting<>("FireWork Mode", ClientSettings.FireWorkMode.MainHand);
   public static Setting<ClientSettings.ClipMode> clipMode = new Setting<>("ClipMode", ClientSettings.ClipMode.Matrix);
   public static Setting<String> prefix = new Setting<>("Prefix", ".");

   public ClientSettings() {
      super("ClientSettings", "Main client settings.", Module.Category.MISC);
   }

   @Override
   public boolean isToggleable() {
      return false;
   }

   public enum ClipMode {
      Default,
      Matrix;
   }

   public enum FireWorkMode {
      MainHand,
      OffHand;
   }
}
