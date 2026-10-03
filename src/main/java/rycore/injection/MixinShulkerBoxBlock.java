package rycore.injection;

import java.util.List;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.render.Tooltips;

@Mixin(ShulkerBoxBlock.class)
public class MixinShulkerBoxBlock {
   // 1.21.11: Block.appendTooltip da bi xoa; tooltip di qua
   // TooltipAppender cua component.
}
