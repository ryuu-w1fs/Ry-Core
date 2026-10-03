package rycore.features.modules.player;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import rycore.utility.player.ItemCompat;
import rycore.core.Managers;
import rycore.features.modules.Module;
import rycore.gui.notification.Notification;
import rycore.setting.Setting;
import rycore.utility.player.InventoryUtility;
import rycore.utility.player.SearchInvResult;

public class ElytraReplace extends Module {
   private final Setting<Integer> durability = new Setting<>("Durability", 5, 0, 100);

   public ElytraReplace() {
      super("ElytraReplace", "Replaces broken elytras.", Module.Category.PLAYER);
   }

   @Override
   public void onUpdate() {
      ItemStack is = mc.player.getEquippedStack(EquipmentSlot.CHEST);
      if (is.isOf(Items.ELYTRA) && 100.0F - (float)is.getDamage() / is.getMaxDamage() * 100.0F <= this.durability.getValue().intValue()) {
         SearchInvResult result = InventoryUtility.findInInventory(
            stack -> ItemCompat.isGlider(stack)
               ? 100.0F - (float)stack.getDamage() / stack.getMaxDamage() * 100.0F > this.durability.getValue().intValue()
               : false
         );
         if (result.found()) {
            clickSlot(result.slot());
            clickSlot(6);
            clickSlot(result.slot());
            this.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            Managers.NOTIFICATION.publicity("ElytraReplace", "Swapping the old elytra for a new one!", 2, Notification.Type.SUCCESS);
            this.sendMessage("Swapping the old elytra for a new one!");
         }
      }
   }
}
