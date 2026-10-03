package rycore.injection;

import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Diem vao cho CustomModel chinh vi tri item tren tay.
 *
 * <p>Tam thoi khong hook: tu ban moi {@code HeldItemFeatureRenderer.renderItem} nhan
 * {@code (S renderState, ItemRenderState, ItemStack, Arm, MatrixStack,
 * OrderedRenderCommandQueue, int)} - khong con LivingEntity de CustomModel quyet dinh
 * co bien doi hay khong, va {@code ModelWithArms.setArmAngle} gio can RenderState.
 * Can viet lai CustomModel theo RenderState truoc khi noi lai hook nay.
 *
 * <p>Khi chua co ban viet lai, item tren tay ve theo vanilla.
 */
@Mixin(HeldItemFeatureRenderer.class)
public abstract class MixinHeldItemFeatureRenderer {
}
