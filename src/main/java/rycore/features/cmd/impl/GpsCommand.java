package rycore.features.cmd.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import rycore.Rycore;
import rycore.features.cmd.Command;

public class GpsCommand extends Command {
   public GpsCommand() {
      super("gps", "Set or clear a GPS target.");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(literal("off").executes(context -> {
         Rycore.gps_position = null;
         return 1;
      }));
      builder.then(arg("x", IntegerArgumentType.integer()).then(arg("z", IntegerArgumentType.integer()).executes(context -> {
         int x = (Integer)context.getArgument("x", Integer.class);
         int z = (Integer)context.getArgument("z", Integer.class);
         Rycore.gps_position = new BlockPos(x, 0, z);
         sendMessage("GPS set to X: " + Rycore.gps_position.getX() + " Z: " + Rycore.gps_position.getZ());
         return 1;
      })));
      builder.executes(context -> {
         sendMessage("Try .gps off / .gps x z");
         return 1;
      });
   }
}
