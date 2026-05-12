package ru.deelter.rideonhead.metrics;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import ru.deelter.rideonhead.RideOnHead;

public class MetricsHandler {

	private static final int PLUGIN_ID = 31273;
	private final RideOnHead plugin;

	public MetricsHandler(RideOnHead plugin) {
		this.plugin = plugin;
	}

	public void setupCharts() {
		Metrics metrics = new Metrics(plugin, PLUGIN_ID);

		metrics.addCustomChart(new SimplePie("storage_type", () -> plugin.getRideConfig().getStorageType().toUpperCase()));
		metrics.addCustomChart(new SimplePie("toggle_default", () -> plugin.getRideConfig().isToggleDefault() ? "enabled" : "disabled"));
		metrics.addCustomChart(new SimplePie("stack_climb", () -> plugin.getRideConfig().isStackClimb() ? "enabled" : "disabled"));
		metrics.addCustomChart(new SimplePie("auto_detect_language", () -> plugin.getRideConfig().isAutoDetectLanguage() ? "enabled" : "disabled"));
		metrics.addCustomChart(new SimplePie("default_language", () -> plugin.getRideConfig().getDefaultLanguage()));

		metrics.addCustomChart(new SimplePie("mount_sound", () -> {
			String sound = plugin.getRideConfig().getMountSound();
			return sound == null || sound.isEmpty() || sound.equalsIgnoreCase("none") ? "disabled" : "custom";
		}));

		metrics.addCustomChart(new SimplePie("dismount_sound", () -> {
			String sound = plugin.getRideConfig().getDismountSound();
			return sound == null || sound.isEmpty() || sound.equalsIgnoreCase("none") ? "disabled" : "custom";
		}));
	}
}