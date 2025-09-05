package tk.bridgersilk.asyncwc;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * asyncwc java plugin
 */
public class Plugin extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("asyncwc");

  public void onEnable()
  {
    LOGGER.info("asyncwc enabled");
  }

  public void onDisable()
  {
    LOGGER.info("asyncwc disabled");
  }
}
