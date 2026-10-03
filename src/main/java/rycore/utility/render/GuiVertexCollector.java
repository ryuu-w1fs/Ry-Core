package rycore.utility.render;

import java.util.ArrayDeque;
import java.util.Deque;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;

/**
 * {@link VertexConsumer} thu vertex de nap vao batch GUI cua 1.21.11.
 *
 * <p>Tu 1.21.6 {@link DrawContext} khong ve ngay ma gom moi phan tu vao
 * {@code GuiRenderState}, roi engine ve ca batch mot luot cuoi frame. Code ve cu cua
 * VCore tu dung {@code BufferBuilder} roi {@code draw()} ngay lap tuc - nam ngoai
 * batch nen bi ghi de, va moi lenh ve thanh mot draw-call rieng.
 *
 * <p>Lop nay giu nguyen cach goi cu ({@code buffer.vertex(...).color(...)}) nhung
 * nap qua {@link SimpleGuiElementRenderState}.
 *
 * <p>Hieu nang: vertex duoc gom vao mang primitive (khong tao object moi dinh), va
 * cac collector duoc tai su dung qua pool - GUI ve lai moi frame nen day la duong
 * nong nhat cua CPU.
 */
public final class GuiVertexCollector implements VertexConsumer {
   /** Dung lai collector giua cac frame de khong cap phat lai lien tuc. */
   private static final Deque<GuiVertexCollector> POOL = new ArrayDeque<>();
   private static final int MAX_POOLED = 64;

   /**
    * Vung cat hien hanh cua frame. 1.21.11 khong con scissor toan cuc: moi GUI
    * element tu khai bao vung cat cua minh, nen gia tri nay duoc gan vao tung
    * element luc submit.
    */
   private static ScreenRect currentScissor;

   public static void setScissor(ScreenRect rect) {
      currentScissor = rect;
   }

   private DrawContext context;
   private RenderPipeline pipeline;
   private TextureSetup textureSetup;

   // Vertex gom dang mang phang: [x, y, u, v] + argb rieng.
   private float[] xy = new float[256 * 4];
   private int[] colors = new int[256];
   private int count;

   private float px;
   private float py;
   private float pu;
   private float pv;
   private boolean pending;

   private float minX = Float.MAX_VALUE;
   private float minY = Float.MAX_VALUE;
   private float maxX = -Float.MAX_VALUE;
   private float maxY = -Float.MAX_VALUE;

   private GuiVertexCollector() {
   }

   private static GuiVertexCollector obtain(DrawContext context, RenderPipeline pipeline, TextureSetup textureSetup) {
      GuiVertexCollector c = POOL.isEmpty() ? new GuiVertexCollector() : POOL.pop();
      c.context = context;
      c.pipeline = pipeline;
      c.textureSetup = textureSetup;
      c.reset();
      return c;
   }

   public static GuiVertexCollector of(DrawContext context) {
      return obtain(context, RenderPipelines.GUI, TextureSetup.empty());
   }

   public static GuiVertexCollector of(DrawContext context, RenderPipeline pipeline, TextureSetup textureSetup) {
      return obtain(context, pipeline, textureSetup);
   }

   /**
    * Batch co texture (font glyph, icon...). Dung pipeline GUI_TEXTURED vi pipeline
    * GUI khong co kenh texture.
    */
   public static GuiVertexCollector ofTextured(DrawContext context, Identifier texture) {
      AbstractTexture tex = MinecraftClient.getInstance().getTextureManager().getTexture(texture);
      return obtain(context, RenderPipelines.GUI_TEXTURED, TextureSetup.of(tex.getGlTextureView(), tex.getSampler()));
   }

   private void reset() {
      this.count = 0;
      this.pending = false;
      this.minX = Float.MAX_VALUE;
      this.minY = Float.MAX_VALUE;
      this.maxX = -Float.MAX_VALUE;
      this.maxY = -Float.MAX_VALUE;
   }

