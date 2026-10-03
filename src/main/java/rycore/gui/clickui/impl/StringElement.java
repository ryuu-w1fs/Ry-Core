package rycore.gui.clickui.impl;

import java.awt.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;
import rycore.Rycore;
import rycore.features.modules.Module;
import rycore.gui.clickui.AbstractElement;
import rycore.gui.clickui.ClickGUI;
import rycore.gui.font.FontRenderers;
import rycore.setting.Setting;
import rycore.utility.render.Render2DEngine;

public class StringElement extends AbstractElement {
   public boolean listening;
   private String currentString = "";

   public StringElement(Setting setting) {
      super(setting);
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.drawGroupGuide(context, this.height);
      Render2DEngine.drawClickGuiRect(
         context.getMatrices(), this.getX() + 5.0F, this.getY() + 2.0F, this.getWidth() - 11.0F, 10.0F, new Color(-1811939328, true)
      );
      FontRenderers.sf_medium_mini
         .drawString(
            context.getMatrices(),
            this.listening ? this.currentString + (Module.mc.player != null && Module.mc.player.age % 5 != 0 ? "" : "_") : (String)this.setting.getValue(),
            this.getSettingNameX(),
            this.y + this.height / 2.0F,
            -1
         );
      if (Render2DEngine.isHovered(mouseX, mouseY, this.getX() + 5.0F, this.getY() + 2.0F, this.getWidth() - 11.0F, 10.0)) {
         if (GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(Module.mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221186));
         }

         ClickGUI.anyHovered = true;
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.hovered && button == 0) {
         this.listening = !this.listening;
      }

      if (this.listening) {
         Rycore.currentKeyListener = Rycore.KeyListening.Strings;
         this.currentString = (String)this.setting.getValue();
      }

      super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public void charTyped(char key, int keyCode) {
      if (StringHelper.isValidChar(key)) {
         this.currentString = this.currentString + key;
      }
   }

   @Override
   public void keyTyped(int keyCode) {
      if (Rycore.currentKeyListener == Rycore.KeyListening.Strings) {
         if (this.listening) {
            switch (keyCode) {
               case 32:
                  this.currentString = this.currentString + " ";
               case 256:
               default:
                  break;
               case 257:
                  this.setting.setValue(this.currentString != null && !this.currentString.isEmpty() ? this.currentString : this.setting.getDefaultValue());
                  this.currentString = "";
                  this.listening = !this.listening;
                  break;
               case 259:
                  this.currentString = SliderElement.removeLastChar(this.currentString);
            }
         }
      }
   }
}
