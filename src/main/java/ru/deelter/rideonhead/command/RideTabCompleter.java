package ru.deelter.rideonhead.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RideTabCompleter implements TabCompleter {

	private static final List<String> FIRST_ARGS = Arrays.asList("toggle", "blacklist", "reload");

	@Override
	public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String @NonNull [] args) {
		if (args.length == 1) {
			return partialMatch(args[0], FIRST_ARGS);
		}

		boolean isAdmin = sender.hasPermission("rideonhead.admin");

		if (args[0].equalsIgnoreCase("toggle")) {
			if (args.length == 2 && isAdmin) {
				List<String> playerNames = new ArrayList<>();
				for (var player : sender.getServer().getOnlinePlayers()) {
					playerNames.add(player.getName());
				}
				return partialMatch(args[1], playerNames);
			}
		} else if (args[0].equalsIgnoreCase("blacklist")) {
			if (args.length == 2) {
				List<String> playerNames = new ArrayList<>();
				for (var player : sender.getServer().getOnlinePlayers()) {
					playerNames.add(player.getName());
				}
				return partialMatch(args[1], playerNames);
			} else if (args.length == 3 && isAdmin) {
				// Suggest target for admin blacklist owner
				List<String> playerNames = new ArrayList<>();
				for (var player : sender.getServer().getOnlinePlayers()) {
					playerNames.add(player.getName());
				}
				return partialMatch(args[2], playerNames);
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			return Collections.emptyList(); // no arguments
		}

		return Collections.emptyList();
	}

	private @NonNull List<String> partialMatch(@NonNull String token, @NonNull List<String> options) {
		List<String> result = new ArrayList<>();
		String lowerToken = token.toLowerCase();
		for (String option : options) {
			if (option.toLowerCase().startsWith(lowerToken)) {
				result.add(option);
			}
		}
		return result;
	}
}