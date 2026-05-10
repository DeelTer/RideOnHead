package ru.deelter.rideonhead.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NonNull;
import ru.deelter.rideonhead.RideOnHead;

@Getter
public class Config {
	private final String storageType;
	private final boolean toggleDefault;
	private final String h2File;
	private final String ymlFile;
	private final boolean stackClimb;
	private final String defaultLanguage;
	private final boolean autoDetectLanguage;
	private final String mountSound;
	private final String dismountSound;

	public Config(@NonNull RideOnHead plugin) {
		FileConfiguration config = plugin.getConfig();
		this.storageType = config.getString("storage.type", "NBT");
		this.toggleDefault = config.getBoolean("toggle-default", true);
		this.h2File = config.getString("storage.h2.file", "plugins/RideOnHead/data.db");
		this.ymlFile = config.getString("storage.yml.file", "plugins/RideOnHead/data.yml");
		this.stackClimb = config.getBoolean("stack-climb", false);
		this.defaultLanguage = config.getString("language.default", "en");
		this.autoDetectLanguage = config.getBoolean("language.auto-detect", false);
		this.mountSound = config.getString("sounds.mount", "");
		this.dismountSound = config.getString("sounds.dismount", "");
	}
}