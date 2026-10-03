package rycore.injection;

import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.IManager;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.movement.AntiWeb;
import rycore.utility.player.InteractionUtility;

@Mixin(CobwebBlock.class)
public class MixinCobwebBlock {
   @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
   public void onEntityCollisionHook(
      BlockState state,
      World world,
      BlockPos pos,
      Entity entity,
      net.minecraft.entity.EntityCollisionHandler handler,
      boolean bl,
      CallbackInfo ci
   ) {
      if (ModuleManager.antiWeb.isEnabled() && entity == IManager.mc.player) {
         if (AntiWeb.mode.getValue() == AntiWeb.Mode.Ignore) {
            ci.cancel();
            if (AntiWeb.grim.getValue()) {
               InteractionUtility.sendSequencedPacket(id -> new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, pos, Direction.UP, id));
            }
         }
      }
   }
}
