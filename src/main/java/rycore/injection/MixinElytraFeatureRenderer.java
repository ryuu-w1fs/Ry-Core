package rycore.injection;

import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * An elytra khi CustomModel bat.
 *
 * <p>Tam thoi khong hook: tu 1.21.2 {@code render} nhan
 * {@code (MatrixStack, OrderedRenderCommandQueue, int, S renderState, float, float)}
 * thay vi nhan entity, nen CustomModel khong co doi tuong de quyet dinh an/hien.
 * Can cho CustomModel doc tu RenderState truoc khi noi lai hook nay.
 */
@Mixin(ElytraFeatureRenderer.class)
public class MixinElytraFeatureRenderer {
}
