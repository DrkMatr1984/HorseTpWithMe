/*
	Copyright (c) 2015 TheMrJezza
	
	To any person obtaining a copy of this software and associated documentation 
	files (the "Software"). You do not have permission to use, copy, modify, merge, 
	publish, distribute, sublicense, and/or sell copies of the Software. The 
	Software is subject to the following conditions:

	The above copyright notice shall be included in all copies or substantial portions
	of the Software.
	
	If you have obtained permission to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software please note the following:

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
 */

package au.TheMrJezza.HorseTpWithMe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.creativegates.EngineMain;
import com.massivecraft.creativegates.entity.UConf;
import com.massivecraft.creativegates.entity.UConfColls;
import com.massivecraft.massivecore.MassiveCore;

public class HorseTpWithMeMain extends JavaPlugin implements Listener {
	private HashMap<Player, Entity> map = new HashMap<Player, Entity>();
	private HashSet<UUID> uuidSet = new HashSet<UUID>();
	private boolean cgates;
	public PluginManager pm;
	private boolean usePerm;
	private boolean tpPigs;
	private boolean needSaddle;
	private String noPerm;
	private List<String> bListWorlds;
	private String noEnterWorld;
	private String noLeaveWorld;
	private FileConfiguration config;
	public static HorseTpWithMeMain instance;

	public void onEnable() {
		instance = this;
		pm = getServer().getPluginManager();
		config = getConfig();
		cgates = false;
		cgates = creativeGateHelper();
		pm.registerEvents(this, this);
		getLogger().info("\033[32;1mHorseTpWithMe v1.4.12 is Enabled and working.\033[0;m");
		reloadTheConfig();
		saveDefaultConfig();
	}

	public void onDisable() {
		config = null;
		pm = null;
		cgates = false;
		instance = null;
	}

	private boolean creativeGateHelper() {
		if (pm.getPlugin("CreativeGates") != null && pm.getPlugin("MassiveCore") != null)
			return true;
		return false;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onAnimalTeleport(AnimalTeleportEvent evt) {
		Player player = evt.getRider();
		Entity entity = evt.getEntity();
		if (uuidSet.contains(player.getUniqueId())) {
			evt.setCancelled(true);
			uuidSet.remove(player.getUniqueId());
			return;
		}
		if (player.isInsideVehicle()) {
			evt.setCancelled(true);
			return;
		}
		if (!player.hasPermission("horsey.ignoreblacklist")) {
			if (isBlocked(evt.getFrom().getWorld().getName())) {
				player.sendMessage(noLeaveWorld.replace("{ANIMAL}", capitalize(entity.getType().name().toLowerCase())));
				evt.setCancelled(true);
				return;
			}
			if (isBlocked(evt.getDestination().getWorld().getName())) {
				player.sendMessage(noEnterWorld.replace("{ANIMAL}", capitalize(entity.getType().name().toLowerCase())));
				evt.setCancelled(true);
				return;
			}
		}
	}

	private boolean isBlocked(String name) {
		if (bListWorlds.contains(name))
			return true;
		return false;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent evt) {
		if (evt.getEntity().isInsideVehicle()) {
			final UUID uuid = evt.getEntity().getUniqueId();
			uuidSet.add(uuid);
			evt.getEntity().leaveVehicle();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onVehicleEnter(VehicleEnterEvent evt) {
		if (evt.isCancelled() && evt.getEntered() instanceof Player) {
			final UUID uuid = evt.getEntered().getUniqueId();
			uuidSet.add(uuid);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onVehicleExit(VehicleExitEvent evt) {
		boolean bool = evt.isCancelled();
		final Vehicle vehicle = evt.getVehicle();
		if (!(evt.getExited() instanceof Player))
			return;
		evt.setCancelled(false);
		if (vehicle instanceof Horse) {
			Horse horse = (Horse) vehicle;
			if (!horse.isTamed())
				return;
			if (horse.getInventory().getSaddle() == null && needSaddle == true)
				return;
		} else if (vehicle instanceof Pig) {
			if (tpPigs == false)
				return;
		} else
			return;
		if (!vehicle.isOnGround()) {
			Material type = vehicle.getLocation().getBlock().getType();
			if (type != Material.AIR) {
				if (cgates == false)
					return;
				UConf uconf = UConfColls.get().getForWorld(vehicle.getWorld().getName()).get(MassiveCore.INSTANCE);
				if (!uconf.isUsingWater())
					return;
				if (!EngineMain.isGateNearby(vehicle.getLocation().getBlock()))
					return;
			}
		}
		final Player player = (Player) evt.getExited();
		if (player.isSneaking()) {
			if (bool == true) {
				new BukkitRunnable() {
					public void run() {
						vehicle.setPassenger(player);
					}
				}.runTaskLater(this, 1L);
			}
			return;
		}
		if (player.hasPermission("horsey.teleport") || !usePerm) {
			map.put(player, vehicle);
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerTeleport(PlayerTeleportEvent evt) {
		final Player player = evt.getPlayer();
		if (map.containsKey(player)) {
			evt.getTo().getChunk().load();
			final LivingEntity entity = (LivingEntity) map.get(player);
			map.remove(player);
			new BukkitRunnable() {
				public void run() {
					Location loc = player.getLocation();
					if (callAnimalTPEvt(entity, player, loc) == true) {
						if (player.isInsideVehicle())
							return;
						entity.setFallDistance(0f);
						entity.teleport(loc.add(0.0D, 0.5D, 0.0D));
						entity.getLocation().getChunk().load();
						new BukkitRunnable() {
							public void run() {
								entity.setPassenger(player);
							}
						}.runTaskLater(instance, 10L);
					}
				}
			}.runTaskLater(this, 1L);
		}
	}

	@EventHandler
	private void onEntityDamage(EntityDamageEvent evt) {
		Entity entity = evt.getEntity();
		if ((entity instanceof Horse || entity instanceof Pig) && entity.getPassenger() != null
				&& entity.getPassenger() instanceof Player && evt.getCause() == DamageCause.SUFFOCATION)
			evt.setCancelled(true);
	}

	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("eject")) {
			Player player = (Player) cs;
			player.leaveVehicle();
			return true;
		} else {
			if (cs.hasPermission("horsey.reload")) {
				reloadTheConfig();
				cs.sendMessage("§7[§2HorseTpWithMe§7: §aConfig Reloaded.§7]");
				return true;
			}
			cs.sendMessage(noPerm);
			return true;
		}
	}

	private String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	private void reloadTheConfig() {
		reloadConfig();
		usePerm = config.getBoolean("Use-A-Permission");
		tpPigs = config.getBoolean("TeleportPigs");
		needSaddle = config.getBoolean("RequireSaddle");
		noPerm = ChatColor.translateAlternateColorCodes('&', config.getString("NoPermMessage"));
		bListWorlds = config.getStringList("Blacklisted-Worlds");
		noEnterWorld = ChatColor.translateAlternateColorCodes('&', config.getString("Blacklisted-World-Message"));
		noLeaveWorld = ChatColor.translateAlternateColorCodes('&', config.getString("Blacklisted-World-Exit-Message"));
	}

	public boolean callAnimalTPEvt(LivingEntity entity, Player player, Location destination) {
		AnimalTeleportEvent animaltpevent = new AnimalTeleportEvent(entity, player, destination);
		pm.callEvent(animaltpevent);
		return (!animaltpevent.isCancelled() == true);
	}
}