package rycore.features.modules.combat.aura.rotation;

import rycore.features.modules.combat.Aura;

public class None implements RotationModeHandler {
   @Override
   public void rotate(Aura aura, boolean ready) {
      Track.rotateClassic(aura, ready);
   }
}
