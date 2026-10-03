package rycore.features.hud.impl;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import rycore.Rycore;
import rycore.core.Managers;
import rycore.features.hud.HudElement;
import rycore.features.modules.render.HudEditor;
import rycore.gui.font.FontRenderers;
import rycore.setting.impl.PositionSetting;
import rycore.utility.math.MathUtility;
import rycore.utility.render.Render2DEngine;
import rycore.utility.render.TextureStorage;

public class Speedometer extends HudElement {
   public float speed = 0.0F;

   public Speedometer(PositionSetting position, Supplier<List<HudElement>> activeElementsSupplier) {
      super("Speed", 50, 10, position, activeElementsSupplier);
   }

   @Override
   public void render(DrawContext context) {
      super.render(context);
      String str = "BPS " + Formatting.WHITE + MathUtility.round(this.getSpeedBps() * Rycore.TICK_TIMER);
      float width = Math.max(42.0F, FontRenderers.getModulesRenderer().getStringWidth(str) + 21.0F);
      float pX = this.getPosX() > mc.getWindow().getScaledWidth() / 2.0F ? this.getPosX() - width : this.getPosX();
      Render2DEngine.drawHudBase(context.getMatrices(), pX, this.getPosY(), width, 13.0F, 3.0F);
      Render2DEngine.drawRect(context.getMatrices(), pX + 14.0F, this.getPosY() + 2.0F, 0.5F, 8.0F, new Color(1157627903, true));
      Render2DEngine.setupRender();
      Render2DEngine.renderGradientTexture(
         context.getMatrices(),
         pX + 2.0F,
         this.getPosY() + 1.0F,
         10.0,
         10.0,
         0.0F,
         0.0F,
         512.0,
         512.0,
         512.0,
         512.0,
         HudEditor.getColor(270),
         HudEditor.getColor(0),
         HudEditor.getColor(180),
         HudEditor.getColor(90)
      );
      Render2DEngine.endRender();
      FontRenderers.getModulesRenderer().drawString(context.getMatrices(), str, pX + 18.0F, this.getPosY() + 5.0F, HudEditor.getColor(1).getRGB());
      this.setBounds(pX, this.getPosY(), width, 13.0F);
   }

   public float getSpeedBps() {
      return Managers.PLAYER.currentPlayerSpeed * 20.0F;
   }
}
