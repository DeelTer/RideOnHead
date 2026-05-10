package ru.deelter.rideonhead;

import ru.deelter.rideonhead.command.RideCommand;
import ru.deelter.rideonhead.config.Config;
import ru.deelter.rideonhead.config.Lang;
import ru.deelter.rideonhead.listener.RideListener;
import ru.deelter.rideonhead.storage.*;

import org.bukkit.plugin.java.JavaPlugin;

public final class RideOnHeadPlugin extends JavaPlugin {

	private Config config;
	private Lang lang;
	private DataStorage storage;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = new Config(this);
		lang = new Lang(this);
		storage = initStorage();

		RideCommand rideCommand = new RideCommand(this);
		getCommand("ride").setExecutor(rideCommand);
		getCommand("ride").setTabCompleter(new RideTabCompleter());

		getServer().getPluginManager().registerEvents(new RideListener(this), this);
		getLogger().info("RideOnHead enabled. Storage: " + config.getStorageType());
	}

	private DataStorage initStorage() {
		return switch (config.getStorageType().toUpperCase()) {
			case "H2" -> new H2DataStorage(this);
			case "YML" -> new YmlDataStorage(this);
			default -> new NBTDataStorage(this);
		};
	}

	@Override
	public void onDisable() {
		if (storage != null) storage.shutdown();
	}

	public Config getPluginConfig() { return config; }
	public Lang getLang() { return lang; }
	public DataStorage getStorage() { return storage; }
}