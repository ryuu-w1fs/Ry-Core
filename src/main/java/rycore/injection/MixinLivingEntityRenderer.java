package rycore.injection;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Diem vao cho Rotations (xoay dau/than theo huong server) va module Hat.
 *
 * <p>Tam thoi khong hook: tu 1.21.2 {@code LivingEntityRenderer.render} nhan
 * {@code (S renderState, MatrixStack, OrderedRenderCommandQueue, int)} thay vi nhan
 * entity. Ca bon hook cu deu doc/ghi truc tiep {@code headYaw}, {@code bodyYaw},
 * {@code lastHeadYaw}... tren entity - nhung gia tri nay gio duoc entity renderer
 * copy vao RenderState truoc khi ve, nen phai sua tai cho dien state thay vi tai
 * cho ve.
 *
 * <p>He qua khi chua viet lai: Rotations khong hien goc xoay server tren model
 * nguoi choi, va module Hat khong ve. Cac phan khac cua hai module van hoat dong.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
}
