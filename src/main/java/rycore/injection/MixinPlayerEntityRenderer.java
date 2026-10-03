package rycore.injection;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Diem vao cua CustomModel (texture va pham vi render cua model nguoi choi).
 *
 * <p>Tam thoi khong hook: tu 1.21.2 ca ba method cu doi chu ky sang RenderState -
 * {@code render(S, MatrixStack, OrderedRenderCommandQueue, CameraRenderState)} va
 * {@code getTexture(PlayerEntityRenderState)} - khong con
 * {@code AbstractClientPlayerEntity} de CustomModel nhan dien nguoi choi dang ve.
 * Can cho CustomModel doc tu PlayerEntityRenderState truoc khi noi lai.
 *
 * <p>Khi chua viet lai: skin/model nguoi choi ve theo vanilla.
 */
@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
}
