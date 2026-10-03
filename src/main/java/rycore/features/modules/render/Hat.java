package rycore.features.modules.render;

import java.awt.Color;
import net.minecraft.client.render.BufferBuilder;
import rycore.utility.render.RenderCompat;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.Module;
import rycore.setting.Setting;
import rycore.setting.impl.ColorSetting;
import rycore.utility.render.Render2DEngine;

public class Hat extends Module {
   public Setting<Boolean> autoOffset = new Setting<>("AutoOffset", false);
   public Setting<Boolean> syncColor = new Setting<>("SyncColor", true);
   public Setting<ColorSetting> color = new Setting<>(
      "Color", new ColorSetting(new Color(10, 255, 135, 255).getRGB()).withoutAlpha(), v -> !this.syncColor.getValue()
   );
   public static MatrixStack matrixStack;

   public Hat() {
      super("Hat", "Renders a cosmetic hat above players.", Module.Category.RENDER);
   }

   public void renderHat(MatrixStack stack) {
      if (mc.player != null) {
         Matrix4f modelMatrix = stack.peek().getPositionMatrix();
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
         float radius = 0.6F;
         int steps = 72;
         double angleStep = (Math.PI * 2) / steps;
         Color customHatColor = this.color.getValue().getColorObject();
         int apexA = 255;
         int baseA = 120;

         for (int i = 0; i < steps; i++) {
            float x1 = (float)(Math.cos(i * angleStep) * radius);
            float z1 = (float)(Math.sin(i * angleStep) * radius);
            float x2 = (float)(Math.cos((i + 1) * angleStep) * radius);
            float z2 = (float)(Math.sin((i + 1) * angleStep) * radius);
            Color apexColor = this.getHatColor((i + 0.5F) / steps, customHatColor);
            Color baseColor1 = this.getHatColor((float)i / steps, customHatColor);
            Color baseColor2 = this.getHatColor((float)(i + 1) / steps, customHatColor);
            buffer.vertex(modelMatrix, 0.0F, 0.3F, 0.0F).color(apexColor.getRed(), apexColor.getGreen(), apexColor.getBlue(), apexA);
            buffer.vertex(modelMatrix, x1, 0.0F, z1).color(baseColor1.getRed(), baseColor1.getGreen(), baseColor1.getBlue(), baseA);
            buffer.vertex(modelMatrix, x2, 0.0F, z2).color(baseColor2.getRed(), baseColor2.getGreen(), baseColor2.getBlue(), baseA);
         }

         RenderCompat.draw(buffer.end());
         buffer = tessellator.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

         for (int i = 0; i <= steps; i++) {
            float x = (float)(Math.cos(i * angleStep) * radius);
            float z = (float)(Math.sin(i * angleStep) * radius);
            Color rimColor = this.getHatColor((float)i / steps, customHatColor);
            buffer.vertex(modelMatrix, x, 0.0F, z).color(rimColor.getRed(), rimColor.getGreen(), rimColor.getBlue(), apexA);
         }

         RenderCompat.draw(buffer.end());
      }
   }

   public static float getYOffset(Entity entity) {
      float offset = -0.41F;
      if (!Boolean.TRUE.equals(ModuleManager.hat.autoOffset.getValue())) {
         return offset;
      }

      if (entity instanceof LivingEntity livingEntity && !livingEntity.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
         offset -= 0.071F;
      }

      return offset;
   }

   private Color getHatColor(float fraction, Color fallback) {
      if (!this.syncColor.getValue()) {
         return fallback;
      }

      HudEditor.Theme theme = HudEditor.getCurrentTheme();
      float normalized = fraction - (float)Math.floor(fraction);
      if (normalized < 0.0F) {
         normalized++;
      }

      double count = normalized * 360.0F;
      return Render2DEngine.TwoColoreffect(theme.color1, theme.color2, 15.0, count);
   }
}
