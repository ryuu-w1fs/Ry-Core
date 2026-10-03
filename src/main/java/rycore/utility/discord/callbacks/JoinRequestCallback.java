package rycore.utility.discord.callbacks;

import com.sun.jna.Callback;
import rycore.utility.discord.DiscordUser;

public interface JoinRequestCallback extends Callback {
   void apply(DiscordUser var1);
}
