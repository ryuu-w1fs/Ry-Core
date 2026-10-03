package rycore.events.impl;

import rycore.events.Event;
import rycore.setting.Setting;

public class EventSetting extends Event {
   final Setting<?> setting;

   public EventSetting(Setting<?> setting) {
      this.setting = setting;
   }

   public Setting<?> getSetting() {
      return this.setting;
   }
}
