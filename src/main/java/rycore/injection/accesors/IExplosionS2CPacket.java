package rycore.injection.accesors;

import java.util.Optional;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * tu ban moi ExplosionS2CPacket la record: ba field playerVelocityX/Y/Z duoc gop
 * thanh {@code playerKnockback} dang {@code Optional<Vec3d>} (rong khi khong co day lui).
 */
@Mixin(ExplosionS2CPacket.class)
public interface IExplosionS2CPacket {
   @Mutable
   @Accessor("playerKnockback")
   void setPlayerKnockback(Optional<Vec3d> knockback);

   @Accessor("playerKnockback")
   Optional<Vec3d> getPlayerKnockback();
}
