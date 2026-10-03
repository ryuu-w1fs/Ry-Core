package rycore.core.manager.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import rycore.Rycore;
import rycore.core.manager.IManager;
import rycore.features.hud.HudElement;
import rycore.features.modules.Module;
import rycore.features.modules.combat.AimBot;
import rycore.features.modules.combat.AntiBot;
import rycore.features.modules.combat.Aura;
import rycore.features.modules.combat.AutoBuff;
import rycore.features.modules.combat.AutoCart;
import rycore.features.modules.combat.AutoCrystal;
import rycore.features.modules.combat.AutoGApple;
import rycore.features.modules.combat.AutoTotem;
import rycore.features.modules.combat.BowSpam;
import rycore.features.modules.combat.Criticals;
import rycore.features.modules.combat.CrystalOptimizer;
import rycore.features.modules.combat.EMaceHelper;
import rycore.features.modules.combat.ElytraTarget;
import rycore.features.modules.combat.HitBox;
import rycore.features.modules.combat.MaceKill;
import rycore.features.modules.combat.MaceSwap;
import rycore.features.modules.combat.Reach;
import rycore.features.modules.combat.TargetStrafe;
import rycore.features.modules.combat.TriggerBot;
import rycore.features.modules.combat.WallsBypass;
import rycore.features.modules.misc.StreamerMode;import rycore.features.modules.misc.AntiAttack;
import rycore.features.modules.misc.AntiCrash;
import rycore.features.modules.misc.AntiServerRP;
import rycore.features.modules.misc.AutoAuth;
import rycore.features.modules.misc.AutoTpAccept;
import rycore.features.modules.misc.ChestStealer;
import rycore.features.modules.misc.ClickGui;
import rycore.features.modules.misc.ClientSettings;
import rycore.features.modules.misc.ClientSound;
import rycore.features.modules.misc.CombatLeave;
import rycore.features.modules.misc.FakePlayer;
import rycore.features.modules.misc.FixHP;
import rycore.features.modules.misc.LagNotifier;
import rycore.features.modules.misc.MessageAppend;
import rycore.features.modules.misc.NameProtect;
import rycore.features.modules.misc.Notifications;
import rycore.features.modules.misc.Nuker;
import rycore.features.modules.misc.RPC;
import rycore.features.modules.misc.Spammer;
import rycore.features.modules.misc.TotemPopCounter;
import rycore.features.modules.misc.UnHook;
import rycore.features.modules.misc.XRay;
import rycore.features.modules.movement.AirStuck;
import rycore.features.modules.movement.AntiWeb;
import rycore.features.modules.movement.AutoSprint;
import rycore.features.modules.movement.AutoWalk;
import rycore.features.modules.movement.ElytraBoost;
import rycore.features.modules.movement.ElytraMotion;
import rycore.features.modules.movement.ElytraPlus;
import rycore.features.modules.movement.Flight;
import rycore.features.modules.movement.FreeLook;
import rycore.features.modules.movement.GuiMove;
import rycore.features.modules.movement.MoveFix;
import rycore.features.modules.movement.NoFall;
import rycore.features.modules.movement.NoSlow;
import rycore.features.modules.movement.Phase;
import rycore.features.modules.movement.Speed;
import rycore.features.modules.movement.TridentBoost;
import rycore.features.modules.movement.Velocity;
import rycore.features.modules.movement.WaterSpeed;
import rycore.features.modules.movement.WindJump;
import rycore.features.modules.player.AutoEat;
import rycore.features.modules.player.AutoRespawn;
import rycore.features.modules.player.AutoTool;
import rycore.features.modules.player.ClickAction;
import rycore.features.modules.player.DurabilityAlert;
import rycore.features.modules.player.ElytraReplace;
import rycore.features.modules.player.ElytraSwap;
import rycore.features.modules.player.FreeCam;
import rycore.features.modules.player.HotbarReplenish;
import rycore.features.modules.player.InventoryCleaner;
import rycore.features.modules.player.ItemHelper;
import rycore.features.modules.player.ItemScroller;
import rycore.features.modules.player.NoDelay;
import rycore.features.modules.player.NoEntityTrace;
import rycore.features.modules.player.NoInteract;
import rycore.features.modules.player.NoPush;
import rycore.features.modules.player.PearlChaser;
import rycore.features.modules.player.PerfectDelay;
import rycore.features.modules.player.PortalInventory;
import rycore.features.modules.player.TapeMouse;
import rycore.features.modules.render.Animations;
import rycore.features.modules.render.Arrows;
import rycore.features.modules.render.AspectRatio;
import rycore.features.modules.render.BlockESP;
import rycore.features.modules.render.ChunkAnimation;
import rycore.features.modules.render.Crosshair;
import rycore.features.modules.render.CustomModel;
import rycore.features.modules.render.ESP;
import rycore.features.modules.render.FragEffects;
import rycore.features.modules.render.Fullbright;
import rycore.features.modules.render.Hat;
import rycore.features.modules.render.HudEditor;
import rycore.features.modules.render.ItemESP;
import rycore.features.modules.render.ItemPhysics;
import rycore.features.modules.render.JumpCircle;
import rycore.features.modules.render.NoCameraClip;
import rycore.features.modules.render.NoRender;
import rycore.features.modules.render.Particles;
import rycore.features.modules.render.PopEffect;
import rycore.features.modules.render.Prediction;
import rycore.features.modules.render.SmoothCamera;
import rycore.features.modules.render.StorageEsp;
import rycore.features.modules.render.Svetych;
import rycore.features.modules.render.TargerESP;
import rycore.features.modules.render.Tooltips;
import rycore.features.modules.render.TotemAnimation;
import rycore.features.modules.render.ViewModel;
import rycore.features.modules.render.WorldTweaks;
import rycore.gui.clickui.ClickGUI;

