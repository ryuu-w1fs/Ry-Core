package rycore.features.modules.render;

import com.google.common.collect.Lists;
import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import rycore.core.Managers;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.Module;
import rycore.features.modules.combat.AntiBot;
import rycore.utility.math.MathUtility;
import rycore.utility.render.Render2DEngine;
import rycore.utility.render.Render3DEngine;
import rycore.utility.render.animation.AnimationUtility;

public class Arrows extends Module {
   private static final boolean GLOW = false;
   private static final float HEIGHT = 1.2F;
   private static final boolean DOWN = true;
   private static final float DOWN_HEIGHT = 3.63F;
   private static final float TRACER_WIDTH = 0.0F;
   private static final int TRACER_RADIUS = 80;
   private static final int PITCH_LOCK = 42;
   private static final Color FRIEND_COLOR = new Color(59392);
   private static final Color TRACER_COLOR = new Color(16711680);
   private float smoothYaw = 0.0F;

   public Arrows() {
      super("Arrows", "Directional arrows to other players.", Module.Category.RENDER);
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (!fullNullCheck()) {
         float middleW = ModuleManager.crosshair.getAnimatedPosX();
         float middleH = ModuleManager.crosshair.getAnimatedPosY();
         int color = 0;
         context.getMatrices().pushMatrix();
         context.getMatrices().translate(middleW, middleH);
         // Matrix3x2fStack la ma tran affine 2D, chi xoay quanh truc Z duoc.
         // Phep nghieng quanh truc X theo pitch khong con bieu dien duoc o tang GUI.
         context.getMatrices().translate(-middleW, -middleH);
         this.smoothYaw = AnimationUtility.fast(this.smoothYaw, mc.player.getYaw(), 13.0F);

         for (PlayerEntity e : Lists.newArrayList(mc.world.getPlayers())) {
            if (e != mc.player && (!ModuleManager.antiBot.isEnabled() || ModuleManager.antiBot.mode.getValue() != AntiBot.Mode.Matrix || !AntiBot.isBot(e))) {
               context.getMatrices().pushMatrix();
               float yaw = this.getRotations(e) - this.smoothYaw;
               context.getMatrices().translate(middleW, middleH);
               context.getMatrices().rotate((float)Math.toRadians(yaw));
               context.getMatrices().translate(-middleW, -middleH);
               if (Managers.FRIEND.isFriend(e)) {
                  color = FRIEND_COLOR.getRGB();
               } else {
                  color = TRACER_COLOR.getRGB();
               }

               Render2DEngine.drawTracerPointer(context.getMatrices(), middleW, middleH - 80.0F, 6.0F, 0.0F, 3.63F, true, false, color);
               context.getMatrices().translate(middleW, middleH);
               context.getMatrices().rotate((float)Math.toRadians(-yaw));
               context.getMatrices().translate(-middleW, -middleH);
               context.getMatrices().popMatrix();
            }
         }

         context.getMatrices().popMatrix();
      }
   }

   private float getRotations(PlayerEntity entity) {
      if (mc.player == null) {
         return 0.0F;
      }

      double x = this.interp(entity.getEntityPos().x, entity.lastX) - this.interp(mc.player.getEntityPos().x, mc.player.lastX);
      double z = this.interp(entity.getEntityPos().z, entity.lastZ) - this.interp(mc.player.getEntityPos().z, mc.player.lastZ);
      return (float)(-(Math.atan2(x, z) * (180.0 / Math.PI)));
   }

   private double interp(double current, double previous) {
      return previous + (current - previous) * Render3DEngine.getTickDelta();
   }
}
