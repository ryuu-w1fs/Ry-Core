package rycore.features.modules.misc;

import java.awt.FontFormatException;
import java.io.IOException;
import meteordevelopment.orbit.EventHandler;
import rycore.Rycore;
import rycore.events.impl.EventSetting;
import rycore.features.modules.Module;
import rycore.gui.clickui.ClickGUI;
import rycore.gui.font.FontRenderers;
import rycore.setting.Setting;

public class ClickGui extends Module {
   public final Setting<Boolean> blur = new Setting<>("Blur", false);
   public final Setting<Integer> moduleRound = new Setting<>("ModuleRound", 4, 1, 5);
   public final Setting<Integer> settingFontScale = new Setting<>("SettingFontScale", 15, 6, 20);
   public final Setting<Integer> modulesFontScale = new Setting<>("ModulesFontScale", 18, 6, 20);

   public ClickGui() {
      super("ClickGui", "Rycore main GUI.", Module.Category.MISC);
   }

   @Override
   public void onEnable() {
      this.applyFontSettings();
      this.setGui();
   }

   @Override
   public void onDisable() {
   }

   public void setGui() {
      mc.setScreen(ClickGUI.getClickGui());
   }

   @Override
   public void onUpdate() {
      if (!(mc.currentScreen instanceof ClickGUI)) {
         this.disable();
      }
   }

   @Override
   public boolean isToggleable() {
      return false;
   }

   public void applyFontSettings() {
      try {
         FontRenderers.sf_medium_mini = FontRenderers.create(this.settingFontScale.getValue().intValue(), "sf_medium");
         FontRenderers.sf_medium_modules = FontRenderers.create(this.modulesFontScale.getValue().intValue(), "sf_medium");
      } catch (IOException | FontFormatException e) {
         Rycore.LOGGER.warn("[ClickGui] Failed to apply font settings", e);
      }
   }

   @EventHandler
   public void onSetting(EventSetting e) {
      try {
         if (e.getSetting() == this.settingFontScale) {
            this.applyFontSettings();
         }

         if (e.getSetting() == this.modulesFontScale) {
            this.applyFontSettings();
         }
      } catch (Exception var3) {
      }
   }
}
