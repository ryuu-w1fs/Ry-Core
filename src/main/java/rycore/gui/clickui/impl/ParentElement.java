package rycore.gui.clickui.impl;

import java.awt.Color;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import rycore.core.Managers;
import rycore.gui.clickui.AbstractElement;
import rycore.gui.clickui.ModuleButton;
import rycore.gui.font.FontRenderers;
import rycore.setting.Setting;
import rycore.setting.impl.SettingGroup;
import rycore.utility.render.TextureStorage;
import rycore.utility.render.animation.AnimationUtility;

import rycore.utility.render.MatrixCompat;

public class ParentElement extends AbstractElement {
   private final ModuleButton owner;
   private final Setting<SettingGroup> parentSetting;
   private float animation;

   public ParentElement(ModuleButton owner, Setting<?> setting) {
      super(setting);
      this.owner = owner;
      this.parentSetting = (Setting<SettingGroup>)setting;
   }

   @Override
   public void init() {
      this.animation = this.getParentSetting().getValue().isExtended() ? 0.0F : 1.0F;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      MatrixStack matrixStack = MatrixCompat.toMatrixStack(context.getMatrices());
      this.drawGroupGuide(context, this.height);
      float tx = this.x + this.width - 11.0F;
      float ty = this.y + 7.5F;
      this.animation = AnimationUtility.fast(this.animation, this.getParentSetting().getValue().isExtended() ? 0.0F : 1.0F, 15.0F);
      matrixStack.push();
      matrixStack.translate(tx, ty, 0.0F);
      matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-180.0F * this.animation));
      matrixStack.translate(-tx, -ty, 0.0F);
      matrixStack.translate(this.x + this.width - 14.0F, this.y + 4.5F, 0.0F);
      context.drawTexture(RenderPipelines.GUI_TEXTURED, TextureStorage.guiArrow, 0, 0, 0.0F, 0.0F, 6, 6, 6, 6);
      matrixStack.translate(-(this.x + this.width - 14.0F), -(this.y + 4.5F), 0.0F);
      matrixStack.pop();
      FontRenderers.sf_medium_mini
         .drawString(matrixStack, this.setting.getName(), this.getSettingNameX(), this.y + this.height / 2.0F - 1.0F, new Color(-1).getRGB());
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button) {
      if (this.hovered) {
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

      super.mouseClicked(mouseX, mouseY, button);
   }

   public Setting<SettingGroup> getParentSetting() {
      return this.parentSetting;
   }
}
