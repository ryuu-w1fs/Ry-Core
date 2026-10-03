package rycore.injection;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rycore.Rycore;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.EventEntityRemoved;
import rycore.events.impl.EventEntitySpawn;
import rycore.events.impl.EventEntitySpawnPost;
import rycore.features.modules.Module;
import rycore.features.modules.render.WorldTweaks;
import rycore.setting.impl.ColorSetting;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
   @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
   public void addEntityHook(Entity entity, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventEntitySpawn ees = new EventEntitySpawn(entity);
         Rycore.EVENT_BUS.post(ees);
         if (ees.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "addEntity", at = @At("RETURN"), cancellable = true)
   public void addEntityHookPost(Entity entity, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventEntitySpawnPost ees = new EventEntitySpawnPost(entity);
         Rycore.EVENT_BUS.post(ees);
         if (ees.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "removeEntity", at = @At("HEAD"))
   public void removeEntityHook(int entityId, RemovalReason removalReason, CallbackInfo ci) {
      if (!Module.fullNullCheck()) {
         EventEntityRemoved eer = new EventEntityRemoved(Module.mc.world.getEntityById(entityId));
         Rycore.EVENT_BUS.post(eer);
      }
   }

   // ClientWorld.getSkyColor() da bi xoa (mau troi tinh trong
   // DimensionEffects/SkyRendering), nen WorldTweaks khong doi duoc mau suong mu.
}
