package ru.deelter.rideonhead;

import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import ru.deelter.rideonhead.command.RideCommand;
import ru.deelter.rideonhead.command.RideTabCompleter;
import ru.deelter.rideonhead.config.Config;
import ru.deelter.rideonhead.config.Lang;
import ru.deelter.rideonhead.listener.RideListener;
import ru.deelter.rideonhead.storage.DataStorage;
import ru.deelter.rideonhead.storage.H2DataStorage;
import ru.deelter.rideonhead.storage.NBTDataStorage;
import ru.deelter.rideonhead.storage.YmlDataStorage;

@Getter
public final class RideOnHead extends JavaPlugin {

	private Config rideConfig;
	private Lang lang;
	private DataStorage storage;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		rideConfig = new Config(this);
		lang = new Lang(this);
		storage = initStorage();

		RideCommand rideCommand = new RideCommand(this);
		PluginCommand pluginCommand = getCommand("ride");
		if (pluginCommand != null) {
			pluginCommand.setExecutor(rideCommand);
			pluginCommand.setTabCompleter(new RideTabCompleter());
		}

		getServer().getPluginManager().registerEvents(new RideListener(this), this);
		getLogger().info("RideOnHead enabled. Storage: " + rideConfig.getStorageType());
	}

	private @NonNull DataStorage initStorage() {
		return switch (rideConfig.getStorageType().toUpperCase()) {
			case "H2" -> new H2DataStorage(this);
			case "YML" -> new YmlDataStorage(this);
			default -> new NBTDataStorage(this);
		};
	}

	/**
	 * Reloads the configuration and language files without affecting storage.
	 */
	public void reloadPlugin() {
		reloadConfig();
		rideConfig = new Config(this);
		lang.reload();
		getLogger().info("Configuration and language files reloaded.");
	}

	@Override
	public void onDisable() {
		if (storage != null) storage.shutdown();
	}
}