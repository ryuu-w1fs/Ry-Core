package rycore.gui.clickui;

import net.minecraft.client.input.MouseInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.Module;
import rycore.gui.clickui.impl.ManagerPanel;

public class ManagerScreen extends Screen {
   private final Screen parent;
   private final ManagerPanel panel;

   public ManagerScreen(Screen parent) {
      super(Text.of("ManagerScreen"));
      this.parent = parent;
      this.panel = new ManagerPanel();
      this.panel.setOpen(true);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      if (ModuleManager.clickGui.blur.getValue()) {
         this.applyBlur(context);
      }

      if (Module.fullNullCheck()) {
         this.renderBackground(context, mouseX, mouseY, delta);
      }

      this.panel.setSize(400.0F, 250.0F);
      this.panel.render(context, mouseX, mouseY, delta);
   }

   public boolean shouldPause() {
      return false;
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubled) {
      return this.mouseClickedLegacy(click.x(), click.y(), click.button());
   }

   private boolean mouseClickedLegacy(double mouseX, double mouseY, int button) {
      return this.panel.mouseClicked((int)mouseX, (int)mouseY, button) ? true : super.mouseClicked(new Click(mouseX, mouseY, new MouseInput(button, 0)), false);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      this.panel.mouseScrolled(mouseX, mouseY, verticalAmount);
      return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Override
   public boolean keyPressed(KeyInput input) {
      return this.keyPressedLegacy(input.key(), input.scancode(), input.modifiers());
   }

   private boolean keyPressedLegacy(int keyCode, int scanCode, int modifiers) {
      if (this.panel.keyPressed(keyCode)) {
         return true;
      } else if (keyCode == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(new KeyInput(keyCode, scanCode, modifiers));
      }
   }

   @Override
   public boolean charTyped(CharInput input) {
      return this.charTypedLegacy((char)input.codepoint(), input.modifiers());
   }

   private boolean charTypedLegacy(char chr, int modifiers) {
      return this.panel.charTyped(chr) ? true : super.charTyped(new CharInput(chr, modifiers));
   }

   public void close() {
      if (this.client != null) {
         this.client.setScreen(this.parent);
      }
   }
}
