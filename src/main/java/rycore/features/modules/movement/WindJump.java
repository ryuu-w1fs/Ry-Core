package rycore.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import rycore.core.manager.client.ModuleManager;
import rycore.events.impl.EventPostSync;
import rycore.events.impl.EventSync;
import rycore.features.modules.Module;
import rycore.features.modules.combat.Aura;
import rycore.setting.Setting;
import rycore.setting.impl.Bind;
import rycore.utility.player.InventoryUtility;
import rycore.utility.player.SearchInvResult;

import rycore.utility.player.InputCompat;

public final class WindJump extends Module {
   private final Setting<Bind> activationBind = new Setting<>("Bind", new Bind(-1, false, false));
   private static final float WIND_CHARGE_PITCH = 90.0F;
   private boolean activationPressed = false;
   private boolean jumpRequested = false;
   private boolean restoreJumpInput = false;
   private boolean previousJumpPressed = false;
   private boolean silentRotationActive = false;
   private boolean auraPausedByWindJump = false;
   private boolean previousAuraPause = false;
   private boolean resumeAuraOnPostSync = false;
   private int pendingRestoreSlot = -1;

   public WindJump() {
      super("WindJump", "Uses wind charge to boost jump.", Module.Category.MOVEMENT);
   }

   @Override
   public void onUpdate() {
      this.restoreJumpInput();
      boolean pressed = this.isKeyPressed(this.activationBind);
      if (!pressed) {
         this.activationPressed = false;
      } else {
         if (!this.activationPressed && this.canStart()) {
            this.pauseAuraIfNeeded();
            this.jumpRequested = true;
         }

         this.activationPressed = true;
      }
   }

   @Override
   public void onDisable() {
      this.jumpRequested = false;
      this.silentRotationActive = false;
      this.activationPressed = false;
      this.resumeAuraOnPostSync = false;
      this.restorePendingSlot();
      this.resumeAura();
      this.restoreJumpInput();
   }

   @EventHandler(priority = -200)
   private void onSync(EventSync event) {
      this.restorePendingSlot();
      if (this.jumpRequested) {
         this.jumpRequested = false;
         if (this.canUseWindCharge() && mc.player.isOnGround()) {
            this.pauseAuraIfNeeded();
            this.silentRotationActive = true;
            mc.player.setPitch(90.0F);
            this.performWindJump();
         } else {
            this.resetRequest();
         }
      }
   }

   @EventHandler
   private void onPostSync(EventPostSync event) {
      if (this.resumeAuraOnPostSync) {
         this.resumeAuraOnPostSync = false;
         this.resumeAura();
      }
   }

   private boolean canStart() {
      return this.pendingRestoreSlot == -1 && !this.silentRotationActive && this.canUseWindCharge() && mc.player.isOnGround();
   }

   private boolean canUseWindCharge() {
      return !fullNullCheck() && mc.currentScreen == null && InventoryUtility.findItemInHotBar(Items.WIND_CHARGE).found();
   }

   private void performWindJump() {
      SearchInvResult windCharge = InventoryUtility.findItemInHotBar(Items.WIND_CHARGE);
      if (!windCharge.found()) {
         this.endSilentRotation();
      } else {
         int previousSlot = mc.player.getInventory().getSelectedSlot();
         boolean switched = false;

         try {
            InventoryUtility.switchTo(windCharge.slot());
            switched = true;
            this.performWindChargeAction();
         } finally {
            if (switched && previousSlot != windCharge.slot()) {
               this.pendingRestoreSlot = previousSlot;
            }

            this.endSilentRotation();
         }
      }
   }

   private void performWindChargeAction() {
      this.pressJumpInput();
      if (mc.interactionManager != null) {
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         mc.player.swingHand(Hand.MAIN_HAND);
      }
   }

   private void endSilentRotation() {
      this.silentRotationActive = false;
      if (this.pendingRestoreSlot == -1) {
         this.resumeAuraOnPostSync = true;
      }
   }

   private void restorePendingSlot() {
      if (this.pendingRestoreSlot != -1 && !fullNullCheck()) {
         InventoryUtility.switchTo(this.pendingRestoreSlot);
         this.pendingRestoreSlot = -1;
         this.resumeAuraOnPostSync = false;
         this.resumeAura();
      }
   }

   private void pressJumpInput() {
      this.previousJumpPressed = mc.options.jumpKey.isPressed();
      mc.options.jumpKey.setPressed(true);
      InputCompat.setJumping(mc.player.input, true);
      if (mc.player.isOnGround()) {
         mc.player.jump();
      }

      this.restoreJumpInput = true;
   }

   private void restoreJumpInput() {
      if (this.restoreJumpInput) {
         mc.options.jumpKey.setPressed(this.previousJumpPressed);
         if (mc.player != null) {
            InputCompat.setJumping(mc.player.input, this.previousJumpPressed);
         }

         this.restoreJumpInput = false;
      }
   }

   private void resetRequest() {
      this.jumpRequested = false;
      this.silentRotationActive = false;
      this.resumeAuraOnPostSync = false;
      this.resumeAura();
   }

   private void pauseAuraIfNeeded() {
      if (!this.auraPausedByWindJump) {
         if (this.shouldPauseAura()) {
            this.previousAuraPause = ModuleManager.aura.externalPause;
            ModuleManager.aura.externalPause = true;
            ModuleManager.moveFix.fixRotation = Float.NaN;
            this.auraPausedByWindJump = true;
         }
      }
   }

   private void resumeAura() {
      if (this.auraPausedByWindJump && ModuleManager.aura != null) {
         ModuleManager.aura.externalPause = this.previousAuraPause;
         this.auraPausedByWindJump = false;
         this.previousAuraPause = false;
      } else {
         this.auraPausedByWindJump = false;
         this.previousAuraPause = false;
      }
   }

   private boolean shouldPauseAura() {
      return ModuleManager.aura != null && ModuleManager.aura.isEnabled() && Aura.target != null && ModuleManager.aura.rotationMode.not(Aura.Mode.None);
   }
}
