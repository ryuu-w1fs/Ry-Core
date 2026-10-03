package rycore.injection;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rycore.events.impl.EventPlayerJump;import rycore.Rycore;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.EventTravel;
import rycore.features.modules.Module;
import rycore.features.modules.combat.Aura;
import rycore.features.modules.movement.WaterSpeed;
import rycore.utility.interfaces.IEntityLiving;

@Mixin(LivingEntity.class)
public class MixinEntityLiving implements IEntityLiving {
   @Unique
   double prevServerX;
   @Unique
   double prevServerY;
   @Unique
   double prevServerZ;
   @Unique
   public List<Aura.Position> positonHistory = new ArrayList<>();
   @Unique
   private boolean prevFlying = false;

   @Override
   public List<Aura.Position> getPositionHistory() {
      return this.positonHistory;
   }

   @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
   private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> info) {
      if ((LivingEntity)(Object)this == Module.mc.player && ModuleManager.animations.shouldChangeAnimationDuration()) {
         info.setReturnValue(ModuleManager.animations.getHandSwingDuration());
      }
   }

   // 1.21.11: updateTrackedPositionAndAngles la final tren Entity (khong phai
   // LivingEntity) nen khong inject duoc; dung tick() thay the.
   @Inject(method = "tick", at = @At("HEAD"))
   private void updateTrackedPositionAndAnglesHook(CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         // Vi tri server truoc do: doc tu TrackedPosition thay cho serverX/Y/Z cu.
         Vec3d tracked = ((Entity)(Object)this).getTrackedPosition().getPos();
         this.prevServerX = tracked.x;
         this.prevServerY = tracked.y;
         this.prevServerZ = tracked.z;
         this.positonHistory.add(new Aura.Position(tracked.x, tracked.y, tracked.z));
         this.positonHistory.removeIf(Aura.Position::shouldRemove);
      }
   }

   @Override
   public double getPrevServerX() {
      return this.prevServerX;
   }

   @Override
   public double getPrevServerY() {
      return this.prevServerY;
   }

   @Override
   public double getPrevServerZ() {
      return this.prevServerZ;
   }

   @Inject(method = "isGliding", at = @At("TAIL"), cancellable = true)
   public void isFallFlyingHook(CallbackInfoReturnable<Boolean> cir) {
   }

   @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
   public void travelHook(Vec3d movementInput, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         if ((LivingEntity)(Object)this == Module.mc.player) {
            EventTravel event = new EventTravel(Module.mc.player.getVelocity(), true);
            Rycore.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               Module.mc.player.move(MovementType.SELF, event.getmVec());
               ci.cancel();
            }
         }
      }
   }

   @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
   public void travelPostHook(Vec3d movementInput, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         if ((LivingEntity)(Object)this == Module.mc.player) {
            EventTravel event = new EventTravel(movementInput, false);
            Rycore.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               Module.mc.player.move(MovementType.SELF, Module.mc.player.getVelocity());
               ci.cancel();
            }
         }
      }
   }

   @ModifyVariable(method = "setSprinting", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private boolean setSprintingHook(boolean sprinting) {
      return Module.mc.player == null
            || Module.mc.world == null
            || !ModuleManager.waterSpeed.isEnabled()
            || !ModuleManager.waterSpeed.mode.is(WaterSpeed.Mode.CancelResurface)
            || !Module.mc.player.isTouchingWater()
               && !(Module.mc.world.getBlockState(BlockPos.ofFloored(Module.mc.player.getEntityPos().add(0.0, -0.5, 0.0))).getBlock() instanceof FluidBlock)
         ? sprinting
         : true;
   }

   @Inject(method = "jump", at = @At("HEAD"))
   private void onJumpPre(CallbackInfo ci) {
      // jump() gio o LivingEntity nen phai loc rieng player cua client.
      if ((Object)this == Module.mc.player) {
         Rycore.EVENT_BUS.post(new EventPlayerJump(true));
      }
   }

   @Inject(method = "jump", at = @At("RETURN"))
   private void onJumpPost(CallbackInfo ci) {
      if ((Object)this == Module.mc.player) {
         Rycore.EVENT_BUS.post(new EventPlayerJump(false));
      }
   }
}
