package tk.bridgersilk.asyncwc.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import tk.bridgersilk.asyncwc.GenerationMode;
import tk.bridgersilk.asyncwc.Plugin;
import tk.bridgersilk.asyncwc.WorldKind;
import tk.bridgersilk.asyncwc.pipeline.AsyncWorldPipeline;

public class AsyncWorldCommand implements CommandExecutor, TabCompleter {
	private final Plugin plugin;

	public AsyncWorldCommand(Plugin plugin) { this.plugin = plugin; }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("asyncwc.use")) {
			sender.sendMessage(ChatColor.RED + "No permission.");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage(ChatColor.YELLOW + "/" + label + " create <flat|normal|void> <name> <minimal|decreased|all>");
			return true;
		}
		if (args[0].equalsIgnoreCase("create")) {
			if (args.length < 4) {
				sender.sendMessage(ChatColor.RED + "Usage: /" + label + " create <flat|normal|void> <name> <minimal|decreased|all>");
				return true;
			}
			WorldKind kind = WorldKind.from(args[1]);
			String worldName = args[2];
			GenerationMode mode = GenerationMode.from(args[3]);

			AsyncWorldPipeline.createWorld(plugin, worldName, kind, mode, progress -> {
				if (sender instanceof Player) {
					((Player) sender).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§b[AsyncWC]" + progress));
				} else {
					sender.sendMessage(ChatColor.GRAY + progress);
				}
			});
			sender.sendMessage(ChatColor.GREEN + "Queued async world creation for '" + worldName + "' (" + kind + ", " + mode + ").");
			return true;
		}
		sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> out = new ArrayList<>();
		if (args.length == 1) {
			out.add("create");
		} else if (args.length == 2) {
			out.add("normal"); out.add("flat"); out.add("void");
		} else if (args.length == 4) {
			out.add("minimal"); out.add("decreased"); out.add("all");
		}
		return out;
	}
}
