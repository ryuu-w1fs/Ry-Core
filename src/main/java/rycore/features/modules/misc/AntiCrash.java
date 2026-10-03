package rycore.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.jetbrains.annotations.NotNull;
import rycore.events.impl.PacketEvent;
import rycore.features.modules.Module;
import rycore.setting.Setting;
import rycore.utility.Timer;

public class AntiCrash extends Module {
   public final Setting<Boolean> debug = new Setting<>("Debug", false);
   private Timer debugTimer = new Timer();

   public AntiCrash() {
      super("AntiCrash", "Prevents game crashes.", Module.Category.MISC);
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.@NotNull Receive receive) {
      if (!(
         receive.getPacket() instanceof ExplosionS2CPacket exp && (exp.center().x > 1.0E9 || exp.center().y > 1.0E9 || exp.center().z > 1.0E9 || exp.radius() > 1.0E9)
      )) {
         if (!(
            receive.getPacket() instanceof ParticleS2CPacket p
               && (
                  p.getX() > 1.0E9
                     || p.getY() > 1.0E9
                     || p.getZ() > 1.0E9
                     || p.getSpeed() > 1.0E9
                     || p.getOffsetX() > 1.0E9
                     || p.getOffsetY() > 1.0E9
                     || p.getOffsetZ() > 1.0E9
               )
         )) {
            if (receive.getPacket() instanceof PlayerPositionLookS2CPacket pos
               && (pos.change().position().x > 1.0E9
                  || pos.change().position().y > 1.0E9
                  || pos.change().position().z > 1.0E9
                  || pos.change().yaw() > 1.0E9
                  || pos.change().pitch() > 1.0E9)) {
               if (this.debug.getValue() && this.debugTimer.passedMs(1000L)) {
                  this.sendMessage("PlayerPositionLookS2CPacket canceled");
                  this.debugTimer.reset();
               }

               receive.cancel();
            }
         } else {
            if (this.debug.getValue() && this.debugTimer.passedMs(1000L)) {
               this.sendMessage("ParticleS2CPacket canceled");
               this.debugTimer.reset();
            }

            receive.cancel();
         }
      } else {
         if (this.debug.getValue() && this.debugTimer.passedMs(1000L)) {
            this.sendMessage("ExplosionS2CPacket canceled");
            this.debugTimer.reset();
         }

         receive.cancel();
      }
   }
}
