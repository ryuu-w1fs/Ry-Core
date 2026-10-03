package rycore.utility.render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Stack;
import javax.imageio.ImageIO;
import rycore.utility.render.RenderCompat;
import net.minecraft.client.render.BufferBuilder;import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import rycore.features.modules.Module;
import rycore.features.modules.render.HudEditor;
import rycore.gui.font.Texture;
import rycore.utility.math.MathUtility;
import rycore.utility.render.shaders.ArcShader;
import rycore.utility.render.shaders.BlurProgram;
import rycore.utility.render.shaders.CheckboxShader;
import rycore.utility.render.shaders.HudShader;
import rycore.utility.render.shaders.RectangleShader;
import rycore.utility.render.shaders.TextureColorProgram;

import org.joml.Matrix3x2fStack;

public class Render2DEngine {
   public static TextureColorProgram TEXTURE_COLOR_PROGRAM;
   public static HudShader HUD_SHADER;
   public static CheckboxShader CHECKBOX_SHADER;
   public static RectangleShader RECTANGLE_SHADER;
   public static ArcShader ARC_PROGRAM;
   public static BlurProgram BLUR_PROGRAM;
   public static HashMap<Integer, Render2DEngine.BlurredShadow> shadowCache = new HashMap<>();
   public static HashMap<Integer, Render2DEngine.BlurredShadow> shadowCache1 = new HashMap<>();
   static final Stack<Render2DEngine.Rectangle> clipStack = new Stack<>();
   private static float globalAlpha = 1.0F;

   public static void setGlobalAlpha(float alpha) {
      globalAlpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
   }

   private static float applyGlobalAlpha(float alpha) {
      return MathHelper.clamp(alpha * globalAlpha, 0.0F, 1.0F);
   }

   private static int applyGlobalAlphaToInt(int color) {
      Color c = new Color(color, true);
      int a = MathHelper.clamp(Math.round(c.getAlpha() * globalAlpha), 0, 255);
      return a == c.getAlpha() ? color : new Color(c.getRed(), c.getGreen(), c.getBlue(), a).getRGB();
   }

   private static Color applyGlobalAlphaToColor(Color color) {
      int a = MathHelper.clamp(Math.round(color.getAlpha() * globalAlpha), 0, 255);
      return a == color.getAlpha() ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
   }

   public static void addWindow(MatrixStack stack, Render2DEngine.Rectangle r1) {
      Matrix4f matrix = stack.peek().getPositionMatrix();
      Vector4f coord = new Vector4f(r1.x, r1.y, 0.0F, 1.0F);
      Vector4f end = new Vector4f(r1.x1, r1.y1, 0.0F, 1.0F);
      coord.mulTranspose(matrix);
      end.mulTranspose(matrix);
      float x = coord.x();
      float y = coord.y();
      float endX = end.x();
      float endY = end.y();
      Render2DEngine.Rectangle r = new Render2DEngine.Rectangle(x, y, endX, endY);
      if (clipStack.empty()) {
         clipStack.push(r);
         beginScissor(r.x, r.y, r.x1, r.y1);
      } else {
         Render2DEngine.Rectangle lastClip = clipStack.peek();
         float lsx = lastClip.x;
         float lsy = lastClip.y;
         float lstx = lastClip.x1;
         float lsty = lastClip.y1;
         float nsx = MathHelper.clamp(r.x, lsx, lstx);
         float nsy = MathHelper.clamp(r.y, lsy, lsty);
         float nstx = MathHelper.clamp(r.x1, nsx, lstx);
         float nsty = MathHelper.clamp(r.y1, nsy, lsty);
         clipStack.push(new Render2DEngine.Rectangle(nsx, nsy, nstx, nsty));
         beginScissor(nsx, nsy, nstx, nsty);
      }
   }

   public static void popWindow() {
      clipStack.pop();
      if (clipStack.empty()) {
         endScissor();
      } else {
         Render2DEngine.Rectangle r = clipStack.peek();
         beginScissor(r.x, r.y, r.x1, r.y1);
      }
   }

   public static void beginScissor(double x, double y, double endX, double endY) {
      // scissor thuoc tung GUI element (ScreenRect scissorArea), khong con
      // la GPU state toan cuc, nen chi ghi lai vung cat de collector dung.
      int w = (int)Math.max(0.0, Math.ceil(endX - x));
      int h = (int)Math.max(0.0, Math.ceil(endY - y));
      net.minecraft.client.gui.ScreenRect rect =
         new net.minecraft.client.gui.ScreenRect((int)Math.floor(x), (int)Math.floor(y), w, h);
      // Vanilla dua scissor qua ScreenRect.transform(matrices): vung cat phai o
      // toa do man hinh sau transform.
      if (GuiContext.isAvailable()) {
         rect = rect.transform(GuiContext.get().getMatrices());
      }

      GuiVertexCollector.setScissor(rect);
   }

   public static void endScissor() {
      GuiVertexCollector.setScissor(null);
   }

   public static void addWindow(MatrixStack stack, float x, float y, float x1, float y1, double animation_factor) {
      float h = y + y1;
      float h2 = (float)(h * (1.0 - MathUtility.clamp(animation_factor, 0.0, 1.0025F)));
      float x3 = x;
      float y3 = y + h2;
      float x4 = x1;
      float y4 = y1 - h2;
      if (x4 < x3) {
         x4 = x3;
      }

      if (y4 < y3) {
         y4 = y3;
      }

      addWindow(stack, new Render2DEngine.Rectangle(x3, y3, x4, y4));
   }

