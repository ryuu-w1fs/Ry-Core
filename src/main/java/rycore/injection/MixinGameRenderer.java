package rycore.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rycore.utility.player.ItemCompat;
import rycore.Rycore;
import rycore.core.Managers;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.Module;
import rycore.features.modules.player.NoEntityTrace;
import rycore.utility.math.FrameRateCounter;
import rycore.utility.render.BlockAnimationUtility;
import rycore.utility.render.Render3DEngine;
import rycore.utility.render.shaders.satin.impl.ReloadableShaderEffectManager;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
   @Shadow
   private float viewDistanceBlocks;
   @Unique
   private boolean rycore$renderingHand;

   @Shadow
   public abstract void tick();

   @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", ordinal = 1, shift = Shift.BEFORE), method = "render")
   void postHudRenderHook(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
      FrameRateCounter.INSTANCE.recordFrame();
   }

   @Inject(
      method = "renderWorld",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;renderHand(FZLorg/joml/Matrix4f;)V",
         shift = Shift.BEFORE
      )
   )
   void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         Camera camera = Module.mc.gameRenderer.getCamera();
         MatrixStack matrixStack = new MatrixStack();
         RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
         matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
         matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
         Render3DEngine.lastProjMat.set(Module.mc.gameRenderer.getBasicProjectionMatrix(tickCounter.getTickProgress(true)));
         Render3DEngine.lastModMat.set(RenderSystem.getModelViewMatrix());
         Render3DEngine.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());
         Managers.MODULE.onRender3D(matrixStack);
         BlockAnimationUtility.onRender(matrixStack);
         Render3DEngine.onRender3D(matrixStack);
         RenderSystem.getModelViewStack().popMatrix();
      }
   }

   @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
   private float renderWorldHook(float delta, float first, float second) {
      return ModuleManager.noRender.isEnabled() && ModuleManager.noRender.nausea.getValue() ? 0.0F : MathHelper.lerp(delta, first, second);
   }

   // 1.21.11: GameRenderer.loadPrograms da bi xoa; shader tu tai qua
   // ShaderLoader nen khong con cho de reload satin.

   // 1.21.11: updateCrosshairTarget/findCrosshairTarget da chuyen khoi
   // GameRenderer (raycast nam trong Camera/MinecraftClient), nen FreeCam va
   // NoEntityTrace khong con hook duoc o day.

   @Unique
   private boolean shouldDisableEntityTrace() {
      if (ModuleManager.noEntityTrace.isEnabled() && Module.mc.player != null) {
         ItemStack mainStack = Module.mc.player.getMainHandStack();
         return mainStack.getItem() == Items.COBWEB && NoEntityTrace.cobweb.getValue()
            || ItemCompat.isPickaxe(mainStack) && NoEntityTrace.pickaxe.getValue()
            || ItemCompat.isAxe(mainStack) && NoEntityTrace.axe.getValue();
      } else {
         return false;
      }
   }

   @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
   private void getFovHook(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
      float fragZoom = ModuleManager.fragEffects.getMulCameraZoomValue();
      if (fragZoom != 1.0F) {
         cir.setReturnValue((Double)cir.getReturnValue() / fragZoom);
      }
   }

   @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
   public void getBasicProjectionMatrixHook(float fov, CallbackInfoReturnable<Matrix4f> cir) {
      if (ModuleManager.aspectRatio.isEnabled() && !this.rycore$renderingHand) {
         MatrixStack matrixStack = new MatrixStack();
         matrixStack.peek().getPositionMatrix().identity();
         float aspect = ModuleManager.aspectRatio.isEnabled()
            ? ModuleManager.aspectRatio.getRatioValue()
            : (float)Module.mc.getWindow().getFramebufferWidth() / Module.mc.getWindow().getFramebufferHeight();
         matrixStack.peek()
            .getPositionMatrix()
            .mul(new Matrix4f().setPerspective((float)(fov * (float) (Math.PI / 180.0)), aspect, 0.05F, this.viewDistanceBlocks * 4.0F));
         cir.setReturnValue(matrixStack.peek().getPositionMatrix());
      }
   }

   @Inject(method = "renderHand", at = @At("HEAD"))
   private void renderHandHead(float tickDelta, boolean bl, Matrix4f matrix4f, CallbackInfo ci) {
      this.rycore$renderingHand = true;
   }

   @Inject(method = "renderHand", at = @At("RETURN"))
   private void renderHandReturn(float tickDelta, boolean bl, Matrix4f matrix4f, CallbackInfo ci) {
      this.rycore$renderingHand = false;
   }

   @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
   private void bobViewHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.noBob.getValue()) {
            ci.cancel();
         } else {
            Rycore.core.bobView(matrices, tickDelta);
            ci.cancel();
         }
      }
   }

   @Unique
   private HitResult ensureTargetInRangeCustom(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
      Vec3d vec3d = hitResult.getPos();
      if (!vec3d.isInRange(cameraPos, interactionRange)) {
         Vec3d vec3d2 = hitResult.getPos();
         Direction direction = Direction.getFacing(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
         return BlockHitResult.createMissed(vec3d2, direction, BlockPos.ofFloored(vec3d2));
      } else {
         return hitResult;
      }
   }

   @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
   private void showFloatingItemHook(ItemStack floatingItem, CallbackInfo info) {
      if (ModuleManager.totemAnimation.isEnabled()) {
         ModuleManager.totemAnimation.showFloatingItem(floatingItem);
         info.cancel();
      }
   }

   // 1.21.11: GameRenderer.renderFloatingItem da bi xoa (hieu ung totem ve
   // trong InGameHud), nen TotemAnimation khong hook duoc o day.

   @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
   private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.hurtCam.getValue()) {
         ci.cancel();
      }
   }
}
