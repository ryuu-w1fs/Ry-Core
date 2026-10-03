package rycore.events.impl;

import net.minecraft.entity.Entity;
import rycore.events.Event;

public class EventAttack extends Event {
   private Entity entity;
   boolean pre;

   public EventAttack(Entity entity, boolean pre) {
      this.entity = entity;
      this.pre = pre;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public boolean isPre() {
      return this.pre;
   }
}
