package rycore.core;

import rycore.Rycore;
import rycore.core.manager.client.AsyncManager;
import rycore.core.manager.client.CommandManager;
import rycore.core.manager.client.ConfigManager;
import rycore.core.manager.client.MacroManager;
import rycore.core.manager.client.ModuleManager;
import rycore.core.manager.client.NotificationManager;
import rycore.core.manager.client.ServerManager;
import rycore.core.manager.client.SoundManager;
import rycore.core.manager.player.CombatManager;
import rycore.core.manager.player.FriendManager;
import rycore.core.manager.player.PlayerManager;

public class Managers {
   public static final CombatManager COMBAT = new CombatManager();
   public static final FriendManager FRIEND = new FriendManager();
   public static final PlayerManager PLAYER = new PlayerManager();
   public static final AsyncManager ASYNC = new AsyncManager();
   public static final ModuleManager MODULE = new ModuleManager();
   public static final ConfigManager CONFIG = new ConfigManager();
   public static final MacroManager MACRO = new MacroManager();
   public static final NotificationManager NOTIFICATION = new NotificationManager();
   public static final ServerManager SERVER = new ServerManager();
   public static final SoundManager SOUND = new SoundManager();
   public static final CommandManager COMMAND = new CommandManager();

   public static void init() {
      CONFIG.load(CONFIG.getCurrentConfig());
      MODULE.onLoad("none");
      FRIEND.loadFriends();
      MACRO.onLoad();
      SOUND.registerSounds();
   }

   public static void subscribe() {
      Rycore.EVENT_BUS.subscribe(NOTIFICATION);
      Rycore.EVENT_BUS.subscribe(SERVER);
      Rycore.EVENT_BUS.subscribe(PLAYER);
      Rycore.EVENT_BUS.subscribe(COMBAT);
      Rycore.EVENT_BUS.subscribe(ASYNC);
   }
}
