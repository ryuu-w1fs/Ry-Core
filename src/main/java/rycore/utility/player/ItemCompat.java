package rycore.utility.player;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

/**
 * Kiem tra loai item theo tag/component.
 *
 * <p>Tu 1.21.2 cac class {@code SwordItem}, {@code PickaxeItem}, {@code AxeItem},
 * {@code ShovelItem}, {@code ArmorItem} va {@code ElytraItem} da bi xoa: moi item
 * dung chung {@code Item} va phan loai bang tag hoac data component.
 */
public final class ItemCompat {
   private ItemCompat() {
   }

   public static boolean isSword(ItemStack stack) {
      return stack != null && stack.isIn(ItemTags.SWORDS);
   }

   public static boolean isAxe(ItemStack stack) {
      return stack != null && stack.isIn(ItemTags.AXES);
   }

   public static boolean isPickaxe(ItemStack stack) {
      return stack != null && stack.isIn(ItemTags.PICKAXES);
   }

   public static boolean isShovel(ItemStack stack) {
      return stack != null && stack.isIn(ItemTags.SHOVELS);
   }

   public static boolean isHoe(ItemStack stack) {
      return stack != null && stack.isIn(ItemTags.HOES);
   }

   /** Dung cho ca vu khi va cong cu - thay cho chuoi instanceof truoc day. */
   public static boolean isToolOrWeapon(ItemStack stack) {
      return isSword(stack) || isPickaxe(stack) || isAxe(stack) || isShovel(stack);
   }

   /**
    * Giap deo duoc tren mot trong bon o armor. Khong co tag "armor" tong hop nen
    * phai hop bon tag theo tung o.
    */
   public static boolean isArmor(ItemStack stack) {
      return stack != null
         && (stack.isIn(ItemTags.HEAD_ARMOR)
            || stack.isIn(ItemTags.CHEST_ARMOR)
            || stack.isIn(ItemTags.LEG_ARMOR)
            || stack.isIn(ItemTags.FOOT_ARMOR));
   }

   /** Bon o giap theo thu tu feet -> head, giong {@code getArmorItems()} truoc day. */
   public static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
      EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
   );

   /** Thay cho {@code LivingEntity.getArmorItems()} da bi xoa tu 1.21.2. */
   public static List<ItemStack> getArmorItems(LivingEntity entity) {
      return ARMOR_SLOTS.stream().map(entity::getEquippedStack).toList();
   }

   /** Item co the bay duoc (elytra va bat ky item mang component GLIDER). */
   public static boolean isGlider(ItemStack stack) {
      return stack != null && stack.contains(DataComponentTypes.GLIDER);
   }

   /**
    * Elytra con dung duoc. Thay cho {@code ElytraItem.isUsable(ItemStack)}:
    * elytra hong khi do ben con lai khong qua 1 diem.
    */
   public static boolean isElytraUsable(ItemStack stack) {
      if (stack == null || !stack.isOf(Items.ELYTRA)) {
         return false;
      }

      return stack.getDamage() < stack.getMaxDamage() - 1;
   }
}
