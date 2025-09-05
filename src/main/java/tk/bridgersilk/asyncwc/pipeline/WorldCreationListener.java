package tk.bridgersilk.asyncwc.pipeline;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import tk.bridgersilk.asyncwc.GenerationMode;
import tk.bridgersilk.asyncwc.Plugin;

public class WorldCreationListener implements Listener {
	private final Plugin plugin;
	public WorldCreationListener(Plugin plugin) { this.plugin = plugin; }

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		if (!plugin.cfg().getBoolean("auto-handle-external-creations", true)) return;
		World world = e.getWorld();
		GenerationMode defaultMode = GenerationMode.from(plugin.cfg().getString("default-mode", "minimal"));

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			plugin.tasks().beginPregeneration(world, defaultMode);
			Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "[AsyncWC] Auto-started pregeneration for " + world.getName() + " (" + defaultMode + ")");
		}, 1L);
	}
}