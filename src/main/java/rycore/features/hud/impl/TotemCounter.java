package rycore.features.hud.impl;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import rycore.events.impl.TotemPopEvent;
import rycore.features.hud.HudElement;
import rycore.gui.font.FontRenderers;
import rycore.setting.impl.PositionSetting;
import rycore.utility.render.Render2DEngine;

public class TotemCounter extends HudElement {
   private float angle;

   public TotemCounter(PositionSetting position, Supplier<List<HudElement>> activeElementsSupplier) {
      super("TotemCounter", 0, 0, position, activeElementsSupplier);
   }

   @Override
   public void render(DrawContext context) {
      super.render(context);
      int count = this.getItemCount(Items.TOTEM_OF_UNDYING);
      if (count != 0) {
         int width = mc.getWindow().getScaledWidth();
         int height = mc.getWindow().getScaledHeight();
         int u = mc.player.getMaxAir();
         int v = Math.min(mc.player.getAir(), u);
         int adjustment = !mc.player.isSubmergedInWater() && v >= u ? 0 : 10;
         float factor = Math.abs(this.angle < 0.0F ? this.angle / 15.0F : 0.0F);
         int center = width / 2;
         int y = height - 55 - adjustment;
         int x = center - 8;
         context.drawItem(Items.TOTEM_OF_UNDYING.getDefaultStack(), x, y);
         context.drawStackOverlay(mc.textRenderer, Items.TOTEM_OF_UNDYING.getDefaultStack(), x, y);
         if (factor > 0.0F) {
            Render2DEngine.drawBlurredShadow(
               context.getMatrices(), x - 6, y - 5, 11.0F, 11.0F, 8, Render2DEngine.injectAlpha(new Color(16711680), (int)(255.0F * factor))
            );
         }

         String sCount = count + "";
         float textX = center - FontRenderers.sf_bold.getStringWidth(sCount) / 2.0F;
         float textY = y - 6;
         FontRenderers.sf_bold.drawString(context.getMatrices(), sCount, textX, textY, -1);
      }
   }

   public void onTotemPop(TotemPopEvent e) {
      if (e.getEntity() == mc.player) {
         this.angle = -15.0F;
      }
   }

   @Override
   public void tick() {
      if (this.angle < 0.0F) {
         this.angle++;
      }
   }

   public int getItemCount(Item item) {
      if (mc.player == null) {
         return 0;
      }

      int n = 0;
      int n2 = 44;

      for (int i = 0; i <= n2; i++) {
         ItemStack itemStack = mc.player.getInventory().getStack(i);
         if (itemStack.getItem() == item) {
            n += itemStack.getCount();
         }
      }

      return n;
   }
}
