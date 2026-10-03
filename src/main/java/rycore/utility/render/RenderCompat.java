package rycore.utility.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lop tuong thich cho tang render 3D (ve trong world).
 *
 * <p>Tu 1.21.2 toan bo GPU state dong da bi bo: {@code RenderSystem.enableBlend()},
 * {@code blendFunc()}, {@code depthMask()}, {@code disableCull()} khong con ton tai.
 * Blend, depth va cull gio duoc nuong cung vao tung RenderPipeline dung san.
 *
 * <p>Quan trong: {@code RenderLayer.draw()} yeu cau vertex format cua buffer PHAI
 * khop format cua pipeline, neu khong se ve sai mau hoac nem exception.
 * {@code debugQuads()} dung {@link VertexFormats#POSITION} (khong co kenh mau) nen
 * moi quad co mau ve ra trang - do la ly do khoi ESP/HitBox bi trang.
 * {@code debugTriangleFan()} moi la layer QUADS dung
 * {@link VertexFormats#POSITION_COLOR}.
 */
public final class RenderCompat {
   private static final Logger LOGGER = LoggerFactory.getLogger("rycore-render");

   /** Chi canh bao mot lan moi to hop, tranh spam log moi frame. */
   private static final java.util.Set<String> WARNED = java.util.concurrent.ConcurrentHashMap.newKeySet();

   /** Blend cong don: SRC_ALPHA, ONE - dung cho glow, particle, ESP phat sang. */
   public static final BlendFunction ADDITIVE_ALPHA = new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE);

   /** Blend alpha thong thuong: SRC_ALPHA, ONE_MINUS_SRC_ALPHA. */
   public static final BlendFunction NORMAL_ALPHA = BlendFunction.TRANSLUCENT;

   private RenderCompat() {
   }

   /**
    * Ve mot buffer da build, chon RenderLayer khop ca DrawMode va vertex format.
    *
    * <p>Bao ve chong crash: mot to hop khong co layer tuong ung se bi bo qua kem
    * canh bao mot lan, thay vi nem exception lam do ca frame render.
    */
   public static void draw(BuiltBuffer buffer) {
      if (buffer == null) {
         return;
      }

      try (buffer) {
         BuiltBuffer.DrawParameters params = buffer.getDrawParameters();
         RenderLayer layer = layerFor(params.mode(), params.format());
         if (layer == null) {
            return;
         }

         layer.draw(buffer);
      } catch (Throwable t) {
         // Mot buffer loi khong duoc lam sap toan bo frame.
         warnOnce("draw-failed:" + t.getClass().getSimpleName(), t.getMessage());
      }
   }

   private static RenderLayer layerFor(DrawMode mode, VertexFormat format) {
      boolean isLine = mode == DrawMode.LINES || mode == DrawMode.DEBUG_LINES || mode == DrawMode.DEBUG_LINE_STRIP;

      if (isLine) {
         // Layer lines dung POSITION_COLOR_NORMAL_LINE_WIDTH.
         if (format == VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH) {
            return RenderLayers.lines();
         }

         // Line voi format khac (vi du POSITION_COLOR) khong co layer khop:
         // ve nhu quad mong con dung mau hon la bo han.
         if (format == VertexFormats.POSITION_COLOR) {
            return RenderLayers.debugTriangleFan();
         }

         return RenderLayers.lines();
      }

      if (format == VertexFormats.POSITION_COLOR) {
         // QUADS / TRIANGLE_STRIP / TRIANGLE_FAN voi mau.
         return RenderLayers.debugTriangleFan();
      }

      if (format == VertexFormats.POSITION) {
         return RenderLayers.debugQuads();
      }

      if (format == VertexFormats.POSITION_TEXTURE_COLOR) {
         // Khong co layer world nao dung dung format nay cho quad tuy y; cac module
         // lien quan (Particles, JumpCircle, FragEffects, Crosshair) dua vao shader
         // da bi xoa o 1.21.11. Bo qua thay vi ve sai mau.
         warnOnce("fmt:POSITION_TEXTURE_COLOR", "can viet lai theo RenderPipeline co texture");
         return null;
      }

      warnOnce("fmt:" + format, "khong co RenderLayer tuong ung");
      return null;
   }

   private static void warnOnce(String key, String detail) {
      if (WARNED.add(key)) {
         LOGGER.warn("[rycore] bo qua lenh ve: {} ({})", key, detail);
      }
   }
}
