/**
 * ClaimControl - Provides more control over Grief Prevention claims.
 * Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 * The MIT License (MIT) - See LICENSE.txt
 *
 */
package me.ryvix.claimcontrol;

import java.util.Map.Entry;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * ClaimTasks
 *
 * Checks claim access for all players that have moved since last check.
 *
 * Will teleport players outside the lower corner who have no access to the claim they are in.
 */
public class ClaimTask extends BukkitRunnable {

	private final ClaimControl plugin;

	// Constructor
	public ClaimTask(ClaimControl plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {

		// Loop through players that have moved
		for (Entry<String, CheckPlayer> checkPlayer : plugin.getCheckPlayers().entrySet()) {

			// Player name from MovedPlayer object
			String playerName = checkPlayer.getKey();

			// Player object from server
			final Player player = plugin.getServer().getPlayer(playerName);

			// If there is no player anymore go to next moved player
			if (player == null) {
				continue;
			}

			// Check player object
			CheckPlayer checkPlayerObj = checkPlayer.getValue();

			// Last location of the player
			Location lLocation = checkPlayerObj.getLocation();

			// Current location of the player
			Location cLocation = player.getLocation();

			// If the aren't in the same spot
			if (lLocation.getBlockX() != cLocation.getBlockX() || lLocation.getBlockY() != cLocation.getBlockY() || lLocation.getBlockZ() != cLocation.getBlockZ()) {

				// send entry/exit flags
				Boolean sendCEntry = false;
				Boolean sendLExit = false;

				// last and current claim ids
				long lClaimId = plugin.claim.getId(lLocation);
				long cClaimId = plugin.claim.getId(cLocation);

				// Is their current location inside a claim?
				if (cClaimId != 0L) {

					// Check if player cannot enter the claim (is private or they are on deny list)
					// Let admins in claims
					boolean canEnter = plugin.claim.canEnter(player, cLocation);
					if (!canEnter && !player.hasPermission("claimcontrol.admin")) {

						// Eject player from the claim (outside lower corner)
						GriefPrevention gp = new GriefPrevention();
						gp.ejectPlayer(player);

						// Tell them they have no access
						player.sendMessage(ChatColor.RED + "You aren't allowed there!");

						// No further checks are needed so return
						// do not remove player from movedPlayers HashMap because they were moved again
						return;

					} else {
						// They can enter the claim
						// Was their last location inside a different claim?

						// Are they still in a claim?
						if (lClaimId != 0L && cClaimId != 0L) {

							// Are they not in the same claim?
							if (lClaimId != cClaimId) {
								sendLExit = true;
								sendCEntry = true;
							}

						} else {
							// Are they not in the same claim?
							if (lClaimId != cClaimId) {
								sendCEntry = true;
							}
						}
					}

				} else {
					// They are not in a claim
					// Check if they were in a claim

					// Is their last location in a claim?
					if (lClaimId != 0L && plugin.claim.canExit(player, lLocation)) {

						// They came out of a claim so send exit message
						sendLExit = true;
					}
				}

				// Exit message of last claim
				if (sendLExit) {
					String exitMsg = plugin.flags.getExitMsg(lClaimId);
					if (!exitMsg.isEmpty()) {
						player.sendMessage(ChatColor.translateAlternateColorCodes("&".charAt(0), exitMsg));
					}
				}

				// Entry message of current claim
				if (sendCEntry) {
					String entryMsg = plugin.flags.getEntryMsg(cClaimId);
					if (!entryMsg.isEmpty()) {
						player.sendMessage(ChatColor.translateAlternateColorCodes("&".charAt(0), entryMsg));
					}
				}
			}

			// Remove player from movedPlayers HashMap
			plugin.removeCheckPlayer(playerName);
		}
	}
}
