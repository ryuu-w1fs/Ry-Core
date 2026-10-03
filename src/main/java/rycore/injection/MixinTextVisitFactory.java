package rycore.injection;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.misc.NameProtect;
import rycore.features.modules.misc.StreamerMode;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory {
   @ModifyArg(
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
         ordinal = 0
      ),
      method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
      index = 0
   )
   private static String adjustText(String text) {
      String result = ModuleManager.nameProtect.isEnabled() ? NameProtect.protectText(text) : text;
      return StreamerMode.filter(result);
   }
}
