/**
 * ClaimControl - Provides more control over Grief Prevention claims.
 * Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 * The MIT License (MIT) - See LICENSE.txt
 *
 */
package me.ryvix.claimcontrol;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Claim {

	private static ClaimControl plugin;

	public Claim(ClaimControl plugin) {
		Claim.plugin = plugin;
	}

	public me.ryanhamshire.GriefPrevention.Claim getGPClaim(Location location) {
		me.ryanhamshire.GriefPrevention.Claim claim = plugin.GP.dataStore.getClaimAt(location, false, null);
		return claim;
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
	public Boolean check(Location loc) {
		try {

			// check if Grief Prevention is enabled in this world
			if (plugin.GP.config_claims_enabledWorlds.contains(loc.getWorld())) {

				// get claim
				if (getGPClaim(loc) != null) {
					return true;
				}

			}

		} catch (Exception e) {

			if (plugin.GP == null) {
				plugin.getLogger().warning("GriefPrevention instance is null! Please report this to ClaimControl.");
			} else if (plugin.GP.dataStore == null) {
				plugin.getLogger().warning("GriefPrevention dataStore is null! Please report this to ClaimControl.");
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
		if (plugin.GP.config_claims_enabledWorlds.contains(loc.getWorld())) {

			// get claim
			me.ryanhamshire.GriefPrevention.Claim claim = getGPClaim(loc);
			if (claim != null) {

				if (claim.parent == null) {
					// parent claim
					Long claimid = claim.getID();
					return claimid;

				} else {
					// GP has a bug where subdivision don't get id's
					// As a workaround we will just use the parent claim until this is fixed
					Long claimid = claim.parent.getID();
					return claimid;
				}
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
		if (plugin.GP.config_claims_enabledWorlds.contains(loc.getWorld())) {

			// get claim
			me.ryanhamshire.GriefPrevention.Claim claim = getGPClaim(loc);
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

	/**
	 * Remove all flags for a claim
	 *
	 * @param claimid
	 */
	public void remove(long claimid) {
		plugin.flags.removeAllFlags(claimid);
	}
}
