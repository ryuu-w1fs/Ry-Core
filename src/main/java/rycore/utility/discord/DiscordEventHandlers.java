package rycore.utility.discord;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import rycore.utility.discord.callbacks.DisconnectedCallback;
import rycore.utility.discord.callbacks.ErroredCallback;
import rycore.utility.discord.callbacks.JoinGameCallback;
import rycore.utility.discord.callbacks.JoinRequestCallback;
import rycore.utility.discord.callbacks.ReadyCallback;
import rycore.utility.discord.callbacks.SpectateGameCallback;

public class DiscordEventHandlers extends Structure {
   public DisconnectedCallback disconnected;
   public JoinRequestCallback joinRequest;
   public SpectateGameCallback spectateGame;
   public ReadyCallback ready;
   public ErroredCallback errored;
   public JoinGameCallback joinGame;

   protected List<String> getFieldOrder() {
      return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
   }
}
