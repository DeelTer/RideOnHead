package ru.deelter.rideonhead.listener;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.NonNull;
import ru.deelter.rideonhead.RideOnHead;

import java.util.List;

public class RideListener implements Listener {

	private final RideOnHead plugin;

	public RideListener(RideOnHead plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteractEntity(@NonNull PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		if (!(event.getRightClicked() instanceof Player target)) return;

		Player rider = event.getPlayer();
		if (rider.isSneaking()) return;
		if (!rider.getPassengers().isEmpty() || rider.getVehicle() != null) return;

		if (!rider.hasPermission("rideonhead.user")) {
			sendMessage(rider, "no-permission");
			return;
		}

		if (!plugin.getStorage().getToggle(target.getUniqueId())) {
			sendMessage(rider, "target-toggle-disabled");
			return;
		}
		if (plugin.getStorage().isBlacklisted(target.getUniqueId(), rider.getUniqueId())) {
			sendMessage(rider, "target-blacklisted");
			return;
		}

		Player mountTarget;
		if (plugin.getRideConfig().isStackClimb()) {
			mountTarget = findTopPassenger(target);
			if (mountTarget == rider) {
				sendMessage(rider, "already-in-stack");
				return;
			}
		} else {
			if (!target.getPassengers().isEmpty()) {
				sendMessage(rider, "target-has-passengers");
				return;
			}
			mountTarget = target;
		}

		mountTarget.addPassenger(rider);
		playSound(rider, plugin.getRideConfig().getMountSound());
	}

	@EventHandler
	public void onSneak(@NonNull PlayerToggleSneakEvent event) {
		if (!event.isSneaking()) return;

		Player carrier = event.getPlayer();
		List<Entity> passengers = carrier.getPassengers();
		if (passengers.isEmpty()) return;

		boolean removedAny = false;
		for (Entity passenger : passengers) {
			if (passenger instanceof Player) {
				carrier.removePassenger(passenger);
				removedAny = true;
			}
		}
		if (removedAny) {
			sendMessage(carrier, "rider-ejected");
			playSound(carrier, plugin.getRideConfig().getDismountSound());
		}
	}

	private Player findTopPassenger(Entity start) {
		Entity current = start;
		while (true) {
			List<Entity> passengers = current.getPassengers();
			if (passengers.isEmpty()) break;
			Entity firstPassenger = passengers.getFirst();
			if (firstPassenger instanceof Player) {
				current = firstPassenger;
			} else {
				break;
			}
		}
		return (Player) current;
	}

	private void playSound(Player player, String soundName) {
		if (soundName == null || soundName.isBlank() || soundName.equalsIgnoreCase("none")) return;
		Sound sound = Sound.sound(Key.key(soundName), Sound.Source.PLAYER, 1f, 1f);
		player.playSound(sound);
	}

	private void sendMessage(Player player, String key) {
		Component msg = plugin.getLang().getMessage(key, player);
		if (msg != null) {
			player.sendMessage(msg);
		}
	}
}