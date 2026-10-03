package rycore.utility.interfaces;

import rycore.features.modules.combat.Aura;

public interface IOtherClientPlayerEntity {
   void resolve(Aura.Resolver var1);

   void releaseResolver();
}
