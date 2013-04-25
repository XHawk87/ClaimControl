/**
 *   ClaimControl - Provides more control over Grief Prevention claims.
 *   Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 *   The MIT License (MIT) - See LICENSE.txt
 *
 */

package me.ryvix.ClaimControl;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Claim {
	private static ClaimControl plugin;

	public Claim(ClaimControl plugin) {
		Claim.plugin = plugin;
	}

	/**
	 * Check if a player can enter an area
	 * 
	 * @param player
	 * @param location
	 * @return
	 */
	public boolean canEnter(Player player, Location location) {
		long claimid = getId(location);
		if (claimid == 0) {
			return true;
		}

		String playerName = player.getName();

		// fee to enter
		double charge = plugin.flags.getCharge(claimid);

		// player is the owner?
		String owner = getOwner(location);
		boolean isOwner = owner.equalsIgnoreCase(playerName);

		// claim is private?
		boolean isPrivate = plugin.flags.getPrivate(claimid);

		// System.out.println("isPrivate:" + isPrivate);
		// System.out.println("isOwner:" + isOwner);
		// System.out.println("charge:" + charge);

		// is the claim private or is the claim charged
		if ((!isPrivate && charge == 0) || isOwner) {
			// System.out.println("checkpoint 1");

			// the player isn't on deny list
			if (!plugin.flags.getDeny(claimid, playerName).equalsIgnoreCase(playerName) || isOwner) {
				// they may enter
				// System.out.println("checkpoint 1.1");
				return true;
			}
			/*
			 * } else if (charge > 0 && !getOwner(location).equalsIgnoreCase(playerName)) {
			 * 
			 * // TODO: allow them to enter and save a timer somehow String time = plugin.flags.getTime(claimid); String timeText; if (time == "forever") { timeText = "forever"; } else { timeText =
			 * " for " + time + " minutes."; } player.sendMessage(ChatColor.RED + "It costs " + plugin.econ.format(charge).toString() + " to enter that area " + timeText +
			 * ". Type /acceptcharge to accept the charge and enter.");
			 */
		} else if (isPrivate) {
			// System.out.println("checkpoint 2");

			// the player is on allow list
			if (plugin.flags.getAllow(claimid, playerName).equalsIgnoreCase(playerName)) {
				// they may enter
				// System.out.println("checkpoint 2.1");
				return true;
			}

			/*
			 * if(player != null) { player.sendMessage(ChatColor.RED + "This is a private claim!"); }
			 */
		}

		// System.out.println("checkpoint 3");
		return false;
	}

	/**
	 * Checks if location is in a claim or not
	 * 
	 * @param coords
	 * @return
	 */
	Boolean check(Location loc) {
		try {

			// check if Grief Prevention is enabled in this world
			if (GriefPrevention.instance.config_claims_enabledWorlds.contains(loc.getWorld())) {

				// get claim
				me.ryanhamshire.GriefPrevention.Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
				if (claim != null) {
					return true;
				}

			}

		} catch (Exception e) {

			if (GriefPrevention.instance == null) {
				plugin.getLogger().warning("GriefPrevention.instance is null! Please report this to ClaimControl.");
			} else if (GriefPrevention.instance.dataStore == null) {
				plugin.getLogger().warning("GriefPrevention.instance.dataStore is null! Please report this to ClaimControl.");
			}

			plugin.getLogger().warning(e.getMessage());
		}

		return false;
	}

	/**
	 * Get a claims id value
	 * 
	 * @param location
	 * @return
	 */
	public long getId(Location loc) {

		// check if Grief Prevention is enabled in this world
		if (GriefPrevention.instance.config_claims_enabledWorlds.contains(loc.getWorld())) {

			// get claim
			me.ryanhamshire.GriefPrevention.Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
			if (claim != null) {
				Long claimid = claim.getID();
				return claimid;
			}
		}
		return 0L;
	}

	/**
	 * Get a claims owner value
	 * 
	 * @param coords
	 * @return
	 */
	public String getOwner(Location loc) {

		// check if Grief Prevention is enabled in this world
		if (GriefPrevention.instance.config_claims_enabledWorlds.contains(loc.getWorld())) {

			// get claim
			me.ryanhamshire.GriefPrevention.Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
			if (claim != null) {
				return claim.getOwnerName();
			}
		}
		return null;
	}

	public boolean canExit(Player player, Location lastLocation) {
		// TODO Auto-generated method stub
		return true;
	}
}
