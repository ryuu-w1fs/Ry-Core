package rycore.core.hooks;

import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.misc.UnHook;

public class ModuleShutdownHook extends Thread {
   @Override
   public void run() {
      if (UnHook.isActive()) {
         ModuleManager.unHook.disable();
      }
   }
}
