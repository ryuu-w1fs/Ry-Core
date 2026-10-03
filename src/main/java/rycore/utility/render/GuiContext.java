package rycore.utility.render;

import net.minecraft.client.gui.DrawContext;

/**
 * Giu {@link DrawContext} cua frame hien tai.
 *
 * <p>Tang ve cua VCore nhan {@code MatrixStack} chu khong nhan DrawContext, nhung
 * tu ban moi moi thu phai di qua {@code GuiRenderState} nam trong DrawContext.
 * Thay vi doi chu ky cua hang tram ham ve, context duoc dat o day tai diem vao
 * (HUD render / Screen render) va tang ve doc lai khi can.
 *
 * <p>Chi dung tren render thread, trong mot frame - khong chia se giua cac thread.
 */
public final class GuiContext {
   private static DrawContext current;

   private GuiContext() {
   }

   public static void set(DrawContext context) {
      current = context;
   }

   public static DrawContext get() {
      return current;
   }

   public static boolean isAvailable() {
      return current != null;
   }

   public static void clear() {
      current = null;
   }
}
