package ru.deelter.rideonhead.storage;

import java.util.List;
import java.util.UUID;

public interface DataStorage {
	boolean getToggle(UUID playerUUID);

	void setToggle(UUID playerUUID, boolean enabled);

	List<UUID> getBlacklist(UUID playerUUID);

	void addToBlacklist(UUID targetUUID, UUID bannedUUID);

	void removeFromBlacklist(UUID targetUUID, UUID bannedUUID);

	boolean isBlacklisted(UUID targetUUID, UUID passengerUUID);

	void shutdown();
}