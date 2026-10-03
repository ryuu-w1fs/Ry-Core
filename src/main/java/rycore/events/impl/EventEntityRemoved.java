package rycore.events.impl;

import net.minecraft.entity.Entity;
import rycore.events.Event;

public class EventEntityRemoved extends Event {
   public Entity entity;

   public EventEntityRemoved(Entity entity) {
      this.entity = entity;
   }

   public Entity getEntity() {
      return this.entity;
   }
}
