package rycore.events.impl;

import rycore.events.Event;

public class PostPlayerUpdateEvent extends Event {
   private int iterations;

   public int getIterations() {
      return this.iterations;
   }

   public void setIterations(int in) {
      this.iterations = in;
   }
}
