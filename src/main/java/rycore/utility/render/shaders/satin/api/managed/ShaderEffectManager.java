package rycore.utility.render.shaders.satin.api.managed;

import java.util.function.Consumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.util.Identifier;
import rycore.utility.render.shaders.satin.impl.ReloadableShaderEffectManager;

public interface ShaderEffectManager {
   static ShaderEffectManager getInstance() {
      return ReloadableShaderEffectManager.INSTANCE;
   }

   ManagedShaderEffect manage(Identifier var1);

   ManagedShaderEffect manage(Identifier var1, Consumer<ManagedShaderEffect> var2);

   ManagedCoreShader manageCoreShader(Identifier var1);

   ManagedCoreShader manageCoreShader(Identifier var1, VertexFormat var2);

   ManagedCoreShader manageCoreShader(Identifier var1, VertexFormat var2, Consumer<ManagedCoreShader> var3);
}
