package rycore.utility.interfaces;

import java.util.List;
import rycore.features.modules.combat.Aura;

public interface IEntityLiving {
   double getPrevServerX();

   double getPrevServerY();

   double getPrevServerZ();

   List<Aura.Position> getPositionHistory();
}