public class ModuleManager implements IManager {
   public ArrayList<Module> modules = new ArrayList<>();
   public List<Integer> activeMouseKeys = new ArrayList<>();
   public static boolean keyPearlAntiPickup = false;
   public static InventoryCleaner inventoryCleaner = new InventoryCleaner();
   public static PortalInventory portalInventory = new PortalInventory();
   public static HotbarReplenish hotbarReplenish = new HotbarReplenish();
   public static TotemPopCounter totemPopCounter = new TotemPopCounter();
   public static DurabilityAlert durabilityAlert = new DurabilityAlert();
   public static TotemAnimation totemAnimation = new TotemAnimation();
   public static ClientSettings clientSettings = new ClientSettings();
   public static MessageAppend messageAppend = new MessageAppend();
   public static ElytraReplace elytraReplace = new ElytraReplace();
   public static Notifications notifications = new Notifications();
   public static NoEntityTrace noEntityTrace = new NoEntityTrace();
   public static NoCameraClip noCameraClip = new NoCameraClip();
   public static TargetStrafe targetStrafe = new TargetStrafe();
   public static ItemScroller itemScroller = new ItemScroller();
   public static ChestStealer chestStealer = new ChestStealer();
   public static AutoTpAccept autoTpAccept = new AutoTpAccept();
   public static AntiServerRP antiServerRP = new AntiServerRP();
   public static PerfectDelay perfectDelay = new PerfectDelay();
   public static TridentBoost tridentBoost = new TridentBoost();
   public static WindJump windJump = new WindJump();
   public static ClientSound ClientSound = new ClientSound();
   public static PearlChaser pearlChaser = new PearlChaser();
   public static WorldTweaks worldTweaks = new WorldTweaks();
   public static NameProtect nameProtect = new NameProtect();
   public static StreamerMode streamerMode = new StreamerMode();
   public static LagNotifier lagNotifier = new LagNotifier();
   public static AutoRespawn autoRespawn = new AutoRespawn();
   public static AspectRatio aspectRatio = new AspectRatio();
   public static SmoothCamera smoothCamera = new SmoothCamera();
   public static ItemPhysics itemPhysics = new ItemPhysics();
   public static WaterSpeed waterSpeed = new WaterSpeed();
   public static TriggerBot triggerBot = new TriggerBot();
   public static StorageEsp storageEsp = new StorageEsp();
   public static NoInteract noInteract = new NoInteract();
   public static JumpCircle jumpCircle = new JumpCircle();
   public static Fullbright fullbright = new Fullbright();
   public static FakePlayer fakePlayer = new FakePlayer();
   public static AutoSprint autoSprint = new AutoSprint();
   public static AutoGApple autoGApple = new AutoGApple();
   public static Animations animations = new Animations();
   public static ChunkAnimation chunkAnimation = new ChunkAnimation();
   public static AntiAttack antiAttack = new AntiAttack();
   public static Prediction prediction = new Prediction();
   public static ElytraSwap elytraSwap = new ElytraSwap();
   public static ElytraPlus elytraPlus = new ElytraPlus();
   public static ElytraTarget elytraTarget = new ElytraTarget();
   public static Particles particles = new Particles();
   public static FragEffects fragEffects = new FragEffects();
   public static ElytraBoost elytraBoost = new ElytraBoost();
   public static ElytraMotion elytraMotion = new ElytraMotion();
   public static TargerESP targerESP = new TargerESP();
   public static ViewModel viewModel = new ViewModel();
   public static CustomModel customModel = new CustomModel();
   public static HudEditor hudEditor = new HudEditor();
   public static Crosshair crosshair = new Crosshair();
   public static Criticals criticals = new Criticals();
   public static TapeMouse tapeMouse = new TapeMouse();
   public static AntiCrash antiCrash = new AntiCrash();
   public static ClickAction keyPearl = new ClickAction();
   public static AutoTotem autoTotem = new AutoTotem();
   public static Velocity velocity = new Velocity();
   public static Tooltips tooltips = new Tooltips();
   public static PopEffect popEffect = new PopEffect();
   public static NoRender noRender = new NoRender();
   public static ClickGui clickGui = new ClickGui();
   public static AutoWalk autoWalk = new AutoWalk();
   public static BlockESP blockESP = new BlockESP();
   public static AirStuck airStuck = new AirStuck();
   public static MaceSwap maceSwap = new MaceSwap();
   public static EMaceHelper eMaceHelper = new EMaceHelper();
   public static WallsBypass wallsBypass = new WallsBypass();
   public static AutoTool autoTool = new AutoTool();
   public static ItemHelper itemHelper = new ItemHelper();
   public static AutoBuff autoBuff = new AutoBuff();
   public static AutoAuth autoAuth = new AutoAuth();
   public static MoveFix moveFix = new MoveFix();
   public static AutoEat autoEat = new AutoEat();
   public static Spammer spammer = new Spammer();
   public static CombatLeave combatLeave = new CombatLeave();
   public static FreeLook freeLook = new FreeLook();
   public static FreeCam freeCam = new FreeCam();
   public static NoDelay nodelay = new NoDelay();
   public static BowSpam bowSpam = new BowSpam();
   public static ItemESP itemESP = new ItemESP();
   public static GuiMove guiMove = new GuiMove();
   public static AntiWeb antiWeb = new AntiWeb();
   public static AntiBot antiBot = new AntiBot();
   public static Arrows Arrows = new Arrows();
   public static NoSlow noSlow = new NoSlow();
   public static NoFall noFall = new NoFall();
   public static HitBox hitBox = new HitBox();
   public static Flight flight = new Flight();
   public static AimBot aimBot = new AimBot();
   public static NoPush noPush = new NoPush();
   public static UnHook unHook = new UnHook();
   public static FixHP fixHP = new FixHP();
   public static Speed speed = new Speed();
   public static Reach reach = new Reach();
   public static Nuker nuker = new Nuker();
   public static Phase phase = new Phase();
   public static XRay xray = new XRay();
   public static AutoCart autoCart = new AutoCart();
   public static AutoCrystal autoCrystal = new AutoCrystal();
   public static CrystalOptimizer crystalOptimizer = new CrystalOptimizer();
   public static Aura aura = new Aura();
   public static MaceKill maceKill = new MaceKill();
   public static ESP esp = new ESP();
   public static Hat hat = new Hat();
   public static Svetych svetych = new Svetych();
   public static RPC rpc = new RPC();

