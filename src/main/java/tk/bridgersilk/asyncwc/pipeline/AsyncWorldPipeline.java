package tk.bridgersilk.asyncwc.pipeline;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import tk.bridgersilk.asyncwc.GenerationMode;
import tk.bridgersilk.asyncwc.Plugin;
import tk.bridgersilk.asyncwc.WorldKind;
import tk.bridgersilk.asyncwc.util.DirUtil;

public class AsyncWorldPipeline {

	public static void createWorld(Plugin plugin,
	                              String worldName,
	                              WorldKind kind,
	                              GenerationMode mode,
	                              Consumer<String> progress) {

		progress.accept("Preparing files...");
		CompletableFuture.runAsync(() -> {
			File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);
			if (worldFolder.exists()) {
				DirUtil.deleteSilently(worldFolder);
			}

			if (mode != GenerationMode.MINIMAL) {
				String templatePath = plugin.cfg().getString("templates." + kind.name().toLowerCase());
				if (templatePath != null && !templatePath.isBlank()) {
					DirUtil.copyDirectory(new File(templatePath), worldFolder);
				}
			}
		}).whenComplete((v, ex) -> {
			if (ex != null) {
				progress.accept(ChatColor.RED + "File preparation failed: " + ex.getMessage());
				return;
			}

			Bukkit.getScheduler().runTask(plugin, () -> {
				progress.accept("Creating world (sync registration)...");
				WorldCreator creator = new WorldCreator(worldName);
				switch (kind) {
					case FLAT -> {
						creator.type(WorldType.FLAT);
                        creator.generatorSettings("{\"layers\": [{\"block\": \"bedrock\", \"height\": 1}, {\"block\": \"dirt\", \"height\": 2}, {\"block\": \"grass_block\", \"height\": 1}], \"biome\":\"plains\"}");
					}
					case VOID -> {
                        creator.type(WorldType.FLAT);
                        creator.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}");
                    }
					default -> creator.type(WorldType.NORMAL);
				}
				World world = Bukkit.createWorld(creator);
				if (world == null) {
					progress.accept(ChatColor.RED + "World creation failed.");
					return;
				}
				world.setAutoSave(true);
				world.setKeepSpawnInMemory(true);

				progress.accept("Scheduling pregeneration...");
				plugin.tasks().beginPregeneration(world, mode);
				progress.accept(ChatColor.GREEN + "World registered. Pregeneration running in background.");
			});
		});
	}
}