package rycore.utility.render.shaders.satin.impl;

import java.util.function.Consumer;
import java.util.function.IntSupplier;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import rycore.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import rycore.utility.render.shaders.satin.api.managed.ManagedFramebuffer;
import rycore.utility.render.shaders.satin.api.managed.ManagedShaderEffect;
import rycore.utility.render.shaders.satin.api.managed.ShaderEffectManager;
import rycore.utility.render.shaders.satin.api.managed.uniform.SamplerUniform;
import rycore.utility.render.shaders.satin.api.managed.uniform.SamplerUniformV2;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform1f;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform1i;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform2f;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform2i;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform3f;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform3i;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform4f;
import rycore.utility.render.shaders.satin.api.managed.uniform.Uniform4i;
import rycore.utility.render.shaders.satin.api.managed.uniform.UniformMat4;

/**
 * Ban rut gon cua satin shader framework cho 1.21.11.
 *
 * <p>Ban goc dung {@code ShaderEffect}, {@code JsonEffectShaderProgram} va he uniform
 * thu cong - tat ca da bi xoa tu 1.21.2, khi toan bo pipeline chuyen sang
 * {@code RenderPipeline} khai bao tinh cong UBO std140. Khong co phep anh xa 1-1,
 * viec phuc hoi doi hoi viet lai ca framework kem shader moi trong resources.
 *
 * <p>Lop nay giu nguyen API cong khai de phan con lai cua RyCore bien dich va chay
 * duoc, nhung cac lenh set uniform khong di tiep xuong GPU. He qua: cac hieu ung
 * dua tren shader (bo goc muot, glow, blur) ve bang pipeline mac dinh thay vi
 * shader rieng. Cac module HUD/ClickGUI van hoat dong binh thuong.
 *
 * <p>Dang chu y: ngay trong ban 1.21 goc, {@code use()} cua ArcShader,
 * RectangleShader, CheckboxShader va HudShader da rong - tuc 4/5 shader nay von
 * khong duoc bind. Chi BlurProgram tung thuc su hoat dong.
 */
public final class ReloadableShaderEffectManager implements ShaderEffectManager {
   public static final ReloadableShaderEffectManager INSTANCE = new ReloadableShaderEffectManager();

   private ReloadableShaderEffectManager() {
   }

   /** Duoc goi tu MixinGameRenderer khi tai lai resource; khong con gi de tai. */
   public void reload(ResourceFactory factory) {
   }

   @Override
   public ManagedShaderEffect manage(Identifier id) {
      return new StubShaderEffect();
   }

   @Override
   public ManagedShaderEffect manage(Identifier id, Consumer<ManagedShaderEffect> onInit) {
      StubShaderEffect effect = new StubShaderEffect();
      if (onInit != null) {
         onInit.accept(effect);
      }

      return effect;
   }

   @Override
   public ManagedCoreShader manageCoreShader(Identifier id) {
      return new StubCoreShader();
   }

   @Override
   public ManagedCoreShader manageCoreShader(Identifier id, VertexFormat format) {
      return new StubCoreShader();
   }

   @Override
   public ManagedCoreShader manageCoreShader(Identifier id, VertexFormat format, Consumer<ManagedCoreShader> onInit) {
      StubCoreShader shader = new StubCoreShader();
      if (onInit != null) {
         onInit.accept(shader);
      }

      return shader;
   }

   /** Cac uniform khong lam gi, dung chung cho moi shader stub. */
   private static final class NoopUniform
      implements Uniform1i, Uniform2i, Uniform3i, Uniform4i, Uniform1f, Uniform2f, Uniform3f, Uniform4f, UniformMat4, SamplerUniform, SamplerUniformV2 {
      static final NoopUniform INSTANCE = new NoopUniform();

      @Override
      public void set(int value) {
      }

      @Override
      public void set(int x, int y) {
      }

      @Override
      public void set(int x, int y, int z) {
      }

      @Override
      public void set(int x, int y, int z, int w) {
      }

      @Override
      public void set(float value) {
      }

      @Override
      public void set(float x, float y) {
      }

      @Override
      public void set(float x, float y, float z) {
      }

      @Override
      public void set(float x, float y, float z, float w) {
      }

      @Override
      public void set(Vector2f value) {
      }

      @Override
      public void set(Vector3f value) {
      }

      @Override
      public void set(Vector4f value) {
      }

      @Override
      public void set(Matrix4f value) {
      }

      @Override
      public void set(AbstractTexture texture) {
      }

      @Override
      public void set(Framebuffer framebuffer) {
      }

      @Override
      public void set(IntSupplier supplier) {
      }

      @Override
      public void setFromArray(float[] values) {
      }
   }

   private static class StubUniformFinder {
      public Uniform1i findUniform1i(String name) {
         return NoopUniform.INSTANCE;
      }

      public Uniform2i findUniform2i(String name) {
         return NoopUniform.INSTANCE;
      }

      public Uniform3i findUniform3i(String name) {
         return NoopUniform.INSTANCE;
      }

      public Uniform4i findUniform4i(String name) {
         return NoopUniform.INSTANCE;
      }

      public Uniform1f findUniform1f(String name) {
         return NoopUniform.INSTANCE;
      }

      public Uniform2f findUniform2f(String name) {
         return NoopUniform.INSTANCE;
      }

      public Uniform3f findUniform3f(String name) {
         return NoopUniform.INSTANCE;
      }

      public Uniform4f findUniform4f(String name) {
         return NoopUniform.INSTANCE;
      }

      public UniformMat4 findUniformMat4(String name) {
         return NoopUniform.INSTANCE;
      }
   }

   private static final class StubCoreShader extends StubUniformFinder implements ManagedCoreShader {
      @Override
      public ShaderProgram getProgram() {
         return null;
      }

      @Override
      public void release() {
      }

      @Override
      public SamplerUniform findSampler(String name) {
         return NoopUniform.INSTANCE;
      }
   }

   private static final class StubShaderEffect extends StubUniformFinder implements ManagedShaderEffect {
      @Override
      public PostEffectProcessor getShaderEffect() {
         return null;
      }

      @Override
      public void release() {
      }

      @Override
      public void render(float tickDelta) {
      }

      @Override
      public ManagedFramebuffer getTarget(String name) {
         return new StubFramebuffer();
      }

      @Override
      public void setUniformValue(String name, int value) {
      }

      @Override
      public void setUniformValue(String name, float value) {
      }

      @Override
      public void setUniformValue(String name, float x, float y) {
      }

      @Override
      public void setUniformValue(String name, float x, float y, float z) {
      }

      @Override
      public void setUniformValue(String name, float x, float y, float z, float w) {
      }

      @Override
      public SamplerUniformV2 findSampler(String name) {
         return NoopUniform.INSTANCE;
      }
   }

   private static final class StubFramebuffer implements ManagedFramebuffer {
      @Override
      public Framebuffer getFramebuffer() {
         return null;
      }

      @Override
      public void beginWrite(boolean setViewport) {
      }

      @Override
      public void draw() {
      }

      @Override
      public void draw(int width, int height, boolean disableBlend) {
      }

      @Override
      public void clear() {
      }

      @Override
      public void clear(boolean getError) {
      }
   }
}
