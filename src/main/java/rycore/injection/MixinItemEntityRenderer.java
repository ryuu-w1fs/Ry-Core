package rycore.injection;

import net.minecraft.client.render.entity.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Diem vao cua module ItemPhysics.
 *
 * <p>Tam thoi khong hook: tu 1.21.2 {@code BakedModel} va
 * {@code ItemRenderer.getModel(...)} da bi xoa, item duoc ve qua
 * {@code ItemRenderState} dung theo tung layer. ItemPhysics can danh sach
 * {@code BakedQuad} de xac dinh item phang hay khoi, va can ve lai model nhieu
 * lan voi ma tran rieng - ca hai deu phai viet lai theo API moi.
 *
 * <p>Khi chua co ban viet lai, item roi duoi dat ve theo vanilla.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer {
}
