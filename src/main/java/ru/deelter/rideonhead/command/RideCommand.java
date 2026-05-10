package ru.deelter.rideonhead.command;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ru.deelter.rideonhead.RideOnHead;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class RideCommand implements CommandExecutor {

	private final RideOnHead plugin;

	@Override
	public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command,
	                         @NonNull String label, String[] args) {
		if (!hasAnyPermission(sender, "rideonhead.user", "rideonhead.admin")) {
			sendMessage(sender, "no-permission");
			return true;
		}
		if (args.length == 0) {
			sendHelp(sender);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "reload" -> handleReload(sender);
			case "toggle" -> handleToggle(sender, args);
			case "blacklist" -> handleBlacklist(sender, args);
			default -> sendHelp(sender);
		}
		return true;
	}

	private void handleReload(@NonNull CommandSender sender) {
		if (!sender.hasPermission("rideonhead.admin")) {
			sendMessage(sender, "admin-no-permission");
			return;
		}
		plugin.reloadPlugin();
		sendMessage(sender, "reload-success");
	}

	private void handleToggle(CommandSender sender, String @NonNull [] args) {
		// Admin toggle on another player
		if (args.length >= 2 && sender.hasPermission("rideonhead.admin")) {
			String targetName = args[1];
			Player target = Bukkit.getPlayerExact(targetName);
			if (target == null) {
				sendMessage(sender, "player-not-found",
						Placeholder.unparsed("player", targetName));
				return;
			}
			UUID targetUUID = target.getUniqueId();
			boolean newState = !plugin.getStorage().getToggle(targetUUID);
			plugin.getStorage().setToggle(targetUUID, newState);

			String adminKey = newState ? "toggle-admin-enabled" : "toggle-admin-disabled";
			sendMessage(sender, adminKey, Placeholder.unparsed("player", target.getName()));

			Player onlineTarget = target.getPlayer();
			if (onlineTarget != null) {
				String notifyKey = newState ? "toggle-target-notify-enabled" : "toggle-target-notify-disabled";
				sendMessage(onlineTarget, notifyKey);
			}
			return;
		}

		// Self toggle
		if (!(sender instanceof Player player)) {
			sendMessage(sender, "player-only-command");
			return;
		}
		UUID uuid = player.getUniqueId();
		boolean newState = !plugin.getStorage().getToggle(uuid);
		plugin.getStorage().setToggle(uuid, newState);
		sendMessage(player, newState ? "toggle-enabled" : "toggle-disabled");
	}

	private void handleBlacklist(CommandSender sender, String @NonNull [] args) {
		// Admin blacklist management
		if (args.length >= 3 && sender.hasPermission("rideonhead.admin")) {
			String ownerName = args[1];
			String targetName = args[2];

			OfflinePlayer owner = findPlayer(ownerName);
			if (owner == null || !owner.hasPlayedBefore()) {
				sendMessage(sender, "player-not-found",
						Placeholder.unparsed("player", ownerName));
				return;
			}
			OfflinePlayer target = findPlayer(targetName);
			if (target == null || !target.hasPlayedBefore()) {
				sendMessage(sender, "player-not-found",
						Placeholder.unparsed("player", targetName));
				return;
			}

			UUID ownerUUID = owner.getUniqueId();
			UUID targetUUID = target.getUniqueId();
			List<UUID> blacklist = plugin.getStorage().getBlacklist(ownerUUID);
			String ownerDisplay = owner.getName() != null ? owner.getName() : ownerName;
			String targetDisplay = target.getName() != null ? target.getName() : targetName;

			if (blacklist.contains(targetUUID)) {
				plugin.getStorage().removeFromBlacklist(ownerUUID, targetUUID);
				sendMessage(sender, "blacklist-admin-remove",
						Placeholder.unparsed("target", targetDisplay),
						Placeholder.unparsed("owner", ownerDisplay));
				notifyPlayerIfOnline(owner, "blacklist-admin-notify-remove", "player", targetDisplay);
			} else {
				plugin.getStorage().addToBlacklist(ownerUUID, targetUUID);
				sendMessage(sender, "blacklist-admin-add",
						Placeholder.unparsed("target", targetDisplay),
						Placeholder.unparsed("owner", ownerDisplay));
				notifyPlayerIfOnline(owner, "blacklist-admin-notify-add", "player", targetDisplay);
			}
			return;
		}

		// Own blacklist
		if (!(sender instanceof Player player)) {
			sendMessage(sender, "player-only-command");
			return;
		}

		if (args.length < 2) {
			listBlacklist(player);
			return;
		}

		String targetName = args[1];
		OfflinePlayer target = findPlayer(targetName);
		if (target == null || !target.hasPlayedBefore()) {
			sendMessage(player, "player-not-found",
					Placeholder.unparsed("player", targetName));
			return;
		}

		UUID targetUUID = target.getUniqueId();
		List<UUID> blacklist = plugin.getStorage().getBlacklist(player.getUniqueId());
		String targetDisplay = target.getName() != null ? target.getName() : targetName;

		if (blacklist.contains(targetUUID)) {
			plugin.getStorage().removeFromBlacklist(player.getUniqueId(), targetUUID);
			sendMessage(player, "blacklist-remove", Placeholder.unparsed("player", targetDisplay));
		} else {
			plugin.getStorage().addToBlacklist(player.getUniqueId(), targetUUID);
			sendMessage(player, "blacklist-add", Placeholder.unparsed("player", targetDisplay));
		}
	}

	private void listBlacklist(@NonNull Player player) {
		List<UUID> blacklist = plugin.getStorage().getBlacklist(player.getUniqueId());
		sendMessage(player, "blacklist-list-header");
		if (blacklist.isEmpty()) {
			sendMessage(player, "blacklist-list-empty");
		} else {
			for (UUID uuid : blacklist) {
				String name = Bukkit.getOfflinePlayer(uuid).getName();
				if (name == null) name = uuid.toString();
				sendMessage(player, "blacklist-list-entry",
						Placeholder.unparsed("player", name));
			}
		}
	}

	private void notifyPlayerIfOnline(@NonNull OfflinePlayer offlinePlayer, String key,
	                                  String placeholderKey, String value) {
		if (offlinePlayer.isOnline()) {
			Player player = offlinePlayer.getPlayer();
			if (player != null) {
				sendMessage(player, key, Placeholder.unparsed(placeholderKey, value));
			}
		}
	}

	/**
	 * Sends a message to the target if the message is not null/empty.
	 */
	private void sendMessage(CommandSender target, String key, TagResolver... resolvers) {
		Component msg = plugin.getLang().getMessage(key, target, resolvers);
		if (msg != null) {
			target.sendMessage(msg);
		}
	}

	@Nullable
	private OfflinePlayer findPlayer(String name) {
		Player online = Bukkit.getPlayerExact(name);
		if (online != null) return online;
		return Bukkit.getOfflinePlayerIfCached(name);
	}

	private boolean hasAnyPermission(CommandSender sender, String @NonNull ... perms) {
		for (String perm : perms) {
			if (sender.hasPermission(perm)) return true;
		}
		return false;
	}

	private void sendHelp(CommandSender sender) {
		sendMessage(sender, "help-message");
	}
}