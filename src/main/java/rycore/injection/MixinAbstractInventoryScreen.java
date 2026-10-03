package rycore.injection;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.features.modules.Module;

@Mixin(StatusEffectsDisplay.class)
public abstract class MixinAbstractInventoryScreen {
   @Unique
   private static final float EFFECTS_ANIM_DURATION = 250.0F;
   @Unique
   private long effectsOpenTime;
   @Unique
   private boolean statusEffectsAnimPushed;

   @Inject(method = "render", at = @At("HEAD"))
   private void onDrawStatusEffectsHead(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
      if (!this.isSurvivalMode()) {
         this.statusEffectsAnimPushed = false;
      } else {
         if (this.effectsOpenTime == 0L) {
            this.effectsOpenTime = System.currentTimeMillis();
         }

         float progress = this.getProgress();
         if (progress < 1.0F) {
            float anim = this.getAnim(progress);
            int centerX = context.getScaledWindowWidth() / 2;
            int centerY = context.getScaledWindowHeight() / 2;
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(centerX, centerY);
            context.getMatrices().scale(anim, anim);
            context.getMatrices().translate(-centerX, -centerY);
            this.statusEffectsAnimPushed = true;
         } else {
            this.statusEffectsAnimPushed = false;
         }
      }
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void onDrawStatusEffectsTail(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
      if (this.statusEffectsAnimPushed) {
         context.getMatrices().popMatrix();
         this.statusEffectsAnimPushed = false;
      }
   }

   @Unique
   private float getProgress() {
      return Math.min(1.0F, (float)(System.currentTimeMillis() - this.effectsOpenTime) / 250.0F);
   }

   @Unique
   private float getAnim(float progress) {
      float x = progress - 1.0F;
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(x, 3.0) + c1 * (float)Math.pow(x, 2.0);
   }

   @Unique
   private boolean isSurvivalMode() {
      return Module.mc.interactionManager != null && Module.mc.interactionManager.getCurrentGameMode() == GameMode.SURVIVAL;
   }
}
