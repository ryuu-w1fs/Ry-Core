package rycore.core.manager.client;

import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import rycore.core.manager.IManager;
import rycore.features.cmd.Command;
import rycore.features.cmd.impl.BindCommand;
import rycore.features.cmd.impl.BlockESPCommand;
import rycore.features.cmd.impl.CfgCommand;
import rycore.features.cmd.impl.ChestStealerCommand;
import rycore.features.cmd.impl.DropAllCommand;
import rycore.features.cmd.impl.EClipCommand;
import rycore.features.cmd.impl.FriendCommand;
import rycore.features.cmd.impl.GarbageCleanerCommand;
import rycore.features.cmd.impl.GpsCommand;
import rycore.features.cmd.impl.HClipCommand;
import rycore.features.cmd.impl.HelpCommand;
import rycore.features.cmd.impl.HorseSpeedCommand;
import rycore.features.cmd.impl.InvCleanerCommand;
import rycore.features.cmd.impl.ItemHelperCommand;
import rycore.features.cmd.impl.LoginCommand;
import rycore.features.cmd.impl.MacroCommand;
import rycore.features.cmd.impl.ModuleCommand;
import rycore.features.cmd.impl.NukerCommand;
import rycore.features.cmd.impl.PrefixCommand;
import rycore.features.cmd.impl.StaffCommand;
import rycore.features.cmd.impl.TabParseCommand;
import rycore.features.cmd.impl.TreasureCommand;
import rycore.features.cmd.impl.VClipCommand;

public class CommandManager implements IManager {
   private String prefix = "@";
   private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher();
   private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance(), net.minecraft.command.permission.PermissionPredicate.ALL);
   private final List<Command> commands = new ArrayList<>();

   public CommandManager() {
      this.add(new GpsCommand());
      this.add(new CfgCommand());
      this.add(new BindCommand());
      this.add(new HelpCommand());
      this.add(new NukerCommand());
      this.add(new EClipCommand());
      this.add(new HClipCommand());
      this.add(new LoginCommand());
      this.add(new MacroCommand());
      this.add(new StaffCommand());
      this.add(new VClipCommand());
      this.add(new FriendCommand());
      this.add(new ModuleCommand());
      this.add(new PrefixCommand());
      this.add(new DropAllCommand());
      this.add(new TreasureCommand());
      this.add(new TabParseCommand());
      this.add(new BlockESPCommand());
      this.add(new HorseSpeedCommand());
      this.add(new InvCleanerCommand());
      this.add(new ChestStealerCommand());
      this.add(new GarbageCleanerCommand());
      this.add(new ItemHelperCommand());
   }

   private void add(@NotNull Command command) {
      command.register(this.dispatcher);
      this.commands.add(command);
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public Command get(Class<? extends Command> commandClass) {
      for (Command command : this.commands) {
         if (command.getClass().equals(commandClass)) {
            return command;
         }
      }

      return null;
   }

   @NotNull
   public static String getClientMessage() {
      return Formatting.WHITE + "⌊" + Formatting.GOLD + "⚡" + Formatting.WHITE + "⌉" + Formatting.RESET;
   }

   public List<Command> getCommands() {
      return this.commands;
   }

   public CommandSource getSource() {
      return this.source;
   }

   public CommandDispatcher<CommandSource> getDispatcher() {
      return this.dispatcher;
   }

   public void registerCommand(Command command) {
      if (command != null) {
         command.register(this.dispatcher);
         this.commands.add(command);
      }
   }
}
