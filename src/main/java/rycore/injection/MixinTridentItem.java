package rycore.injection;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rycore.Rycore;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.UseTridentEvent;

@Mixin(TridentItem.class)
public abstract class MixinTridentItem {
   @Inject(method = "onStoppedUsing", at = @At("HEAD"), cancellable = true)
   public void onStoppedUsingHook(
      ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> cir
   ) {
      if (user == Rycore.mc.player && EnchantmentHelper.getTridentSpinAttackStrength(stack, Rycore.mc.player) > 0.0F) {
         UseTridentEvent e = new UseTridentEvent();
         Rycore.EVENT_BUS.post(e);
         if (e.isCancelled()) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(method = "use", at = @At("HEAD"), cancellable = true)
   public void useHook(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, user) > 0.0F
         && !user.isTouchingWaterOrRain()
         && ModuleManager.tridentBoost.isEnabled()
         && ModuleManager.tridentBoost.anyWeather.getValue()) {
         user.setCurrentHand(hand);
         cir.setReturnValue(ActionResult.CONSUME);
      }
   }
}
