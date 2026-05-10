package ru.deelter.rideonhead.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import ru.deelter.rideonhead.RideOnHead;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class YmlDataStorage implements DataStorage {

	private final File file;
	private YamlConfiguration yamlConfig;

	public YmlDataStorage(RideOnHead plugin) {
		this.file = new File(plugin.getRideConfig().getYmlFile());
		reload();
	}

	private void reload() {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		yamlConfig = YamlConfiguration.loadConfiguration(file);
	}

	private void save() {
		try {
			yamlConfig.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String path(UUID uuid, String key) {
		return "players." + uuid.toString() + "." + key;
	}

	@Override
	public boolean getToggle(UUID playerUUID) {
		return yamlConfig.getBoolean(path(playerUUID, "toggle"), true);
	}

	@Override
	public void setToggle(UUID playerUUID, boolean enabled) {
		yamlConfig.set(path(playerUUID, "toggle"), enabled);
		save();
	}

	@Override
	public List<UUID> getBlacklist(UUID playerUUID) {
		List<String> raw = yamlConfig.getStringList(path(playerUUID, "blacklist"));
		return raw.stream().map(UUID::fromString).collect(Collectors.toList());
	}

	@Override
	public void addToBlacklist(UUID targetUUID, UUID bannedUUID) {
		List<UUID> list = getBlacklist(targetUUID);
		if (!list.contains(bannedUUID)) {
			list.add(bannedUUID);
			yamlConfig.set(path(targetUUID, "blacklist"),
					list.stream().map(UUID::toString).collect(Collectors.toList()));
			save();
		}
	}

	@Override
	public void removeFromBlacklist(UUID targetUUID, UUID bannedUUID) {
		List<UUID> list = getBlacklist(targetUUID);
		list.remove(bannedUUID);
		yamlConfig.set(path(targetUUID, "blacklist"),
				list.stream().map(UUID::toString).collect(Collectors.toList()));
		save();
	}

	@Override
	public boolean isBlacklisted(UUID targetUUID, UUID passengerUUID) {
		return getBlacklist(targetUUID).contains(passengerUUID);
	}

	@Override
	public void shutdown() {
		save();
	}
}