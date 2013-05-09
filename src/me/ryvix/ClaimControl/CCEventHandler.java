/**
 * ClaimControl - Provides more control over Grief Prevention claims.
 * Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 * The MIT License (MIT) - See LICENSE.txt
 *
 */
package me.ryvix.ClaimControl;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CCEventHandler implements Listener {

	private final ClaimControl plugin;

	/**
	 * ClaimControl Event Handler Constructor
	 *
	 * @param plugin
	 */
	public CCEventHandler(ClaimControl plugin) {
		this.plugin = plugin;
	}

	/**
	 * Detect player bed enter
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		Player player = event.getPlayer();
		Location location = event.getBed().getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You aren't allowed to do that!");
		}
	}

	/**
	 * Detect player empty bucket
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerEmptyBucket(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		Location location = event.getBlockClicked().getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You aren't allowed to do that!");
		}
	}

	/**
	 * Detect player fill bucket
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerFillBucket(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		Location location = event.getBlockClicked().getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You aren't allowed to do that!");
		}
	}

	/**
	 * Detect player commands
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You aren't allowed to do that!");
		}
	}

	/**
	 * Detect player interacting with entities
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Location location = event.getRightClicked().getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You aren't allowed to do that!");
		}
	}

	/**
	 * Detect player interact
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		Location location = null;
		Action action = event.getAction();

		if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK || action == Action.PHYSICAL) {
			// if they clicked a block check where the block is
			Block clickedBlock = event.getClickedBlock();
			if (clickedBlock == null) {
				return;
			}
			location = clickedBlock.getLocation();

		} else if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
			// if they clicked air check where the player is looking
			location = player.getEyeLocation();
		}

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			// player.sendMessage(ChatColor.RED + "You aren't allowed to do that!");
		}
	}

	/**
	 * Detect player move
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		final String playerName = player.getName();
		plugin.addCheckPlayer(playerName, new CheckPlayer(playerName, event.getTo()));
	}

	/**
	 * Detect player pickup item
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		Location location = event.getItem().getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			// player.sendMessage(ChatColor.RED + "You aren't allowed there!");
		}
	}

	/**
	 * Detect player drop item
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		Location location = event.getItemDrop().getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			// player.sendMessage(ChatColor.RED + "You aren't allowed there!");
		}
	}

	/**
	 * Detect player shear
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		Player player = event.getPlayer();
		Location location = event.getEntity().getLocation();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You aren't allowed to do that!");
		}
	}

	/**
	 * Detect player teleport
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location location = event.getTo();

		// check if the claim is protected
		Boolean isClaim = plugin.claim.check(location);

		// keep players out but let admins in claims
		if (isClaim && !plugin.claim.canEnter(player, location) && !player.hasPermission("claimcontrol.admin")) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "You aren't allowed to go there!");
		}
	}

	/**
	 * Detect player portal
	 *
	 * @param event
	 */
	/*
	 * @EventHandler(priority = EventPriority.LOWEST) public void onPlayerPortal(PlayerPortalEvent event) { Player player = event.getPlayer(); Location fromLocation = event.getFrom(); Location
	 * toLocation = event.getTo();
	 * 
	 * // check if the claim is protected Boolean isClaimFrom = plugin.claim.check(player, fromLocation); Boolean isClaimTo = plugin.claim.check(player, toLocation);
	 * 
	 * // keep players out but let admins in claims if ((isClaimFrom || isClaimTo) && (!plugin.claim.canEnter(player, fromLocation) || !plugin.claim.canEnter(player, toLocation)) &&
	 * !player.hasPermission("claimcontrol.admin")) { event.setCancelled(true); player.sendMessage(ChatColor.RED + "You aren't allowed to go there!"); } }
	 */
	/**
	 * Detect portal create
	 *
	 * @param event
	 */
	/*
	 * @EventHandler(priority = EventPriority.LOWEST) public void onPortalCreate(PortalCreateEvent event) { reason = event.getReason(); //blocks = event.getBlocks();
	 * 
	 * // check if the claim is protected Boolean isClaimFrom = plugin.claim.check(player, fromLocation); Boolean isClaimTo = plugin.claim.check(player, toLocation);
	 * 
	 * // keep players out but let admins in claims if ((isClaimFrom || isClaimTo) && (!plugin.claim.canEnter(player, fromLocation) || !plugin.claim.canEnter(player, toLocation)) &&
	 * !player.hasPermission("claimcontrol.admin")) { event.setCancelled(true); player.sendMessage(ChatColor.RED + "You aren't allowed to go there!"); } }
	 */
	/**
	 * Prevent PvP if PvP is disabled
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		// System.out.println("Damage: " + event.getDamage());
		// System.out.println("Cause: " + event.getCause().name());

		Entity victim = event.getEntity();

		// check if entity receiving damage is a player
		if (!(victim instanceof Player)) {
			return;
		}

		Entity damager = event.getDamager();

		// check if entity dealing damage is a player
		if (!(damager instanceof Player)) {
			return;
		}

		// if player is in a claim
		if (plugin.claim.check(victim.getLocation())) {

			// if no pvp allowed
			if (!plugin.flags.getPvp(plugin.claim.getId(victim.getLocation()))) {

				// stop projectiles
				if (damager instanceof Projectile) {
					damager.remove();
				}

				event.setCancelled(true);
			}
		}
	}

	/**
	 * Prevent combuster if PvP is disabled
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityCombustByEntityEvent(EntityCombustByEntityEvent event) {

		Entity victim = event.getEntity();

		// check if entity receiving damage is a player
		if (!(victim instanceof Player)) {
			return;
		}

		Entity combuster = event.getCombuster();

		// check if combuster is a projectile
		if (combuster instanceof Projectile) {

			// if player is in a claim
			if (plugin.claim.check(victim.getLocation())) {

				// if no pvp allowed
				if (!plugin.flags.getPvp(plugin.claim.getId(victim.getLocation()))) {
					combuster.remove();
					event.setCancelled(true);
				}
			}
		}

	}

	/**
	 * Prevent splash potions if PvP is disabled
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPotionSplashEvent(PotionSplashEvent event) {

		List<LivingEntity> affectedEntities = (List<LivingEntity>) event.getAffectedEntities();
		ThrownPotion potion = event.getPotion();
		Entity shooter = potion.getShooter();

		// check if entity dealing damage is a player
		if (!(shooter instanceof Player)) {
			return;
		}

		// check all affected entities
		for (LivingEntity victim : affectedEntities) {

			// check if entity receiving damage is a player
			if (!(victim instanceof Player)) {
				continue;
			}

			// if player is in a claim
			if (plugin.claim.check(victim.getLocation())) {

				// if no pvp allowed
				if (!plugin.flags.getPvp(plugin.claim.getId(victim.getLocation()))) {

					// block potions except on the victim
					if (shooter.getEntityId() != victim.getEntityId()) {
						event.setIntensity(victim, -1.0);
					}
				}
			}
		}
	}

	/**
	 * Prevent monsters if they are disabled
	 *
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {

		Entity entity = event.getEntity();

		// only monsters
		if (!(entity instanceof Monster)) {
			return;
		}

		// if monster is in a claim
		if (plugin.claim.check(entity.getLocation())) {

			// if no monsters allowed
			if (!plugin.flags.getMonsters(plugin.claim.getId(entity.getLocation()))) {

				// don't remove this mob if it's already set to be removed
				if (plugin.getEntityUUIDs().contains(entity.getUniqueId())) {
					return;
				}

				event.setCancelled(true);
			}
		}
	}
}