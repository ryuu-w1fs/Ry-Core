package rycore.injection.accesors;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Tu 1.21.2 packet khong con ba field int velocityX/Y/Z ma gop thanh mot
 * {@link Vec3d} duy nhat, tinh theo don vi block/tick (truoc day la 1/8000 block).
 */
@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface ISPacketEntityVelocity {
   @Mutable
   @Accessor("velocity")
   void setVelocity(Vec3d velocity);

   @Accessor("velocity")
   Vec3d getVelocityVec();
}
