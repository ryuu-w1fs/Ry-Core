package rycore;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rycore.core.Core;
import rycore.core.Managers;
import rycore.core.hooks.ManagerShutdownHook;
import rycore.core.hooks.ModuleShutdownHook;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.render.popeffect.PopEffectParticles;
import rycore.utility.player.MovementUtility;
import rycore.utility.render.Render2DEngine;

public class Rycore implements ModInitializer {
   public static final ModMetadata MOD_META = ((ModContainer)FabricLoader.getInstance().getModContainer("rycore").orElseThrow()).getMetadata();
   public static final String RANK = "DEV";
   public static final String USER = "vu_2007";
   public static final String MOD_ID = "rycore";
   public static final String VERSION = "Release";
   public static final Logger LOGGER = LoggerFactory.getLogger("Rycore");
   public static final Runtime RUNTIME = Runtime.getRuntime();
   public static final boolean baritone = FabricLoader.getInstance().isModLoaded("baritone") || FabricLoader.getInstance().isModLoaded("baritone-meteor");
   public static final IEventBus EVENT_BUS = new EventBus();
   public static Color copy_color = new Color(-1);
   public static Rycore.KeyListening currentKeyListener;
   public static BlockPos gps_position;
   public static float TICK_TIMER = 1.0F;
   public static float FRAG_EFFECT_TIMER = 1.0F;
   public static MinecraftClient mc;
   public static long initTime;
   public static Core core = new Core();

   public void onInitialize() {
      mc = MinecraftClient.getInstance();
      initTime = System.currentTimeMillis();
      EVENT_BUS.registerLambdaFactory("rycore", (lookupInMethod, klass) -> (Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
      EVENT_BUS.subscribe(core);
      this.preloadRuntimeClasses();
      PopEffectParticles.register();
      Managers.init();
      Managers.subscribe();
      Render2DEngine.initShaders();
      ModuleManager.rpc.startRpc();
      LOGGER.info("[Rycore] Init time: {} ms.", System.currentTimeMillis() - initTime);
      initTime = System.currentTimeMillis();
      RUNTIME.addShutdownHook(new ManagerShutdownHook());
      RUNTIME.addShutdownHook(new ModuleShutdownHook());
   }

   private void preloadRuntimeClasses() {
      try {
         Class.forName(MovementUtility.class.getName(), true, Rycore.class.getClassLoader());
      } catch (ClassNotFoundException exception) {
         throw new IllegalStateException("Không thể nạp class runtime bắt buộc của Rycore.", exception);
      }
   }

   public static boolean isFuturePresent() {
      return FabricLoader.getInstance().getModContainer("future").isPresent();
   }

   public enum KeyListening {
      ClickGui,
      Search,
      Sliders,
      Strings;
   }
}