   public static void horizontalGradient(MatrixStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      int start = applyGlobalAlphaToInt(startColor.getRGB());
      int end = applyGlobalAlphaToInt(endColor.getRGB());
      buffer.vertex(matrix, x1, y1, 0.0F).color(start);
      buffer.vertex(matrix, x1, y2, 0.0F).color(start);
      buffer.vertex(matrix, x2, y2, 0.0F).color(end);
      buffer.vertex(matrix, x2, y1, 0.0F).color(end);
      buffer.submit(DrawMode.QUADS);
      endRender();
   }

   public static void verticalGradient(MatrixStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      int start = applyGlobalAlphaToInt(startColor.getRGB());
      int end = applyGlobalAlphaToInt(endColor.getRGB());
      buffer.vertex(matrix, left, top, 0.0F).color(start);
      buffer.vertex(matrix, left, bottom, 0.0F).color(end);
      buffer.vertex(matrix, right, bottom, 0.0F).color(end);
      buffer.vertex(matrix, right, top, 0.0F).color(start);
      buffer.submit(DrawMode.QUADS);
      endRender();
   }

   public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      int color = applyGlobalAlphaToInt(c.getRGB());
      buffer.vertex(matrix, x, y + height, 0.0F).color(color);
      buffer.vertex(matrix, x + width, y + height, 0.0F).color(color);
      buffer.vertex(matrix, x + width, y, 0.0F).color(color);
      buffer.vertex(matrix, x, y, 0.0F).color(color);
      buffer.submit(DrawMode.QUADS);
      endRender();
   }

   public static void drawGradientRect(
      MatrixStack matrices, float x, float y, float width, float height, Color bottomLeft, Color bottomRight, Color topRight, Color topLeft
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      setRectPoints(buffer, matrix, x, y, x + width, y + height, bottomLeft, bottomRight, topRight, topLeft);
      buffer.submit(DrawMode.QUADS);
      endRender();
   }

   public static void drawClickGuiRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
      drawClickGuiRound(matrices, x, y, width, height, 0.0F, c);
   }

   public static void drawRectWithOutline(MatrixStack matrices, float x, float y, float width, float height, Color c, Color c2) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      int fill = applyGlobalAlphaToInt(c.getRGB());
      buffer.vertex(matrix, x, y + height, 0.0F).color(fill);
      buffer.vertex(matrix, x + width, y + height, 0.0F).color(fill);
      buffer.vertex(matrix, x + width, y, 0.0F).color(fill);
      buffer.vertex(matrix, x, y, 0.0F).color(fill);
      buffer.submit(DrawMode.QUADS);
      buffer = GuiVertexCollector.of(GuiContext.get());
      int outline = applyGlobalAlphaToInt(c2.getRGB());
      buffer.vertex(matrix, x, y + height, 0.0F).color(outline);
      buffer.vertex(matrix, x + width, y + height, 0.0F).color(outline);
      buffer.vertex(matrix, x + width, y, 0.0F).color(outline);
      buffer.vertex(matrix, x, y, 0.0F).color(outline);
      buffer.vertex(matrix, x, y + height, 0.0F).color(outline);
      buffer.submit(DrawMode.QUADS);
      endRender();
   }

   public static void drawRectDumbWay(MatrixStack matrices, float x, float y, float x1, float y1, Color c1) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      int color = applyGlobalAlphaToInt(c1.getRGB());
      buffer.vertex(matrix, x, y1, 0.0F).color(color);
      buffer.vertex(matrix, x1, y1, 0.0F).color(color);
      buffer.vertex(matrix, x1, y, 0.0F).color(color);
      buffer.vertex(matrix, x, y, 0.0F).color(color);
      buffer.submit(DrawMode.QUADS);
      endRender();
   }

   public static void setRectPoints(net.minecraft.client.render.VertexConsumer bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color c1, Color c2, Color c3, Color c4) {
      bufferBuilder.vertex(matrix, x, y1, 0.0F).color(applyGlobalAlphaToInt(c1.getRGB()));
      bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(applyGlobalAlphaToInt(c2.getRGB()));
      bufferBuilder.vertex(matrix, x1, y, 0.0F).color(applyGlobalAlphaToInt(c3.getRGB()));
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(applyGlobalAlphaToInt(c4.getRGB()));
   }

   public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
      return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
   }

   public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
   }

   public static void drawGradientBlurredShadow(
      MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4
   ) {
   }

   public static void drawGradientBlurredShadow1(
      MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4
   ) {
   }

   public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ImageIO.write(bi, "png", baos);
         byte[] bytes = baos.toByteArray();
         registerTexture(i, bytes);
      } catch (Exception var4) {
      }
   }

   private static void registerTexture(Texture i, byte[] content) {
      try {
         ByteBuffer data = BufferUtils.createByteBuffer(content.length).put(content);
         data.flip();
         NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "rycore", NativeImage.read(data));
         Module.mc.execute(() -> Module.mc.getTextureManager().registerTexture(i.getId(), tex));
      } catch (Exception var4) {
      }
   }

   public static void renderTexture(
      MatrixStack matrices,
      double x0,
      double y0,
      double width,
      double height,
      float u,
      float v,
      double regionWidth,
      double regionHeight,
      double textureWidth,
      double textureHeight
   ) {
      double x1 = x0 + width;
      double y1 = y0 + height;
      double z = 0.0;
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      buffer.vertex(matrix, (float)x0, (float)y1, (float)z).texture(u / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight);
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z)
         .texture((u + (float)regionWidth) / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight);
      buffer.vertex(matrix, (float)x1, (float)y0, (float)z).texture((u + (float)regionWidth) / (float)textureWidth, v / (float)textureHeight);
      buffer.vertex(matrix, (float)x0, (float)y0, (float)z).texture(u / (float)textureWidth, (v + 0.0F) / (float)textureHeight);
      buffer.submit(DrawMode.QUADS);
   }

   public static void renderGradientTexture(
      MatrixStack matrices,
      double x0,
      double y0,
      double width,
      double height,
      float u,
      float v,
      double regionWidth,
      double regionHeight,
      double textureWidth,
      double textureHeight,
      Color c1,
      Color c2,
      Color c3,
      Color c4
   ) {
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      renderGradientTextureInternal(buffer, matrices, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
      buffer.submit(DrawMode.QUADS);
   }

   public static void renderGradientTextureInternal(
      GuiVertexCollector buff,
      MatrixStack matrices,
      double x0,
      double y0,
      double width,
      double height,
      float u,
      float v,
      double regionWidth,
      double regionHeight,
      double textureWidth,
      double textureHeight,
      Color c1,
      Color c2,
      Color c3,
      Color c4
   ) {
      double x1 = x0 + width;
      double y1 = y0 + height;
      double z = 0.0;
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      buff.vertex(matrix, (float)x0, (float)y1, (float)z)
         .texture(u / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight)
         .color(applyGlobalAlphaToInt(c1.getRGB()));
      buff.vertex(matrix, (float)x1, (float)y1, (float)z)
         .texture((u + (float)regionWidth) / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight)
         .color(applyGlobalAlphaToInt(c2.getRGB()));
      buff.vertex(matrix, (float)x1, (float)y0, (float)z)
         .texture((u + (float)regionWidth) / (float)textureWidth, v / (float)textureHeight)
         .color(applyGlobalAlphaToInt(c3.getRGB()));
      buff.vertex(matrix, (float)x0, (float)y0, (float)z)
         .texture(u / (float)textureWidth, (v + 0.0F) / (float)textureHeight)
         .color(applyGlobalAlphaToInt(c4.getRGB()));
   }

   public static void renderRoundedGradientRect(
      MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float Radius
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      drawRound(matrices, x, y, width, height, Radius, color1);
      setupRender();
      GuiVertexCollector bufferBuilder = GuiVertexCollector.of(GuiContext.get());
      bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(applyGlobalAlphaToInt(color1.getRGB()));
      bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(applyGlobalAlphaToInt(color2.getRGB()));
      bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(applyGlobalAlphaToInt(color3.getRGB()));
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(applyGlobalAlphaToInt(color4.getRGB()));
      bufferBuilder.submit(DrawMode.QUADS);
      endRender();
   }

   public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
      renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius, 4.0);
   }

   public static void drawClickGuiRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
      if (color.getAlpha() > 0) {
         if (shouldBlurClickGuiComponents()) {
            drawRoundedBlur(matrices, x, y, width, height, radius, color, 5.0F * globalAlpha, color.getAlpha() / 255.0F);
         } else {
            drawRound(matrices, x, y, width, height, radius, color);
         }
      }
   }

   public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
      setupRender();
      renderRoundedQuadInternal(
         matrices.peek().getPositionMatrix(),
         c.getRed() / 255.0F,
         c.getGreen() / 255.0F,
         c.getBlue() / 255.0F,
         c.getAlpha() / 255.0F,
         fromX,
         fromY,
         toX,
         toY,
         radius,
         samples
      );
      endRender();
   }

   public static void renderRoundedQuad2(
      MatrixStack matrices, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius
   ) {
      setupRender();
      renderRoundedQuadInternal2(
         matrices.peek().getPositionMatrix(),
         c.getRed() / 255.0F,
         c.getGreen() / 255.0F,
         c.getBlue() / 255.0F,
         c.getAlpha() / 255.0F,
         c2.getRed() / 255.0F,
         c2.getGreen() / 255.0F,
         c2.getBlue() / 255.0F,
         c2.getAlpha() / 255.0F,
         c3.getRed() / 255.0F,
         c3.getGreen() / 255.0F,
         c3.getBlue() / 255.0F,
         c3.getAlpha() / 255.0F,
         c4.getRed() / 255.0F,
         c4.getGreen() / 255.0F,
         c4.getBlue() / 255.0F,
         c4.getAlpha() / 255.0F,
         fromX,
         fromY,
         toX,
         toY,
         radius
      );
      endRender();
   }

   public static void renderRoundedQuadInternal(
      Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples
   ) {
      GuiVertexCollector bufferBuilder = GuiVertexCollector.of(GuiContext.get());
      float alpha = ca * applyGlobalAlpha(1.0F);
      double[][] map = new double[][]{
         {toX - radius, toY - radius, radius},
         {toX - radius, fromY + radius, radius},
         {fromX + radius, fromY + radius, radius},
         {fromX + radius, toY - radius, radius}
      };

      for (int i = 0; i < 4; i++) {
         double[] current = map[i];
         double rad = current[2];
         double r = i * 90.0;

         while (r < 90.0 + i * 90.0) {
            float rad1 = (float)Math.toRadians(r);
            float sin = (float)(Math.sin(rad1) * rad);
            float cos = (float)(Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr, cg, cb, alpha);
            r += 90.0 / samples;
         }

         float rad1 = (float)Math.toRadians(90.0 + i * 90.0);
         float sin = (float)(Math.sin(rad1) * rad);
         float cos = (float)(Math.cos(rad1) * rad);
         bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr, cg, cb, alpha);
      }

      bufferBuilder.submit(DrawMode.TRIANGLE_FAN);
   }

   public static void renderRoundedQuadInternal2(
      Matrix4f matrix,
      float cr,
      float cg,
      float cb,
      float ca,
      float cr1,
      float cg1,
      float cb1,
      float ca1,
      float cr2,
      float cg2,
      float cb2,
      float ca2,
      float cr3,
      float cg3,
      float cb3,
      float ca3,
      double fromX,
      double fromY,
      double toX,
      double toY,
      double radC1
   ) {
      GuiVertexCollector bufferBuilder = GuiVertexCollector.of(GuiContext.get());
      float alpha1 = ca1 * applyGlobalAlpha(1.0F);
      float alpha = ca * applyGlobalAlpha(1.0F);
      float alpha2 = ca2 * applyGlobalAlpha(1.0F);
      float alpha3 = ca3 * applyGlobalAlpha(1.0F);
      double[][] map = new double[][]{
         {toX - radC1, toY - radC1, radC1}, {toX - radC1, fromY + radC1, radC1}, {fromX + radC1, fromY + radC1, radC1}, {fromX + radC1, toY - radC1, radC1}
      };

      for (int i = 0; i < 4; i++) {
         double[] current = map[i];
         double rad = current[2];

         for (double r = i * 90; r < 90 + i * 90; r += 10.0) {
            float rad1 = (float)Math.toRadians(r);
            float sin = (float)(Math.sin(rad1) * rad);
            float cos = (float)(Math.cos(rad1) * rad);
            switch (i) {
               case 0:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr1, cg1, cb1, alpha1);
                  break;
               case 1:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr, cg, cb, alpha);
                  break;
               case 2:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr2, cg2, cb2, alpha2);
                  break;
               default:
                  bufferBuilder.vertex(matrix, (float)current[0] + sin, (float)current[1] + cos, 0.0F).color(cr3, cg3, cb3, alpha3);
            }
         }
      }

      bufferBuilder.submit(DrawMode.TRIANGLE_FAN);
   }

   public static void draw2DGradientRect(
      MatrixStack matrices,
      float left,
      float top,
      float right,
      float bottom,
      Color leftBottomColor,
      Color leftTopColor,
      Color rightBottomColor,
      Color rightTopColor
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      setupRender();
      GuiVertexCollector bufferBuilder = GuiVertexCollector.of(GuiContext.get());
      bufferBuilder.vertex(matrix, right, top, 0.0F).color(applyGlobalAlphaToInt(rightTopColor.getRGB()));
      bufferBuilder.vertex(matrix, left, top, 0.0F).color(applyGlobalAlphaToInt(leftTopColor.getRGB()));
      bufferBuilder.vertex(matrix, left, bottom, 0.0F).color(applyGlobalAlphaToInt(leftBottomColor.getRGB()));
      bufferBuilder.vertex(matrix, right, bottom, 0.0F).color(applyGlobalAlphaToInt(rightBottomColor.getRGB()));
      bufferBuilder.submit(DrawMode.QUADS);
      endRender();
   }

   public static void setupRender() {
   }

   public static void drawTracerPointer(
      MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color
   ) {
      drawNewArrow(matrices, x, y, size + 8.0F, new Color(color));
   }

   public static void drawNewArrow(MatrixStack matrices, float x, float y, float size, Color color) {
      setupRender();
      Color arrowColor = applyGlobalAlphaToColor(color);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GuiVertexCollector bufferBuilder = GuiVertexCollector.of(GuiContext.get());
      bufferBuilder.vertex(matrix, x - size / 2.0F, y + size, 0.0F).texture(0.0F, 1.0F);
      bufferBuilder.vertex(matrix, x + size / 2.0F, y + size, 0.0F).texture(1.0F, 1.0F);
      bufferBuilder.vertex(matrix, x + size / 2.0F, y, 0.0F).texture(1.0F, 0.0F);
      bufferBuilder.vertex(matrix, x - size / 2.0F, y, 0.0F).texture(0.0F, 0.0F);
      bufferBuilder.submit(DrawMode.QUADS);
      endRender();
   }

   public static void drawDefaultArrow(
      MatrixStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color
   ) {
      if (glow) {
         drawBlurredShadow(matrices, x - size * tracerWidth, y, x + size * tracerWidth - (x - size * tracerWidth), size, 10, injectAlpha(new Color(color), 140));
      }

      matrices.push();
      setupRender();
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GuiVertexCollector bufferBuilder = GuiVertexCollector.of(GuiContext.get());
      int baseColor = applyGlobalAlphaToInt(color);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(baseColor);
      bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0F).color(baseColor);
      bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0F).color(baseColor);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(baseColor);
      color = darker(new Color(color), 0.8F).getRGB();
      int darkerColor = applyGlobalAlphaToInt(color);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(darkerColor);
      bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0F).color(darkerColor);
      bufferBuilder.vertex(matrix, x + size * tracerWidth, y + size, 0.0F).color(darkerColor);
      bufferBuilder.vertex(matrix, x, y, 0.0F).color(darkerColor);
      if (down) {
         color = darker(new Color(color), 0.6F).getRGB();
         int downColor = applyGlobalAlphaToInt(color);
         bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0F).color(downColor);
         bufferBuilder.vertex(matrix, x + size * tracerWidth, y + size, 0.0F).color(downColor);
         bufferBuilder.vertex(matrix, x, y + size - downHeight, 0.0F).color(downColor);
         bufferBuilder.vertex(matrix, x - size * tracerWidth, y + size, 0.0F).color(downColor);
      }

      bufferBuilder.submit(DrawMode.QUADS);
      endRender();
      matrices.pop();
   }

   public static void endRender() {
   }

   public static void drawGradientRound(
      MatrixStack ms, float v, float v1, float i, float i1, float v2, Color darker, Color darker1, Color darker2, Color darker3
   ) {
      renderRoundedQuad2(ms, darker, darker1, darker2, darker3, v, v1, v + i, v1 + i1, v2);
   }

   public static float scrollAnimate(float endPoint, float current, float speed) {
      boolean shouldContinueAnimation = endPoint > current;
      if (speed < 0.0F) {
         speed = 0.0F;
      } else if (speed > 1.0F) {
         speed = 1.0F;
      }

      float dif = Math.max(endPoint, current) - Math.min(endPoint, current);
      float factor = dif * speed;
      return current + (shouldContinueAnimation ? factor : -factor);
   }

   public static Color injectAlpha(Color color, int alpha) {
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
   }

   public static Color TwoColoreffect(Color cl1, Color cl2, double speed, double count) {
      int angle = (int)((System.currentTimeMillis() / speed + count) % 360.0);
      angle = (angle >= 180 ? 360 - angle : angle) * 2;
      return interpolateColorC(cl1, cl2, angle / 360.0F);
   }

   public static Color rainbow(int delay, float saturation, float brightness) {
      double rainbow = Math.ceil((float)(System.currentTimeMillis() + delay) / 16.0F);
      rainbow %= 360.0;
      return Color.getHSBColor((float)(rainbow / 360.0), saturation, brightness);
   }

   public static Color fade(int speed, int index, Color color, float alpha) {
      float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360.0F));
      return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int)(alpha * 255.0F))));
   }

   public static Color applyOpacity(Color color, float opacity) {
      opacity = applyGlobalAlpha(Math.min(1.0F, Math.max(0.0F, opacity)));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity));
   }

   public static int applyOpacity(int color_int, float opacity) {
      opacity = applyGlobalAlpha(Math.min(1.0F, Math.max(0.0F, opacity)));
      Color color = new Color(color_int);
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity)).getRGB();
   }

   public static Color darker(Color color, float factor) {
      return new Color(
         Math.max((int)(color.getRed() * factor), 0),
         Math.max((int)(color.getGreen() * factor), 0),
         Math.max((int)(color.getBlue() * factor), 0),
         color.getAlpha()
      );
   }

   public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      float hue = angle / 360.0F;
      Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int)(opacity * 255.0F))));
   }

   public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = (angle >= 180 ? 360 - angle : angle) * 2;
      return trueColor ? interpolateColorHue(start, end, angle / 360.0F) : interpolateColorC(start, end, angle / 360.0F);
   }

   public static Color interpolateColorC(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      return new Color(
         interpolateInt(color1.getRed(), color2.getRed(), amount),
         interpolateInt(color1.getGreen(), color2.getGreen(), amount),
         interpolateInt(color1.getBlue(), color2.getBlue(), amount),
         interpolateInt(color1.getAlpha(), color2.getAlpha(), amount)
      );
   }

   public static Color interpolateColorHue(Color color1, Color color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
      float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
      Color resultColor = Color.getHSBColor(
         interpolateFloat(color1HSB[0], color2HSB[0], amount),
         interpolateFloat(color1HSB[1], color2HSB[1], amount),
         interpolateFloat(color1HSB[2], color2HSB[2], amount)
      );
      return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
   }

   public static double interpolate(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
      return (float)interpolate(oldValue, newValue, (float)interpolationValue);
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return (int)interpolate(oldValue, newValue, (float)interpolationValue);
   }

   public static void drawArc(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float thickness, float start, float end, Color c1, Color c2
   ) {
      // Shader arc la stub: ve vien hinh chu nhat bo goc thay cho cung tron.
      drawRound(matrices, x - width / 2.0F, y - height / 2.0F, width, height, radius, c1);
   }

   public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha) {
      // Shader rectangle la stub: ve bang mau thuc.
      drawRound(matrices, x, y, width, height, radius, new Color(1.0F, 1.0F, 1.0F, applyGlobalAlpha(alpha)));
   }

   public static void drawRect(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha, Color c1, Color c2, Color c3, Color c4
   ) {
      // Shader rectangle la stub: ve gradient bang mau thuc.
      drawGradientRect(
         matrices, x, y, width, height,
         applyGlobalAlphaToColor(c2), applyGlobalAlphaToColor(c3),
         applyGlobalAlphaToColor(c4), applyGlobalAlphaToColor(c1)
      );
   }

   public static void drawCheckbox(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float thickness, float progress, Color color
   ) {
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Vector4f topLeft = new Vector4f(x, y, 0.0F, 1.0F);
      Vector4f bottomRight = new Vector4f(x + width, y + height, 0.0F, 1.0F);
      topLeft.mulTranspose(matrix);
      bottomRight.mulTranspose(matrix);
      float transformedX = Math.min(topLeft.x(), bottomRight.x());
      float transformedY = Math.min(topLeft.y(), bottomRight.y());
      float transformedWidth = Math.abs(bottomRight.x() - topLeft.x());
      float transformedHeight = Math.abs(bottomRight.y() - topLeft.y());
      if (!(transformedWidth <= 0.01F) && !(transformedHeight <= 0.01F)) {
         float scale = Math.min(transformedWidth / width, transformedHeight / height);
         // Shader checkbox la stub: ve o vuong mau theo tien do.
         float fill = MathHelper.clamp(progress, 0.0F, 1.0F);
         if (fill > 0.0F) {
            drawRound(matrices, x, y, width * fill, height, radius, applyGlobalAlphaToColor(color));
         }
      }
   }

   public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius) {
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         drawRoundedBlur(matrices, x, y, width, height, radius, applyGlobalAlphaToColor(HudEditor.getBlurColor()));
      } else {
         // Shader HUD la stub: ve nen bang mau thuc.
         float alpha = applyGlobalAlpha(HudEditor.getAlpha());
         drawRound(matrices, x, y, width, height, radius, applyOpacity(HudEditor.getBlurColor(), alpha));
      }
   }

   public static void drawHudBase2(
      MatrixStack matrices, float x, float y, float width, float height, float radius, float blurStrenth, float blurOpacity, float animationFactor
   ) {
      if (HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry)) {
         blurStrenth *= animationFactor;
         blurOpacity *= animationFactor;
         Color c = interpolateColorC(Color.BLACK, HudEditor.getBlurColor(), animationFactor);
         drawRoundedBlur(matrices, x, y, width, height, radius, applyGlobalAlphaToColor(c), blurStrenth, applyGlobalAlpha(blurOpacity));
      } else {
         // Shader HUD la stub: ve nen bang mau thuc.
         float alpha = applyGlobalAlpha(HudEditor.getAlpha() * animationFactor);
         drawRound(matrices, x, y, width, height, radius, applyOpacity(HudEditor.getBlurColor(), alpha));
      }
   }

   public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius, boolean hud) {
      // Shader HUD la stub: ve nen bang mau thuc.
      float alpha = applyGlobalAlpha(HudEditor.getAlpha());
      drawRound(matrices, x, y, width, height, radius, applyOpacity(HudEditor.getBlurColor(), alpha));
   }

   public static void drawRoundedBlur(MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1) {
      drawRoundedBlur(matrices, x, y, width, height, radius, applyGlobalAlphaToColor(c1), 5.0F, applyGlobalAlpha(HudEditor.getBlurOpacity()));
   }

   public static void drawRoundedBlur(
      MatrixStack matrices, float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity
   ) {
      // Shader blur la stub: ve nen mo bang mau phang thay cho blur that.
      drawRound(matrices, x, y, width, height, radius, applyOpacity(c1, applyGlobalAlpha(blurOpacity)));
   }

   public static void drawHudBase(MatrixStack matrices, float x, float y, float width, float height, float radius, float alpha) {
      // Shader HUD la stub: ve nen bang mau thuc.
      drawRound(matrices, x, y, width, height, radius, applyOpacity(HudEditor.getBlurColor(), applyGlobalAlpha(alpha)));
   }

   public static void drawGuiBase(MatrixStack matrices, float x, float y, float width, float height, float radius, float opacity) {
      if (shouldBlurClickGuiComponents()) {
         drawRoundedBlur(matrices, x, y, width, height, radius, HudEditor.getBlurColor(), 5.0F * globalAlpha, opacity);
      } else {
         // Shader HUD la stub: ve nen bang mau thuc.
         drawRound(matrices, x, y, width, height, radius, applyOpacity(HudEditor.getBlurColor(), applyGlobalAlpha(opacity)));
      }
   }

   private static boolean shouldBlurClickGuiComponents() {
      return HudEditor.hudStyle.is(HudEditor.HudStyle.Blurry);
   }

   public static GuiVertexCollector preShaderDraw(MatrixStack matrices, float x, float y, float width, float height) {
      setupRender();
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      setRectanglePoints(buffer, matrix, x, y, x + width, y + height);
      return buffer;
   }

   public static void setRectanglePoints(net.minecraft.client.render.VertexConsumer buffer, Matrix4f matrix, float x, float y, float x1, float y1) {
      buffer.vertex(matrix, x, y, 0.0F);
      buffer.vertex(matrix, x, y1, 0.0F);
      buffer.vertex(matrix, x1, y1, 0.0F);
      buffer.vertex(matrix, x1, y, 0.0F);
   }

   public static void drawLine(float x, float y, float x1, float y1, int color) {
      GuiVertexCollector bufferBuilder = GuiVertexCollector.of(GuiContext.get());
      int lineColor = applyGlobalAlphaToInt(color);
      bufferBuilder.vertex(x, y, 0.0F).color(lineColor);
      bufferBuilder.vertex(x1, y1, 0.0F).color(lineColor);
      bufferBuilder.submit(DrawMode.DEBUG_LINE_STRIP);
   }

   public static boolean isDark(Color color) {
      return isDark(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
   }

   public static boolean isDark(float r, float g, float b) {
      return colorDistance(r, g, b, 0.0F, 0.0F, 0.0F) < colorDistance(r, g, b, 1.0F, 1.0F, 1.0F);
   }

   public static float colorDistance(float r1, float g1, float b1, float r2, float g2, float b2) {
      float a = r2 - r1;
      float b = g2 - g1;
      float c = b2 - b1;
      return (float)Math.sqrt(a * a + b * b + c * c);
   }

   public static void initShaders() {
      HUD_SHADER = new HudShader();
      TEXTURE_COLOR_PROGRAM = new TextureColorProgram();
      ARC_PROGRAM = new ArcShader();
      CHECKBOX_SHADER = new CheckboxShader();
      RECTANGLE_SHADER = new RectangleShader();
      BLUR_PROGRAM = new BlurProgram();
   }

   @NotNull
   public static Color getColor(@NotNull Color start, @NotNull Color end, float progress, boolean smooth) {
      if (!smooth) {
         return progress >= 0.95 ? end : start;
      }

      int rDiff = end.getRed() - start.getRed();
      int gDiff = end.getGreen() - start.getGreen();
      int bDiff = end.getBlue() - start.getBlue();
      int aDiff = end.getAlpha() - start.getAlpha();
      return new Color(
         fixColorValue(start.getRed() + (int)(rDiff * progress)),
         fixColorValue(start.getGreen() + (int)(gDiff * progress)),
         fixColorValue(start.getBlue() + (int)(bDiff * progress)),
         fixColorValue(start.getAlpha() + (int)(aDiff * progress))
      );
   }

   private static int fixColorValue(int colorVal) {
      return colorVal > 255 ? 255 : Math.max(colorVal, 0);
   }

   /** Ve ngay - dung cho tang 3D (trong world), nam ngoai batch GUI. */
   public static void endBuilding(BufferBuilder bb) {
      BuiltBuffer builtBuffer = bb.endNullable();
      if (builtBuffer != null) {
         RenderCompat.draw(builtBuffer);
      }
   }

   /** Nap vao batch GUI - dung cho tang 2D (HUD, ClickGUI). */
   public static void endBuilding(GuiVertexCollector bb) {
      bb.submit();
   }

   public static class BlurredShadow {
      Texture id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));

      public BlurredShadow(BufferedImage bufferedImage) {
         Render2DEngine.registerBufferedImageTexture(this.id, bufferedImage);
      }

      public void bind() {
      }
   }

   public record Rectangle(float x, float y, float x1, float y1) {
      public boolean contains(double x, double y) {
         return x >= this.x && x <= this.x1 && y >= this.y && y <= this.y1;
      }
   }

   // --- Overload tuong thich GUI 2D (Matrix3x2fStack) - xem MatrixCompat ---

   public static void addWindow(Matrix3x2fStack stack, Render2DEngine.Rectangle r1) {
      addWindow(MatrixCompat.toMatrixStack(stack), r1);
   }

   public static void addWindow(Matrix3x2fStack stack, float x, float y, float x1, float y1, double animation_factor) {
      addWindow(MatrixCompat.toMatrixStack(stack), x, y, x1, y1, animation_factor);
   }

   public static void horizontalGradient(Matrix3x2fStack matrices, float x1, float y1, float x2, float y2, Color startColor, Color endColor) {
      horizontalGradient(MatrixCompat.toMatrixStack(matrices), x1, y1, x2, y2, startColor, endColor);
   }

   public static void verticalGradient(Matrix3x2fStack matrices, float left, float top, float right, float bottom, Color startColor, Color endColor) {
      verticalGradient(MatrixCompat.toMatrixStack(matrices), left, top, right, bottom, startColor, endColor);
   }

   public static void drawRect(Matrix3x2fStack matrices, float x, float y, float width, float height, Color c) {
      drawRect(MatrixCompat.toMatrixStack(matrices), x, y, width, height, c);
   }

   public static void drawClickGuiRect(Matrix3x2fStack matrices, float x, float y, float width, float height, Color c) {
      drawClickGuiRect(MatrixCompat.toMatrixStack(matrices), x, y, width, height, c);
   }

   public static void drawRectWithOutline(Matrix3x2fStack matrices, float x, float y, float width, float height, Color c, Color c2) {
      drawRectWithOutline(MatrixCompat.toMatrixStack(matrices), x, y, width, height, c, c2);
   }

   public static void drawRectDumbWay(Matrix3x2fStack matrices, float x, float y, float x1, float y1, Color c1) {
      drawRectDumbWay(MatrixCompat.toMatrixStack(matrices), x, y, x1, y1, c1);
   }

   public static void drawBlurredShadow(Matrix3x2fStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
      drawBlurredShadow(MatrixCompat.toMatrixStack(matrices), x, y, width, height, blurRadius, color);
   }

   public static void drawRound(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, Color color) {
      drawRound(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, color);
   }

   public static void drawClickGuiRound(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, Color color) {
      drawClickGuiRound(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, color);
   }

   public static void renderRoundedQuad(Matrix3x2fStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
      renderRoundedQuad(MatrixCompat.toMatrixStack(matrices), c, fromX, fromY, toX, toY, radius, samples);
   }

   public static void drawNewArrow(Matrix3x2fStack matrices, float x, float y, float size, Color color) {
      drawNewArrow(MatrixCompat.toMatrixStack(matrices), x, y, size, color);
   }

   public static void drawRect(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float alpha) {
      drawRect(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, alpha);
   }

   public static void drawHudBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius) {
      drawHudBase(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius);
   }

   public static void drawHudBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, boolean hud) {
      drawHudBase(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, hud);
   }

   public static void drawRoundedBlur(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, Color c1) {
      drawRoundedBlur(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, c1);
   }

   public static void drawHudBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float alpha) {
      drawHudBase(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, alpha);
   }

   public static void drawGuiBase(Matrix3x2fStack matrices, float x, float y, float width, float height, float radius, float opacity) {
      drawGuiBase(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, opacity);
   }

   public static GuiVertexCollector preShaderDraw(Matrix3x2fStack matrices, float x, float y, float width, float height) {
      return preShaderDraw(MatrixCompat.toMatrixStack(matrices), x, y, width, height);
   }

   public static void drawGradientRect(Matrix3x2fStack matrices , float x, float y, float width, float height, Color bottomLeft, Color bottomRight, Color topRight, Color topLeft) {
      drawGradientRect(MatrixCompat.toMatrixStack(matrices), x, y, width, height, bottomLeft, bottomRight, topRight, topLeft);
   }

   public static void drawGradientBlurredShadow(Matrix3x2fStack matrices , float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
      drawGradientBlurredShadow(MatrixCompat.toMatrixStack(matrices), x, y, width, height, blurRadius, color1, color2, color3, color4);
   }

   public static void drawGradientBlurredShadow1(Matrix3x2fStack matrices , float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
      drawGradientBlurredShadow1(MatrixCompat.toMatrixStack(matrices), x, y, width, height, blurRadius, color1, color2, color3, color4);
   }

   public static void renderTexture(Matrix3x2fStack matrices , double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
      renderTexture(MatrixCompat.toMatrixStack(matrices), x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
   }

   public static void renderGradientTexture(Matrix3x2fStack matrices , double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
      renderGradientTexture(MatrixCompat.toMatrixStack(matrices), x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
   }

   public static void renderRoundedGradientRect(Matrix3x2fStack matrices , Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float Radius) {
      renderRoundedGradientRect(MatrixCompat.toMatrixStack(matrices), color1, color2, color3, color4, x, y, width, height, Radius);
   }

   public static void renderRoundedQuad2(Matrix3x2fStack matrices , Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius) {
      renderRoundedQuad2(MatrixCompat.toMatrixStack(matrices), c, c2, c3, c4, fromX, fromY, toX, toY, radius);
   }

   public static void draw2DGradientRect(Matrix3x2fStack matrices , float left, float top, float right, float bottom, Color leftBottomColor, Color leftTopColor, Color rightBottomColor, Color rightTopColor) {
      draw2DGradientRect(MatrixCompat.toMatrixStack(matrices), left, top, right, bottom, leftBottomColor, leftTopColor, rightBottomColor, rightTopColor);
   }

   public static void drawTracerPointer(Matrix3x2fStack matrices , float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
      drawTracerPointer(MatrixCompat.toMatrixStack(matrices), x, y, size, tracerWidth, downHeight, down, glow, color);
   }

   public static void drawDefaultArrow(Matrix3x2fStack matrices , float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {
      drawDefaultArrow(MatrixCompat.toMatrixStack(matrices), x, y, size, tracerWidth, downHeight, down, glow, color);
   }

   public static void drawGradientRound(Matrix3x2fStack ms , float v, float v1, float i, float i1, float v2, Color darker, Color darker1, Color darker2, Color darker3) {
      drawGradientRound(MatrixCompat.toMatrixStack(ms), v, v1, i, i1, v2, darker, darker1, darker2, darker3);
   }

   public static void drawArc(Matrix3x2fStack matrices , float x, float y, float width, float height, float radius, float thickness, float start, float end, Color c1, Color c2) {
      drawArc(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, thickness, start, end, c1, c2);
   }

   public static void drawRect(Matrix3x2fStack matrices , float x, float y, float width, float height, float radius, float alpha, Color c1, Color c2, Color c3, Color c4) {
      drawRect(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, alpha, c1, c2, c3, c4);
   }

   public static void drawCheckbox(Matrix3x2fStack matrices , float x, float y, float width, float height, float radius, float thickness, float progress, Color color) {
      drawCheckbox(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, thickness, progress, color);
   }

   public static void drawHudBase2(Matrix3x2fStack matrices , float x, float y, float width, float height, float radius, float blurStrenth, float blurOpacity, float animationFactor) {
      drawHudBase2(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, blurStrenth, blurOpacity, animationFactor);
   }

   public static void drawRoundedBlur(Matrix3x2fStack matrices , float x, float y, float width, float height, float radius, Color c1, float blurStrenth, float blurOpacity) {
      drawRoundedBlur(MatrixCompat.toMatrixStack(matrices), x, y, width, height, radius, c1, blurStrenth, blurOpacity);
   }

   /**
    * Thanh gradient hue doc, ve trong MOT batch.
    *
    * <p>Thay cho vong lap ve tung dong 1px: o 1.21.11 moi lenh ve la mot GUI
    * element rieng, nen vong lap sinh hang tram element moi frame.
    */
   public static void drawHueBar(MatrixStack matrices, float x, float y, float width, float height) {
      if (!GuiContext.isAvailable() || height <= 0.0F) {
         return;
      }

      Matrix4f matrix = matrices.peek().getPositionMatrix();
      GuiVertexCollector buffer = GuiVertexCollector.of(GuiContext.get());
      int steps = Math.max(1, Math.min(32, (int)height));
      float step = height / steps;

      for (int i = 0; i < steps; i++) {
         float t0 = (float)i / steps;
         float t1 = (float)(i + 1) / steps;
         int c0 = applyGlobalAlphaToInt(Color.HSBtoRGB(t0, 1.0F, 1.0F));
         int c1 = applyGlobalAlphaToInt(Color.HSBtoRGB(t1, 1.0F, 1.0F));
         float y0 = y + i * step;
         float y1 = y0 + step;
         buffer.vertex(matrix, x, y1, 0.0F).color(c1);
         buffer.vertex(matrix, x + width, y1, 0.0F).color(c1);
         buffer.vertex(matrix, x + width, y0, 0.0F).color(c0);
         buffer.vertex(matrix, x, y0, 0.0F).color(c0);
      }

      buffer.submit(DrawMode.QUADS);
   }
}
