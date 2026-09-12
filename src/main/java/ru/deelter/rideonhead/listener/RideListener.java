package ru.deelter.rideonhead.listener;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;
import ru.deelter.rideonhead.RideOnHead;

import java.util.List;

public class RideListener implements Listener {

	private static final int HELMET_SLOT = 39;

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

		if (plugin.getRideConfig().isRequireSaddle() && !hasSaddleHelmet(target)) {
			sendMessage(rider, "target-no-saddle");
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
	public void onUseSaddleItem(@NonNull PlayerInteractEvent event) {
		if (!isSaddleEquipEnabled()) return;
		if (event.getHand() != EquipmentSlot.HAND) return;

		ItemStack saddle = event.getItem();
		if (saddle == null || saddle.getType() != Material.SADDLE) return;

		equipSaddle(event.getPlayer(), saddle);
	}

	@EventHandler
	public void onSaddleInventoryClick(@NonNull InventoryClickEvent event) {
		if (!isSaddleEquipEnabled()) return;
		if (!(event.getClickedInventory() instanceof PlayerInventory)) return;
		if (event.getSlot() != HELMET_SLOT) return;

		ItemStack cursor = event.getCursor();
		if (cursor.getType() != Material.SADDLE) return;
		if (!(event.getWhoClicked() instanceof Player player)) return;

		event.setCancelled(true);
		equipSaddle(player, cursor);
	}

	private boolean isSaddleEquipEnabled() {
		return plugin.getRideConfig().isRequireSaddle() && plugin.getRideConfig().isSaddleEquip();
	}

	private void equipSaddle(@NonNull Player player, @NonNull ItemStack saddle) {
		PlayerInventory inventory = player.getInventory();
		if (inventory.getHelmet() != null) return;

		ItemStack helmet = saddle.clone();
		helmet.setAmount(1);
		inventory.setHelmet(helmet);
		saddle.subtract();
		playSound(player, plugin.getRideConfig().getMountSound());
	}

	private boolean hasSaddleHelmet(@NonNull Player target) {
		ItemStack helmet = target.getInventory().getHelmet();
		return helmet != null && helmet.getType() == Material.SADDLE;
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