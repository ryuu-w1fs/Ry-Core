package rycore.features.hud.impl;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import rycore.core.manager.client.ModuleManager;
import rycore.features.hud.HudElement;
import rycore.features.modules.misc.NameProtect;
import rycore.features.modules.render.HudEditor;
import rycore.gui.font.FontRenderers;
import rycore.setting.impl.PositionSetting;
import rycore.utility.math.FrameRateCounter;
import rycore.utility.render.Render2DEngine;
import rycore.utility.render.TextureStorage;

public class WaterMark extends HudElement {
   private static final String TITLE = "Release";
   private static final float TITLE_X = 15.0F;
   private static final float TITLE_RIGHT_PADDING = 3.0F;
   private static final float HEIGHT = 15.0F;
   private static final float GAP = 5.0F;
   private static final float RADIUS = 3.0F;

   public WaterMark(PositionSetting position, Supplier<List<HudElement>> activeElementsSupplier) {
      super("WaterMark", 100, 35, position, activeElementsSupplier);
   }

   @Override
   public void render(DrawContext context) {
      this.setPositionPixels(5.0F, 5.0F);
      super.render(context);
      String username = ModuleManager.nameProtect.isEnabled() ? NameProtect.getCustomName() : mc.getSession().getUsername();
      int fps = FrameRateCounter.INSTANCE.getFps();
      String fpsText = fps + " FPS";
      float usernameWidth = FontRenderers.sf_bold.getStringWidth(username);
      float fpsTextWidth = FontRenderers.sf_bold.getStringWidth(fpsText);
      float titleWidth = FontRenderers.sf_bold.getStringWidth("Release");
      float leftWidth = 15.0F + titleWidth + 3.0F;
      float rightX = this.getPosX() + leftWidth + 5.0F;
      float accountTextX = rightX + 13.0F;
      float fpsIconX = accountTextX + usernameWidth + 4.5F;
      float fpsTextX = fpsIconX + 12.0F;
      float rightWidth = Math.max(fpsTextX + fpsTextWidth - rightX + 6.0F, 45.0F);
      Render2DEngine.drawHudBase(context.getMatrices(), this.getPosX(), this.getPosY(), leftWidth, 15.0F, 3.0F);
      Render2DEngine.drawHudBase(context.getMatrices(), rightX, this.getPosY(), rightWidth, 15.0F, 3.0F);
      Render2DEngine.setupRender();
      Render2DEngine.drawRect(context.getMatrices(), this.getPosX() + 13.0F, this.getPosY() + 1.5F, 0.5F, 11.0F, new Color(1157627903, true));
      FontRenderers.sf_bold.drawGradientString(context.getMatrices(), "Release", this.getPosX() + 15.0F, this.getPosY() + 5.0F, 20);
      Render2DEngine.renderGradientTexture(
         context.getMatrices(),
         this.getPosX() + 1.0F,
         this.getPosY() + 2.0F,
         11.0,
         11.0,
         0.0F,
         0.0F,
         128.0,
         128.0,
         128.0,
         128.0,
         HudEditor.getColor(270),
         HudEditor.getColor(0),
         HudEditor.getColor(180),
         HudEditor.getColor(90)
      );
      Render2DEngine.renderGradientTexture(
         context.getMatrices(),
         rightX + 3.0F,
         this.getPosY() + 3.0F,
         8.0,
         8.0,
         0.0F,
         0.0F,
         128.0,
         128.0,
         128.0,
         128.0,
         HudEditor.getColor(270),
         HudEditor.getColor(0),
         HudEditor.getColor(180),
         HudEditor.getColor(90)
      );
      Render2DEngine.renderGradientTexture(
         context.getMatrices(),
         fpsIconX,
         this.getPosY() + 2.0F,
         10.0,
         10.0,
         0.0F,
         0.0F,
         128.0,
         128.0,
         128.0,
         128.0,
         HudEditor.getColor(270),
         HudEditor.getColor(0),
         HudEditor.getColor(180),
         HudEditor.getColor(90)
      );
      Render2DEngine.endRender();
      Render2DEngine.setupRender();
      FontRenderers.sf_bold.drawString(context.getMatrices(), username, accountTextX, this.getPosY() + 5.0F, HudEditor.getTextColor().getRGB());
      FontRenderers.sf_bold.drawString(context.getMatrices(), fpsText, fpsTextX, this.getPosY() + 5.0F, HudEditor.getTextColor().getRGB());
      Render2DEngine.endRender();
      this.setBounds(this.getPosX(), this.getPosY(), leftWidth + 5.0F + rightWidth, 15.0F);
   }

   @Override
   protected boolean isDraggable() {
      return false;
   }
}