   private void push(float x, float y, float u, float v, int argb) {
      if (this.count == this.colors.length) {
         int grown = this.colors.length * 2;
         float[] nxy = new float[grown * 4];
         System.arraycopy(this.xy, 0, nxy, 0, this.count * 4);
         int[] nc = new int[grown];
         System.arraycopy(this.colors, 0, nc, 0, this.count);
         this.xy = nxy;
         this.colors = nc;
      }

      int i = this.count * 4;
      this.xy[i] = x;
      this.xy[i + 1] = y;
      this.xy[i + 2] = u;
      this.xy[i + 3] = v;
      this.colors[this.count] = argb;
      this.count++;
   }

   private void flushPending() {
      if (this.pending) {
         // Dinh chua co color: coi nhu mau trang.
         this.push(this.px, this.py, this.pu, this.pv, -1);
         this.pending = false;
      }
   }

   @Override
   public VertexConsumer vertex(float x, float y, float z) {
      this.flushPending();
      this.px = x;
      this.py = y;
      this.pu = 0.0F;
      this.pv = 0.0F;
      this.pending = true;
      if (x < this.minX) {
         this.minX = x;
      }

      if (y < this.minY) {
         this.minY = y;
      }

      if (x > this.maxX) {
         this.maxX = x;
      }

      if (y > this.maxY) {
         this.maxY = y;
      }

      return this;
   }

   @Override
   public VertexConsumer color(int argb) {
      if (this.pending) {
         this.push(this.px, this.py, this.pu, this.pv, argb);
         this.pending = false;
      }

      return this;
   }

   @Override
   public VertexConsumer color(int r, int g, int b, int a) {
      return this.color(a << 24 | r << 16 | g << 8 | b);
   }

   @Override
   public VertexConsumer texture(float u, float v) {
      this.pu = u;
      this.pv = v;
      return this;
   }

   @Override
   public VertexConsumer overlay(int u, int v) {
      return this;
   }

   @Override
   public VertexConsumer light(int u, int v) {
      return this;
   }

   @Override
   public VertexConsumer normal(float x, float y, float z) {
      return this;
   }

   @Override
   public VertexConsumer lineWidth(float width) {
      return this;
   }

   public boolean isEmpty() {
      this.flushPending();
      return this.count == 0;
   }

   /** Nap vertex da thu vao batch GUI cua frame hien tai. */
   public void submit() {
      this.submit(DrawMode.QUADS);
   }

