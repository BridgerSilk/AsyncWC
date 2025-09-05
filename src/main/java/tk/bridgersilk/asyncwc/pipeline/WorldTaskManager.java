package tk.bridgersilk.asyncwc.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import tk.bridgersilk.asyncwc.GenerationMode;
import tk.bridgersilk.asyncwc.Plugin;

public class WorldTaskManager {
	private final Plugin plugin;
	private final Map<String, PregenerationJob> jobs = new HashMap<>();

	public WorldTaskManager(Plugin plugin) { this.plugin = plugin; }

	public void beginPregeneration(World world, GenerationMode mode) {
		int radius = plugin.cfg().getInt("pregen.radius", 8);
		int cps = plugin.cfg().getInt("pregen.chunks-per-tick", 10);
		boolean border = plugin.cfg().getBoolean("pregen.set-worldborder", false);

		PregenerationJob job = new PregenerationJob(world.getName(), radius, cps, mode);
		jobs.put(world.getName(), job);

		if (border) {
			WorldBorder wb = world.getWorldBorder();
			wb.setCenter(world.getSpawnLocation());
			wb.setSize((radius * 16 * 2) + 32); // approx
		}

		CompletableFuture.supplyAsync(() -> buildSpiral(world.getSpawnLocation().getChunk(), radius))
				.thenAccept(list -> Bukkit.getScheduler().runTask(plugin, () -> consumeChunks(world, list, job)));
	}

	private List<ChunkPos> buildSpiral(Chunk spawn, int radius) {
		List<ChunkPos> list = new ArrayList<>();
		int sx = spawn.getX(); int sz = spawn.getZ();
		for (int r = 0; r <= radius; r++) {
			for (int x = -r; x <= r; x++) {
				list.add(new ChunkPos(sx + x, sz + r));
				list.add(new ChunkPos(sx + x, sz - r));
			}
			for (int z = -r + 1; z <= r - 1; z++) {
				list.add(new ChunkPos(sx + r, sz + z));
				list.add(new ChunkPos(sx - r, sz + z));
			}
		}
		return list;
	}

	private void consumeChunks(World world, List<ChunkPos> chunks, PregenerationJob job) {
		Bukkit.getScheduler().runTaskTimer(plugin, task -> {
			if (!world.isChunkGenerated(world.getSpawnLocation().getBlockX() >> 4, world.getSpawnLocation().getBlockZ() >> 4)) {
				world.getChunkAt(world.getSpawnLocation());
			}

			int perTick = job.chunksPerTick;
			int processed = 0;
			while (processed < perTick && job.index < chunks.size()) {
				ChunkPos pos = chunks.get(job.index++);
				try {
					world.getChunkAt(pos.x, pos.z);
				} catch (Throwable t) {
					// fallback for non-paper
					world.getChunkAt(pos.x, pos.z).load(true);
				}
				processed++;
			}

			if (job.index >= chunks.size()) {
				job.done = true;
				broadcast(world.getName(), ChatColor.GREEN + "Pregeneration finished for " + world.getName());
				task.cancel();
			}
		}, 1L, 1L);
	}

	private void broadcast(String worldName, String message) {
		Bukkit.getConsoleSender().sendMessage("[AsyncWC] " + message);
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.hasPermission("asyncwc.use")) p.sendMessage(ChatColor.AQUA + "[AsyncWC] " + message);
		}
	}

	private static class PregenerationJob {
		final String worldName; final int radius; final int chunksPerTick; final GenerationMode mode;
		int index = 0; boolean done = false;
		PregenerationJob(String worldName, int radius, int chunksPerTick, GenerationMode mode) {
			this.worldName = worldName; this.radius = radius; this.chunksPerTick = chunksPerTick; this.mode = mode;
		}
	}

	private record ChunkPos(int x, int z) {}
}