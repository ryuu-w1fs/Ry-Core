package rycore.injection;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rycore.Rycore;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.EventAttack;
import rycore.events.impl.EventEatFood;
import rycore.events.impl.EventPlayerJump;
import rycore.events.impl.EventPlayerTravel;
import rycore.features.modules.Module;
import rycore.features.modules.movement.AutoSprint;
import rycore.features.modules.movement.Speed;

@Mixin(value = PlayerEntity.class, priority = 800)
public class MixinPlayerEntity {
   // 1.21.11: attack() khong con goi setSprinting(false) nen khong co diem INVOKE
   // de chen; TAIL la tuong duong gan nhat (ap lai sprint sau khi danh xong).
   @Inject(method = "attack", at = @At("TAIL"))
   public void attackAHook(CallbackInfo callbackInfo) {
      if (ModuleManager.autoSprint.isEnabled() && AutoSprint.sprint.getValue()) {
         float multiplier = 0.6F + 0.4F * AutoSprint.motion.getValue();
         Module.mc
            .player
            .setVelocity(
               Module.mc.player.getVelocity().x / 0.6 * multiplier, Module.mc.player.getVelocity().y, Module.mc.player.getVelocity().z / 0.6 * multiplier
            );
         Module.mc.player.setSprinting(true);
      }
   }

   @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
   public void getMovementSpeedHook(CallbackInfoReturnable<Float> cir) {
      if (ModuleManager.speed.isEnabled() && ModuleManager.speed.mode.is(Speed.Mode.Vanilla)) {
         cir.setReturnValue(ModuleManager.speed.boostFactor.getValue());
      }
   }

   @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
   private void attackAHook2(Entity target, CallbackInfo ci) {
      EventAttack event = new EventAttack(target, false);
      Rycore.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
   private void onTravelhookPre(Vec3d movementInput, CallbackInfo ci) {
      if (Module.mc.player != null) {
         EventPlayerTravel event = new EventPlayerTravel(movementInput, true);
         Rycore.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            Module.mc.player.move(MovementType.SELF, Module.mc.player.getVelocity());
            ci.cancel();
         }
      }
   }

   @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
   private void onTravelhookPost(Vec3d movementInput, CallbackInfo ci) {
      if (Module.mc.player != null) {
         EventPlayerTravel event = new EventPlayerTravel(movementInput, false);
         Rycore.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            Module.mc.player.move(MovementType.SELF, Module.mc.player.getVelocity());
            ci.cancel();
         }
      }
   }

   // 1.21.11: jump() khong con duoc override trong PlayerEntity;
   // hai hook da chuyen sang MixinEntityLiving.

   // 1.21.11: PlayerEntity.eatFood da bi xoa; viec an item di qua
   // ConsumableComponent.

   @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
   public void getBlockInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
      if (ModuleManager.reach.isEnabled()) {
         if (ModuleManager.reach.Creative.getValue() && Module.mc.player.isCreative()) {
            cir.setReturnValue((double)ModuleManager.reach.creativeBlocksRange.getValue().floatValue());
         } else {
            cir.setReturnValue((double)ModuleManager.reach.blocksRange.getValue().floatValue());
         }
      }
   }

   @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
   public void getEntityInteractionRangeHook(CallbackInfoReturnable<Double> cir) {
      if (ModuleManager.reach.isEnabled()) {
         if (ModuleManager.reach.Creative.getValue() && Module.mc.player.isCreative()) {
            cir.setReturnValue((double)ModuleManager.reach.creativeEntityRange.getValue().floatValue());
         } else {
            cir.setReturnValue((double)ModuleManager.reach.entityRange.getValue().floatValue());
         }
      }
   }
}
