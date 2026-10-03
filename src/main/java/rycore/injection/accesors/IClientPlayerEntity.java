package rycore.injection.accesors;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface IClientPlayerEntity {
   @Invoker("sendMovementPackets")
   void iSendMovementPackets();

   @Accessor("lastYawClient")
   float getLastYaw();

   @Accessor("lastPitchClient")
   float getLastPitch();

   @Accessor("lastYawClient")
   void setLastYaw(float var1);

   @Accessor("lastPitchClient")
   void setLastPitch(float var1);

   @Accessor("mountJumpStrength")
   void setMountJumpStrength(float var1);
}
