package rycore.injection;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rycore.Rycore;
import rycore.core.Managers;
import rycore.features.modules.misc.UnHook;
import rycore.utility.render.TextureStorage;

@Mixin(PlayerListEntry.class)
public class MixinPlayerListEntry {
   @Unique
   private boolean loadedCapeTexture;
   @Unique
   private Identifier customCapeTexture;
   @Unique
   private String playerName;

   @Inject(method = "<init>(Lcom/mojang/authlib/GameProfile;Z)V", at = @At("TAIL"))
   private void initHook(GameProfile profile, boolean secureChatEnforced, CallbackInfo ci) {
      this.playerName = profile.name();
      this.getTexture(profile);
   }

   @Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
   private void getCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
      if (UnHook.isActive()) {
         this.customCapeTexture = null;
      } else {
         this.ensureFriendCape();
         if (this.customCapeTexture != null) {
            SkinTextures prev = (SkinTextures)cir.getReturnValue();
            // SkinTextures nhan AssetInfo.TextureAsset (co ca id() va
            // texturePath()), khong phai Identifier.
            Identifier capeId = this.customCapeTexture;
            net.minecraft.util.AssetInfo.TextureAsset cape = new net.minecraft.util.AssetInfo.TextureAsset() {
               @Override
               public Identifier id() {
                  return capeId;
               }

               @Override
               public Identifier texturePath() {
                  return capeId;
               }
            };
            SkinTextures newTextures = new SkinTextures(
               prev.body(), cape, cape, prev.model(), prev.secure()
            );
            cir.setReturnValue(newTextures);
         }
      }
   }

   @Unique
   private void getTexture(GameProfile profile) {
      if (!this.loadedCapeTexture) {
         this.loadedCapeTexture = true;
         Util.getMainWorkerExecutor().execute(() -> {
            String name = profile.name();
            if (this.isStarcapeTarget(name)) {
               this.customCapeTexture = TextureStorage.starCape;
            }
         });
      }
   }

   @Unique
   private void ensureFriendCape() {
      if (this.playerName != null && !this.playerName.isEmpty()) {
         if (this.isStarcapeTarget(this.playerName)) {
            this.customCapeTexture = TextureStorage.starCape;
         } else {
            if (this.customCapeTexture == TextureStorage.starCape) {
               this.customCapeTexture = null;
            }
         }
      }
   }

   private boolean isStarcapeTarget(String name) {
      if (name != null && !name.isEmpty()) {
         if (Rycore.mc.player != null) {
            String selfName = Rycore.mc.player.getName().getString();
            if (selfName.equalsIgnoreCase(name)) {
               return true;
            }
         }

         return Managers.FRIEND.isFriend(name);
      } else {
         return false;
      }
   }
}
