package rycore.utility.render;

import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

/**
 * Dung {@link PlayerEntityRenderState} tu mot {@link PlayerEntity}.
 *
 * <p>Tu 1.21.2 cac model entity khong con nhan truc tiep entity: {@code setAngles} va
 * {@code animateModel} doc tu mot doi tuong RenderState duoc entity renderer dien san
 * moi khung hinh. RyCore ve model nguoi choi ngoai luong render cua vanilla (PopEffect,
 * CustomModel) nen phai tu dien state nay.
 */
public final class RenderStateCompat {
   private RenderStateCompat() {
   }

   /** Dien cac truong ma PlayerEntityModel can de dat goc cac bo phan. */
   public static PlayerEntityRenderState of(PlayerEntity entity, float tickDelta) {
      PlayerEntityRenderState state = new PlayerEntityRenderState();
      fill(state, entity, tickDelta);
      return state;
   }

   public static void fill(PlayerEntityRenderState state, PlayerEntity entity, float tickDelta) {
      state.bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.lastBodyYaw, entity.bodyYaw);
      state.relativeHeadYaw = MathHelper.lerpAngleDegrees(tickDelta, entity.lastHeadYaw, entity.headYaw) - state.bodyYaw;
      state.pitch = entity.getPitch(tickDelta);
      state.age = entity.age + tickDelta;

      state.limbSwingAnimationProgress = entity.limbAnimator.getAnimationProgress(tickDelta);
      state.limbSwingAmplitude = Math.min(entity.limbAnimator.getAmplitude(tickDelta), 1.0F);
      state.limbAmplitudeInverse = 1.0F - state.limbSwingAmplitude;

      state.pose = entity.getPose();
      state.isInSneakingPose = entity.isInSneakingPose();
      state.isGliding = entity.isGliding();
      state.isSwimming = entity.isSwimming();
      state.hasVehicle = entity.hasVehicle();
      state.isUsingItem = entity.isUsingItem();
      state.activeHand = entity.getActiveHand();
      state.preferredArm = entity.getMainArm() == Arm.RIGHT ? Arm.RIGHT : Arm.LEFT;
      state.touchingWater = entity.isTouchingWater();
      state.baby = entity.isBaby();
      state.invisible = entity.isInvisible();

      state.handSwingProgress = entity.getHandSwingProgress(tickDelta);
      state.leaningPitch = entity.getLeaningPitch(tickDelta);

      // Cac lop ngoai (ao/quan) do PopEffect tu bat tat, mac dinh tat de khop
      // hanh vi setOuterLayerVisible(model, false).
      state.hatVisible = true;
      state.jacketVisible = false;
      state.leftSleeveVisible = false;
      state.rightSleeveVisible = false;
      state.leftPantsLegVisible = false;
      state.rightPantsLegVisible = false;
      state.capeVisible = false;
   }
}
