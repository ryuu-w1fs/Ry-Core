package rycore.gui.clickui.impl;

import java.awt.Color;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;
import rycore.core.Managers;
import rycore.core.manager.IManager;
import rycore.gui.clickui.AbstractElement;
import rycore.gui.clickui.ClickGUI;
import rycore.gui.clickui.ModuleButton;
import rycore.gui.font.FontRenderers;
import rycore.setting.Setting;
import rycore.setting.impl.BooleanSettingGroup;
import rycore.utility.render.Render2DEngine;
import rycore.utility.render.SettingControlRenderer;
import rycore.utility.render.TextureStorage;
import rycore.utility.render.animation.AnimationUtility;

import rycore.utility.render.MatrixCompat;

public class BooleanParentElement extends AbstractElement {
   private static final float CHECKBOX_RIGHT_PADDING = 22.0F;
   private final ModuleButton owner;
   private final Setting<BooleanSettingGroup> parentSetting;
   float animation;
   float arrowAnimation;

   public BooleanParentElement(ModuleButton owner, Setting<BooleanSettingGroup> setting) {
      super(setting);
      this.owner = owner;
      this.parentSetting = setting;
   }

   @Override
   public void init() {
      this.animation = this.getParentSetting().getValue().isEnabled() ? 1.0F : 0.0F;
      this.arrowAnimation = this.getParentSetting().getValue().isExtended() ? 0.0F : 1.0F;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      MatrixStack matrixStack = MatrixCompat.toMatrixStack(context.getMatrices());
      float tx = this.x + this.width - 11.0F;
      float ty = this.y + 7.5F;
      this.drawGroupGuide(context, this.height);
      this.arrowAnimation = AnimationUtility.fast(this.arrowAnimation, this.getParentSetting().getValue().isExtended() ? 0.0F : 1.0F, 15.0F);
      matrixStack.push();
      matrixStack.translate(tx, ty, 0.0F);
      matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-180.0F * this.arrowAnimation));
      matrixStack.translate(-tx, -ty, 0.0F);
      matrixStack.translate(this.x + this.width - 14.0F, this.y + 4.5F, 0.0F);
      context.drawTexture(RenderPipelines.GUI_TEXTURED, TextureStorage.guiArrow, 0, 0, 0.0F, 0.0F, 6, 6, 6, 6);
      matrixStack.translate(-(this.x + this.width - 14.0F), -(this.y + 4.5F), 0.0F);
      matrixStack.pop();
      FontRenderers.sf_medium_mini
         .drawString(matrixStack, this.setting.getName(), this.getSettingNameX(), this.y + this.height / 2.0F - 1.0F, new Color(-1).getRGB());
      this.animation = AnimationUtility.fast(this.animation, this.getParentSetting().getValue().isEnabled() ? 1.0F : 0.0F, 15.0F);
      float checkboxX = this.x + this.width - 10.0F - 22.0F;
      float checkboxY = this.y + this.height / 2.0F - 5.0F;
      SettingControlRenderer.drawCheckbox(context.getMatrices(), checkboxX, checkboxY, this.animation);
      if (Render2DEngine.isHovered(mouseX, mouseY, checkboxX, checkboxY, 10.0, 10.0)) {
         if (GLFW.glfwGetPlatform() != 393219) {
            GLFW.glfwSetCursor(IManager.mc.getWindow().getHandle(), GLFW.glfwCreateStandardCursor(221188));
         }

         ClickGUI.anyHovered = true;
      }
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.hovered) {
         if (button == 0) {
            this.getParentSetting().getValue().setEnabled(!this.getParentSetting().getValue().isEnabled());
            Managers.SOUND.playBoolean();
         } else {
            boolean opening = !this.getParentSetting().getValue().isExtended();
            if (opening) {
               this.owner.closeSiblingSettingPanels(this.getParentSetting());
            }

            this.getParentSetting().getValue().setExtended(opening);
            if (opening) {
               Managers.SOUND.playSwipeIn();
            } else {
               Managers.SOUND.playSwipeOut();
            }
         }
      }

      super.mouseClicked(mouseX, mouseY, button);
   }

   public Setting<BooleanSettingGroup> getParentSetting() {
      return this.parentSetting;
   }
}