   /**
    * Pipeline GUI chi ve QUADS, nen cac che do khac phai chuyen thanh quad:
    * TRIANGLE_FAN thanh quad "nan quat", LINE_STRIP thanh quad mong.
    */
   public void submit(DrawMode mode) {
      this.flushPending();
      if (this.count == 0 || this.context == null) {
         this.release();
         return;
      }

      float[] outXy;
      int[] outColors;
      int outCount;

      if (mode == DrawMode.TRIANGLE_FAN && this.count >= 3) {
         int tris = this.count - 2;
         outCount = tris * 4;
         outXy = new float[outCount * 4];
         outColors = new int[outCount];
         int w = 0;
         for (int i = 1; i + 1 < this.count; i++) {
            w = this.copyVert(0, outXy, outColors, w);
            w = this.copyVert(i, outXy, outColors, w);
            w = this.copyVert(i + 1, outXy, outColors, w);
            w = this.copyVert(i + 1, outXy, outColors, w);
         }
      } else if ((mode == DrawMode.DEBUG_LINE_STRIP || mode == DrawMode.DEBUG_LINES || mode == DrawMode.LINES)
         && this.count >= 2) {
         int segs = this.count - 1;
         outXy = new float[segs * 4 * 4];
         outColors = new int[segs * 4];
         int w = 0;
         for (int i = 0; i + 1 < this.count; i++) {
            int a = i * 4;
            int b = (i + 1) * 4;
            float ax = this.xy[a];
            float ay = this.xy[a + 1];
            float bx = this.xy[b];
            float by = this.xy[b + 1];
            float dx = bx - ax;
            float dy = by - ay;
            float len = (float)Math.sqrt(dx * dx + dy * dy);
            if (len < 1.0E-4F) {
               continue;
            }

            // Phap tuyen vuong goc doan thang, do day 1px.
            float nx = -dy / len * 0.5F;
            float ny = dx / len * 0.5F;
            w = this.writeVert(outXy, outColors, w, ax + nx, ay + ny, this.colors[i]);
            w = this.writeVert(outXy, outColors, w, ax - nx, ay - ny, this.colors[i]);
            w = this.writeVert(outXy, outColors, w, bx - nx, by - ny, this.colors[i + 1]);
            w = this.writeVert(outXy, outColors, w, bx + nx, by + ny, this.colors[i + 1]);
         }

         outCount = w;
      } else {
         outCount = this.count;
         outXy = new float[outCount * 4];
         System.arraycopy(this.xy, 0, outXy, 0, outCount * 4);
         outColors = new int[outCount];
         System.arraycopy(this.colors, 0, outColors, 0, outCount);
      }

      if (outCount > 0) {
         // Chup ma tran hien tai: batch chi ve o cuoi frame, luc do transform da doi.
         Matrix3x2f pose = new Matrix3x2f(this.context.getMatrices());

         // bounds phai la toa do MAN HINH (sau transform) vi engine dung no de cull
         // va sap lop; dung toa do goc se cull sai.
         ScreenRect bounds = new ScreenRect(
            (int)Math.floor(this.minX) - 1,
            (int)Math.floor(this.minY) - 1,
            (int)Math.ceil(this.maxX - this.minX) + 2,
            (int)Math.ceil(this.maxY - this.minY) + 2
         ).transform(pose);

         this.context.state.addSimpleElement(
            new Element(this.pipeline, this.textureSetup, pose, outXy, outColors, outCount, bounds, currentScissor)
         );
      }

      this.release();
   }

   private int copyVert(int src, float[] outXy, int[] outColors, int w) {
      int s = src * 4;
      int d = w * 4;
      outXy[d] = this.xy[s];
      outXy[d + 1] = this.xy[s + 1];
      outXy[d + 2] = this.xy[s + 2];
      outXy[d + 3] = this.xy[s + 3];
      outColors[w] = this.colors[src];
      return w + 1;
   }

   private int writeVert(float[] outXy, int[] outColors, int w, float x, float y, int argb) {
      int d = w * 4;
      outXy[d] = x;
      outXy[d + 1] = y;
      outXy[d + 2] = 0.0F;
      outXy[d + 3] = 0.0F;
      outColors[w] = argb;
      return w + 1;
   }

   private void release() {
      this.context = null;
      this.pipeline = null;
      this.textureSetup = null;
      this.reset();
      if (POOL.size() < MAX_POOLED) {
         POOL.push(this);
      }
   }

   private record Element(
      RenderPipeline pipeline,
      TextureSetup textureSetup,
      Matrix3x2f pose,
      float[] xy,
      int[] colors,
      int count,
      ScreenRect bounds,
      ScreenRect scissorArea
   ) implements SimpleGuiElementRenderState {
      @Override
      public void setupVertices(VertexConsumer consumer) {
         // Layout vertex phai khop pipeline:
         //  - GUI (POSITION_COLOR):              vertex -> color
         //  - GUI_TEXTURED (POSITION_TEX_COLOR): vertex -> texture -> color
         // Goi sai thu tu lam lech layout va moi quad ra mau trang.
         boolean textured = this.pipeline == RenderPipelines.GUI_TEXTURED;
         for (int i = 0; i < this.count; i++) {
            int o = i * 4;
            VertexConsumer vc = consumer.vertex(this.pose, this.xy[o], this.xy[o + 1]);
            if (textured) {
               vc = vc.texture(this.xy[o + 2], this.xy[o + 3]);
            }

            vc.color(this.colors[i]);
         }
      }
   }
}
