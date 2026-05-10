package ru.deelter.rideonhead.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ru.deelter.rideonhead.RideOnHead;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class H2DataStorage implements DataStorage {

	private final RideOnHead plugin;
	private final Connection connection;

	private final Cache<UUID, Boolean> toggleCache = Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES).build();
	private final Cache<UUID, List<UUID>> blacklistCache = Caffeine.newBuilder()
			.expireAfterWrite(10, TimeUnit.MINUTES).build();

	public H2DataStorage(RideOnHead plugin) {
		this.plugin = plugin;
		try {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection("jdbc:h2:file:" + plugin.getRideConfig().getH2File());
			initTables();
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize H2 database", e);
		}
	}

	private void initTables() throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("CREATE TABLE IF NOT EXISTS player_settings (uuid VARCHAR(36) PRIMARY KEY, toggle BOOLEAN NOT NULL)");
			stmt.execute("CREATE TABLE IF NOT EXISTS player_blacklist (target VARCHAR(36) NOT NULL, banned VARCHAR(36) NOT NULL, PRIMARY KEY (target, banned))");
		}
	}

	@Override
	public boolean getToggle(UUID uuid) {
		return toggleCache.get(uuid, id -> {
			try (PreparedStatement ps = connection.prepareStatement(
					"SELECT toggle FROM player_settings WHERE uuid = ?")) {
				ps.setString(1, id.toString());
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					return rs.getBoolean("toggle");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return plugin.getRideConfig().isToggleDefault();
		});
	}

	@Override
	public void setToggle(UUID uuid, boolean enabled) {
		try (PreparedStatement ps = connection.prepareStatement(
				"MERGE INTO player_settings KEY(uuid) VALUES (?, ?)")) {
			ps.setString(1, uuid.toString());
			ps.setBoolean(2, enabled);
			ps.executeUpdate();
			toggleCache.put(uuid, enabled);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<UUID> getBlacklist(UUID uuid) {
		return blacklistCache.get(uuid, id -> {
			List<UUID> list = new ArrayList<>();
			try (PreparedStatement ps = connection.prepareStatement(
					"SELECT banned FROM player_blacklist WHERE target = ?")) {
				ps.setString(1, id.toString());
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					list.add(UUID.fromString(rs.getString("banned")));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return list;
		});
	}

	@Override
	public void addToBlacklist(UUID target, UUID banned) {
		try (PreparedStatement ps = connection.prepareStatement(
				"MERGE INTO player_blacklist KEY(target, banned) VALUES (?, ?)")) {
			ps.setString(1, target.toString());
			ps.setString(2, banned.toString());
			ps.executeUpdate();
			blacklistCache.invalidate(target);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeFromBlacklist(UUID target, UUID banned) {
		try (PreparedStatement ps = connection.prepareStatement(
				"DELETE FROM player_blacklist WHERE target = ? AND banned = ?")) {
			ps.setString(1, target.toString());
			ps.setString(2, banned.toString());
			ps.executeUpdate();
			blacklistCache.invalidate(target);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isBlacklisted(UUID target, UUID passenger) {
		return getBlacklist(target).contains(passenger);
	}

	@Override
	public void shutdown() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}