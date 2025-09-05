package tk.bridgersilk.asyncwc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import tk.bridgersilk.asyncwc.commands.AsyncWorldCommand;
import tk.bridgersilk.asyncwc.pipeline.WorldCreationListener;
import tk.bridgersilk.asyncwc.pipeline.WorldTaskManager;

public class Plugin extends JavaPlugin {
	private static Plugin instance;
	private WorldTaskManager taskManager;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		this.taskManager = new WorldTaskManager(this);

		getCommand("asyncworld").setExecutor(new AsyncWorldCommand(this));
		getCommand("asyncworld").setTabCompleter(new AsyncWorldCommand(this));
		getServer().getPluginManager().registerEvents(new WorldCreationListener(this), this);
		getLogger().info("AsyncWC enabled.");
	}

	public static Plugin get() { return instance; }
	public WorldTaskManager tasks() { return taskManager; }
	public FileConfiguration cfg() { return getConfig(); }
}