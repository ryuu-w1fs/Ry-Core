package rycore.injection;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.Managers;
import rycore.core.manager.client.CommandManager;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.ClientClickEvent;
import rycore.features.modules.Module;
import rycore.gui.misc.DialogScreen;
import rycore.utility.render.TextureStorage;

@Mixin(Screen.class)
public abstract class MixinScreen {
   @Inject(method = "handleClickEvent", at = @At("HEAD"), cancellable = true)
   private static void onRunCommand(ClickEvent clickEvent, MinecraftClient client, Screen screen, CallbackInfo ci) {
      if (clickEvent instanceof ClientClickEvent clientClickEvent
         && clientClickEvent.getValue().startsWith(Managers.COMMAND.getPrefix())) {
         try {
            CommandManager manager = Managers.COMMAND;
            manager.getDispatcher().execute(clientClickEvent.getValue().substring(Managers.COMMAND.getPrefix().length()), manager.getSource());
            ci.cancel();
         } catch (CommandSyntaxException var5) {
         }
      }
   }

   @Inject(method = "onFilesDropped", at = @At("HEAD"))
   public void filesDragged(List<Path> paths, CallbackInfo ci) {
      String configPath = paths.get(0).toString();
      File cfgFile = new File(configPath);
      String fileName = cfgFile.getName();
      if (fileName.contains(".vc")) {
         DialogScreen dialogScreen = new DialogScreen(
            TextureStorage.setting, "Config detected!", "Are you sure you want to load " + fileName + "?", "Yes", "No", () -> {
               Managers.MODULE.onUnload("none");
               Managers.CONFIG.load(cfgFile);
               Managers.MODULE.onLoad("none");
               Module.mc.setScreen(null);
            }, () -> Module.mc.setScreen(null)
         );
         Module.mc.setScreen(dialogScreen);
      }
   }

   @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
   private void renderInGameBackground(CallbackInfo info) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.disableGuiBackGround.getValue()) {
         info.cancel();
      }
   }

   @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
   public void onRenderBackground(DrawContext context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
      if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.disableGuiBackGround.getValue() && Module.mc.world != null) {
         ci.cancel();
      }
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void captureGuiContext(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      // Moi screen deu di qua day, nen tang ve 2D luon co DrawContext cua frame.
      rycore.utility.render.GuiContext.set(context);
   }
}
