package rycore.features.modules.player;

import java.awt.Color;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import rycore.utility.player.ItemCompat;
import rycore.core.Managers;
import rycore.features.modules.Module;
import rycore.gui.font.FontRenderers;
import rycore.setting.Setting;
import rycore.utility.Timer;
import rycore.utility.render.TextureStorage;

public class DurabilityAlert extends Module {
   private final Setting<Boolean> friends = new Setting<>("Friend message", false);
   private final Setting<Integer> percent = new Setting<>("Percent", 20, 1, 100);
   private static final Color WARNING_COLOR = new Color(255, 92, 92);
   private static final int ICON_RENDER_SIZE = 50;
   private static final int ICON_TEXTURE_SIZE = 200;
   private boolean need_alert = false;
   private final Timer timer = new Timer();

   public DurabilityAlert() {
      super("DurabilityAlert", "Alerts when armor is low.", Module.Category.PLAYER);
   }

   @Override
   public void onUpdate() {
      if (this.friends.getValue()) {
         for (PlayerEntity player : mc.world.getPlayers()) {
            if (Managers.FRIEND.isFriend(player) && player != mc.player) {
               for (ItemStack stack : ItemCompat.getArmorItems(player)) {
                  if (!stack.isEmpty() && ItemCompat.isArmor(stack) && getDurability(stack) < this.percent.getValue() && this.timer.passedMs(30000L)
                     )
                   {
                     mc.player.networkHandler.sendChatCommand("msg " + player.getName().getString() + " Your armor is about to break!");
                     this.timer.reset();
                  }
               }
            }
         }
      }

      boolean flag = false;

      for (ItemStack stack : ItemCompat.getArmorItems(mc.player)) {
         if (!stack.isEmpty() && ItemCompat.isArmor(stack) && getDurability(stack) < this.percent.getValue()) {
            this.need_alert = true;
            flag = true;
         }
      }

      if (!flag && this.need_alert) {
         this.need_alert = false;
      }
   }

   @Override
   public void onRender2D(DrawContext context) {
      if (this.need_alert) {
         FontRenderers.sf_bold
            .drawCenteredString(
               context.getMatrices(),
               "Armor about to break!",
               mc.getWindow().getScaledWidth() / 2.0F,
               mc.getWindow().getScaledHeight() / 3.0F - 60.0F,
               WARNING_COLOR.getRGB()
            );
         context.drawTexture(
            RenderPipelines.GUI_TEXTURED, TextureStorage.brokenShield,
            (int)(mc.getWindow().getScaledWidth() / 2.0F - 25.0F),
            (int)(mc.getWindow().getScaledHeight() / 3.0F - 120.0F),
            0.0F,
            0.0F,
            50,
            50,
            200,
            200
         );
      }
   }

   public static int getDurability(ItemStack stack) {
      return (int)((stack.getMaxDamage() - stack.getDamage()) / Math.max(0.1, stack.getMaxDamage()) * 100.0);
   }
}
