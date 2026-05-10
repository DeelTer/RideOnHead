package ru.deelter.rideonhead.storage;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.deelter.rideonhead.RideOnHead;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NBTDataStorage implements DataStorage {

	private final RideOnHead plugin;
	private final NamespacedKey toggleKey;
	private final NamespacedKey blacklistKey;

	public NBTDataStorage(RideOnHead plugin) {
		this.plugin = plugin;
		this.toggleKey = new NamespacedKey(plugin, "ride_toggle");
		this.blacklistKey = new NamespacedKey(plugin, "ride_blacklist");
	}

	private PersistentDataContainer getContainer(UUID playerUUID) {
		Player player = Bukkit.getPlayer(playerUUID);
		return player != null ? player.getPersistentDataContainer() : null;
	}

	@Override
	public boolean getToggle(UUID playerUUID) {
		PersistentDataContainer pdc = getContainer(playerUUID);
		if (pdc != null) {
			return pdc.getOrDefault(toggleKey, PersistentDataType.BOOLEAN, plugin.getRideConfig().isToggleDefault());
		}
		return plugin.getRideConfig().isToggleDefault();
	}

	@Override
	public void setToggle(UUID playerUUID, boolean enabled) {
		PersistentDataContainer pdc = getContainer(playerUUID);
		if (pdc != null) {
			pdc.set(toggleKey, PersistentDataType.BOOLEAN, enabled);
		}
	}

	@Override
	public List<UUID> getBlacklist(UUID playerUUID) {
		PersistentDataContainer pdc = getContainer(playerUUID);
		if (pdc == null) return new ArrayList<>();
		String raw = pdc.get(blacklistKey, PersistentDataType.STRING);
		if (raw == null || raw.isEmpty()) return new ArrayList<>();
		return Arrays.stream(raw.split(","))
				.map(UUID::fromString)
				.collect(Collectors.toList());
	}

	@Override
	public void addToBlacklist(UUID targetUUID, UUID bannedUUID) {
		List<UUID> list = new ArrayList<>(getBlacklist(targetUUID));
		if (!list.contains(bannedUUID)) {
			list.add(bannedUUID);
		}
		saveList(targetUUID, list);
	}

	@Override
	public void removeFromBlacklist(UUID targetUUID, UUID bannedUUID) {
		List<UUID> list = new ArrayList<>(getBlacklist(targetUUID));
		list.remove(bannedUUID);
		saveList(targetUUID, list);
	}

	@Override
	public boolean isBlacklisted(UUID targetUUID, UUID passengerUUID) {
		return getBlacklist(targetUUID).contains(passengerUUID);
	}

	private void saveList(UUID playerUUID, List<UUID> list) {
		PersistentDataContainer pdc = getContainer(playerUUID);
		if (pdc != null) {
			String joined = list.stream().map(UUID::toString).collect(Collectors.joining(","));
			pdc.set(blacklistKey, PersistentDataType.STRING, joined);
		}
	}

	@Override
	public void shutdown() {
	}
}