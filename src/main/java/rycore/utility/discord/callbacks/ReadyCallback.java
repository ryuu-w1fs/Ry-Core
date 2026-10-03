package rycore.utility.discord.callbacks;

import com.sun.jna.Callback;
import rycore.utility.discord.DiscordUser;

public interface ReadyCallback extends Callback {
   void apply(DiscordUser var1);
}
