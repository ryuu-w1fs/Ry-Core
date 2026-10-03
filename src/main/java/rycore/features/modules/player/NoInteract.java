package rycore.features.modules.player;

import rycore.features.modules.Module;
import rycore.setting.Setting;

public class NoInteract extends Module {
   public static Setting<Boolean> onlyAura = new Setting<>("OnlyAura", false);

   public NoInteract() {
      super("NoInteract", "Prevents opening containers.", Module.Category.PLAYER);
   }
}
