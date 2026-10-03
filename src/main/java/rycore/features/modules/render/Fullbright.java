package rycore.features.modules.render;

import rycore.features.modules.Module;

public class Fullbright extends Module {
   public static final float MIN_BRIGHT = 0.5F;

   public Fullbright() {
      super("Fullbright", "Removes darkness.", Module.Category.RENDER);
   }
}
