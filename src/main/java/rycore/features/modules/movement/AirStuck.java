package rycore.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import rycore.events.impl.EventTick;
import rycore.events.impl.PacketEvent;
import rycore.features.modules.Module;

import rycore.utility.player.InputCompat;

public class AirStuck extends Module {
   private static final boolean CANCEL_PACKETS = true;
   private Vec3d freezePos = Vec3d.ZERO;

   public AirStuck() {
      super("AirStuck", "Freezes the player in mid-air, preventing movement.", Module.Category.MOVEMENT);
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.freezePos = mc.player.getEntityPos();
      }
   }

   @Override
   public void onDisable() {
      this.freezePos = Vec3d.ZERO;
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (!fullNullCheck() && this.freezePos != Vec3d.ZERO) {
         ClientPlayerEntity player = mc.player;
         if (player != null) {
            player.setVelocity(0.0, 0.0, 0.0);
            if (player.input != null) {
               InputCompat.setForward(player.input, 0.0F);
               InputCompat.setSideways(player.input, 0.0F);
            }

            player.setPos(player.getX(), this.freezePos.y, player.getZ());
         }
      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send e) {
      if (!fullNullCheck()) {
         if (e.getPacket() instanceof PlayerMoveC2SPacket) {
            e.cancel();
         }
      }
   }
}
