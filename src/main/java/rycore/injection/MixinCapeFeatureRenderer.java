package rycore.injection;

import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Ve cape tuy chinh cua CustomModel.
 *
 * <p>Tam thoi khong hook: tu 1.21.2 {@code render} nhan
 * {@code (MatrixStack, OrderedRenderCommandQueue, int, S renderState, float, float)}
 * thay vi nhan entity, nen CustomModel khong co doi tuong de quyet dinh an/hien.
 * Can cho CustomModel doc tu RenderState truoc khi noi lai hook nay.
 */
@Mixin(CapeFeatureRenderer.class)
public class MixinCapeFeatureRenderer {
}
