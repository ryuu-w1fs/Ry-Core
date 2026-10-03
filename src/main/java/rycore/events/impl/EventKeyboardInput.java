package rycore.events.impl;

import net.minecraft.client.input.Input;
import rycore.events.Event;

import rycore.utility.player.InputCompat;

public class EventKeyboardInput extends Event {
   private final Input input;
   private boolean clearMovementInput;

   public EventKeyboardInput(Input input) {
      this.input = input;
   }

   public Input getInput() {
      return this.input;
   }

   public void clearMovementInput() {
      this.clearMovementInput = true;
   }

   public boolean shouldClearMovementInput() {
      return this.clearMovementInput;
   }

   public static void clearMovementInput(Input input) {
      InputCompat.setForward(input, 0.0F);
      InputCompat.setSideways(input, 0.0F);
      InputCompat.setJumping(input, false);
      InputCompat.setSneaking(input, false);
   }
}
