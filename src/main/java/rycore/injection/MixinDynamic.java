package rycore.injection;

import net.minecraft.client.render.RenderTickCounter.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rycore.Rycore;

/**
 * Nhan nhip tick de dieu khien toc do game (Timer, FragEffects).
 *
 * <p>1.21.11 doi ten cac field: {@code lastFrameDuration} -> {@code dynamicDeltaTicks},
 * {@code tickDelta} -> {@code tickProgress}, {@code prevTimeMillis} -> {@code lastTimeMillis};
 * {@code beginRenderTick} cung nhan them tham so {@code paused}.
 */
@Mixin(Dynamic.class)
public class MixinDynamic {
   @Shadow
   private float dynamicDeltaTicks;
   @Shadow
   private float tickProgress;
   @Shadow
   private long lastTimeMillis;
   @Final
   @Shadow
   private float tickTime;

   @Inject(method = "beginRenderTick(JZ)I", at = @At("HEAD"), cancellable = true)
   private void beginRenderTickHook(long timeMillis, boolean paused, CallbackInfoReturnable<Integer> cir) {
      float timerMultiplier = Rycore.TICK_TIMER * Rycore.FRAG_EFFECT_TIMER;
      if (timerMultiplier != 1.0F) {
         this.dynamicDeltaTicks = (float)(timeMillis - this.lastTimeMillis) / this.tickTime * timerMultiplier;
         this.lastTimeMillis = timeMillis;
         this.tickProgress = this.tickProgress + this.dynamicDeltaTicks;
         int i = (int)this.tickProgress;
         this.tickProgress -= i;
         cir.setReturnValue(i);
      }
   }
}