   public ModuleManager() {
      for (Field field : this.getClass().getDeclaredFields()) {
         if (Module.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);

            try {
               this.modules.add((Module)field.get(this));
            } catch (IllegalAccessException e) {
               Rycore.LOGGER.error("Error initializing modules", e);
            }
         }
      }

      hudEditor.enableSilently();
   }

   public Module get(String name) {
      for (Module module : this.modules) {
         if (module.getName().equalsIgnoreCase(name)) {
            return module;
         }
      }

      return null;
   }

   public ArrayList<Module> getEnabledModules() {
      ArrayList<Module> enabledModules = new ArrayList<>();

      for (Module module : this.modules) {
         if (module.isEnabled()) {
            enabledModules.add(module);
         }
      }

      return enabledModules;
   }

   public ArrayList<Module> getModulesByCategory(Module.Category category) {
      ArrayList<Module> modulesCategory = new ArrayList<>();
      this.modules.forEach(module -> {
         if (module.getCategory() == category) {
            modulesCategory.add(module);
         }
      });
      return modulesCategory;
   }

   public List<Module.Category> getCategories() {
      return new ArrayList<>(Module.Category.values());
   }

   public void onLoad(String category) {
      try {
         Rycore.EVENT_BUS.unsubscribe(unHook);
      } catch (Exception var3) {
      }

      unHook.setEnabled(false);
      this.modules.sort(Comparator.comparing(Module::getName));
      this.modules.forEach(m -> {
         boolean shouldEnable = m.isEnabled() && (m.getCategory().getName().equalsIgnoreCase(category) || category.equals("none"));
         if (shouldEnable) {
            m.enableSilently();
         }
      });
      if (ConfigManager.firstLaunch) {
         notifications.enable();
         rpc.enable();
         ClientSound.enable();
      }
   }

   public void onUpdate() {
      if (!Module.fullNullCheck()) {
         this.modules.stream().filter(Module::isEnabled).forEach(Module::onUpdate);
      }
   }

   /** Cursor chuan, tao mot lan: truoc day tao moi frame gay leak handle. */
   private static long arrowCursor;
   private static boolean arrowCursorSet;

   public void onRender2D(DrawContext context) {
      if (!mc.getDebugHud().shouldShowDebugHud() && !mc.options.hudHidden) {
         HudElement.anyHovered = false;
         for (Module module : this.modules) {
            if (!module.isEnabled()) {
               continue;
            }

            try {
               module.onRender2D(context);
            } catch (Throwable t) {
               // Mot module loi khong duoc chan cac module con lai ve.
               Module.logRenderError(module, t);
            }
         }

         if (!HudElement.anyHovered && !ClickGUI.anyHovered && GLFW.glfwGetPlatform() != 393219) {
            if (arrowCursor == 0L) {
               arrowCursor = GLFW.glfwCreateStandardCursor(221185);
            }

            if (!arrowCursorSet) {
               GLFW.glfwSetCursor(mc.getWindow().getHandle(), arrowCursor);
               arrowCursorSet = true;
            }
         } else {
            arrowCursorSet = false;
         }

         Rycore.core.onRender2D(context);
      }
   }

   public void onRender3D(MatrixStack stack) {
      for (Module module : this.modules) {
         if (!module.isEnabled()) {
            continue;
         }

         try {
            module.onRender3D(stack);
         } catch (Throwable t) {
            Module.logRenderError(module, t);
         }
      }
   }

   public void onLogout() {
      this.modules.forEach(Module::onLogout);
   }

   public void onLogin() {
      this.modules.forEach(Module::onLogin);
   }

   public void onUnload(String category) {
      this.modules.forEach(module -> {
         if (module.isEnabled() && (module.getCategory().getName().equalsIgnoreCase(category) || category.equals("none"))) {
            Rycore.EVENT_BUS.unsubscribe(module);
            module.setEnabled(false);
         }
      });
      this.modules.forEach(Module::onUnload);
   }

   public void onKeyPressed(int eventKey) {
      if (eventKey != -1 && eventKey != 0 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
               module.toggle();
            }
         });
      }
   }

   public void onKeyReleased(int eventKey) {
      if (eventKey != -1 && eventKey != 0 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && module.getBind().isHold()) {
               module.disable();
            }
         });
      }
   }

   public void onMoseKeyPressed(int eventKey) {
      if (eventKey != -1 && !(mc.currentScreen instanceof ClickGUI)) {
         this.modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey)) {
               module.toggle();
            }
         });
      }
   }

   public void onMoseKeyReleased(int eventKey) {
      if (eventKey != -1 && !(mc.currentScreen instanceof ClickGUI)) {
         this.activeMouseKeys.add(eventKey);
         this.modules.forEach(module -> {
            if (Objects.equals(module.getBind().getBind(), "M" + eventKey) && module.getBind().isHold()) {
               module.disable();
            }
         });
      }
   }

   public ArrayList<Module> getModulesSearch(String string) {
      ArrayList<Module> modulesCategory = new ArrayList<>();
      this.modules.forEach(module -> {
         if (module.getName().toLowerCase().contains(string.toLowerCase())) {
            modulesCategory.add(module);
         }
      });
      return modulesCategory;
   }

   public void registerModule(Module module) {
      if (module != null) {
         this.modules.add(module);
         if (module.isEnabled()) {
            Rycore.EVENT_BUS.subscribe(module);
         }
      }
   }
}
