package rycore.utility.player;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;

/**
 * Doc/ghi input di chuyen cua nguoi choi.
 *
 * <p>Tu 1.21.2 lop {@link Input} khong con cac field {@code movementForward},
 * {@code movementSideways}, {@code jumping}, {@code sneaking}. Thay vao do co
 * {@code movementVector} (Vec2f: x = sideways, y = forward) va {@code playerInput}
 * (record {@link PlayerInput} gom cac co nhan phim). Lop nay giu nguyen cach dung
 * cu de phan con lai cua RyCore khong phai sua theo.
 */
public final class InputCompat {
   private InputCompat() {
   }

   public static float getForward(Input input) {
      return input.movementVector.y;
   }

   public static float getSideways(Input input) {
      return input.movementVector.x;
   }

   public static void setForward(Input input, float forward) {
      input.movementVector = new Vec2f(input.movementVector.x, forward);
   }

   public static void setSideways(Input input, float sideways) {
      input.movementVector = new Vec2f(sideways, input.movementVector.y);
   }

   public static void setMovement(Input input, float sideways, float forward) {
      input.movementVector = new Vec2f(sideways, forward);
   }

   public static boolean isJumping(Input input) {
      return input.playerInput.jump();
   }

   public static boolean isSneaking(Input input) {
      return input.playerInput.sneak();
   }

   /**
    * PlayerInput la record nen moi co phai dung lai ca bo; cac helper duoi day chi
    * thay dung mot co va giu nguyen phan con lai.
    */
   public static void setJumping(Input input, boolean jumping) {
      PlayerInput p = input.playerInput;
      input.playerInput = new PlayerInput(p.forward(), p.backward(), p.left(), p.right(), jumping, p.sneak(), p.sprint());
   }

   public static void setSneaking(Input input, boolean sneaking) {
      PlayerInput p = input.playerInput;
      input.playerInput = new PlayerInput(p.forward(), p.backward(), p.left(), p.right(), p.jump(), sneaking, p.sprint());
   }
}
