package ru.deelter.rideonhead.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.deelter.rideonhead.RideOnHead;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Lang {

	private final RideOnHead plugin;
	private final Map<String, Map<String, String>> languageMessages = new HashMap<>();
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private String defaultLanguage;
	private boolean autoDetect;

	public Lang(RideOnHead plugin) {
		this.plugin = plugin;
		reload();
	}

	public void reload() {
		languageMessages.clear();
		defaultLanguage = plugin.getRideConfig().getDefaultLanguage();
		autoDetect = plugin.getRideConfig().isAutoDetectLanguage();

		File langFolder = new File(plugin.getDataFolder(), "lang");
		if (!langFolder.exists()) {
			langFolder.mkdirs();
		}

		// Copy all built-in languages
		String[] resourceFiles = {
				"lang/en.yml", "lang/ru.yml", "lang/uk.yml", "lang/de.yml", "lang/es.yml",
				"lang/fr.yml", "lang/zh.yml"
		};
		for (String resourcePath : resourceFiles) {
			File target = new File(plugin.getDataFolder(), resourcePath);
			if (!target.exists()) {
				plugin.saveResource(resourcePath, false);
			}
		}
		File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
		if (files != null) {
			for (File file : files) {
				String langCode = file.getName().replace(".yml", "");
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				Map<String, String> messages = new HashMap<>();
				if (cfg.isConfigurationSection("messages")) {
					for (String key : cfg.getConfigurationSection("messages").getKeys(false)) {
						messages.put(key, cfg.getString("messages." + key));
					}
				}
				languageMessages.put(langCode, messages);
			}
		}
	}

	@Nullable
	public Component getMessage(String key, @NotNull CommandSender sender) {
		return getMessage(key, sender, TagResolver.empty());
	}

	@Nullable
	public Component getMessage(String key, CommandSender sender, TagResolver... resolvers) {
		Player player = sender instanceof Player ? (Player) sender : null;
		String raw = resolveRawMessage(key, player);
		if (raw == null || raw.isEmpty()) {
			return null;
		}
		TagResolver combined = TagResolver.resolver(resolvers);
		return miniMessage.deserialize(raw, combined);
	}

	@Nullable
	public Component getMessage(String key, CommandSender sender,
	                            String placeholderKey, String value) {
		return getMessage(key, sender, Placeholder.unparsed(placeholderKey, value));
	}

	private String resolveRawMessage(String key, Player player) {
		String lang = resolvePlayerLanguage(player);
		Map<String, String> messages = languageMessages.get(lang);
		if (messages != null && messages.containsKey(key)) {
			return messages.get(key);
		}
		Map<String, String> defaultMessages = languageMessages.get(defaultLanguage);
		if (defaultMessages != null && defaultMessages.containsKey(key)) {
			return defaultMessages.get(key);
		}
		return key;
	}

	private String resolvePlayerLanguage(Player player) {
		if (!autoDetect || player == null) {
			return defaultLanguage;
		}
		Locale locale = player.locale();
		String shortLang = locale.toString().split("_")[0].toLowerCase();
		if (languageMessages.containsKey(shortLang)) {
			return shortLang;
		}
		return defaultLanguage;
	}
}