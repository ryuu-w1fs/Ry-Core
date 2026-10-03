package rycore.features.modules.misc;

import java.util.regex.Pattern;
import rycore.core.manager.client.ModuleManager;
import rycore.features.modules.Module;
import rycore.setting.Setting;

/**
 * An thong tin nhan dang khi dang stream.
 *
 * <p>Khac NameProtect (chi thay ten nguoi choi): module nay loc them cac chuoi lam
 * lo server dang choi - so hieu anarchy, dia chi IP, ma moi.
 */
public class StreamerMode extends Module {
   /** "Anarchy-123" / "Анархия-123" -> giu nguyen tien to, an so. */
   private static final Pattern ANARCHY = Pattern.compile("(?iu)(anarchy|анархия)([\\s\\-_]*)(\\d+)");

   /** Dia chi IPv4 kem cong (tuy chon), vi du 123.45.67.89:25565 */
   private static final Pattern IP = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d{1,5})?\\b");

   /** Ten mien server, vi du play.example.net */
   private static final Pattern DOMAIN = Pattern.compile("(?i)\\b[a-z0-9][a-z0-9-]*(\\.[a-z0-9][a-z0-9-]*)+(:\\d{1,5})?\\b");

   public static Setting<Boolean> hideAnarchy = new Setting<>("Hide anarchy number", true);
   public static Setting<Boolean> hideIp = new Setting<>("Hide server IP", true);
   public static Setting<Boolean> hideDomain = new Setting<>("Hide server domain", false);
   public static Setting<String> mask = new Setting<>("Mask", "???");

   public StreamerMode() {
      super("StreamerMode", "Hides server info while streaming.", Module.Category.MISC);
   }

   /** Loc mot doan text truoc khi hien thi. Tra ve nguyen ban neu module tat. */
   public static String filter(String text) {
      if (text == null || text.isEmpty() || !ModuleManager.streamerMode.isEnabled()) {
         return text;
      }

      String masked = mask.getValue();
      if (masked == null || masked.isEmpty()) {
         masked = "???";
      }

      String result = text;
      if (hideAnarchy.getValue()) {
         result = ANARCHY.matcher(result).replaceAll("$1$2" + java.util.regex.Matcher.quoteReplacement(masked));
      }

      if (hideIp.getValue()) {
         result = IP.matcher(result).replaceAll(java.util.regex.Matcher.quoteReplacement(masked));
      }

      if (hideDomain.getValue()) {
         // Chay sau IP de khong an mat dia chi so truoc khi IP kip khop.
         result = DOMAIN.matcher(result).replaceAll(java.util.regex.Matcher.quoteReplacement(masked));
      }

      return result;
   }
}
