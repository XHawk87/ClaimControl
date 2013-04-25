/**
 *   ClaimControl - Provides more control over Grief Prevention claims.
 *   Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 *   The MIT License (MIT) - See LICENSE.txt
 *
 */

package me.ryvix.ClaimControl;

import java.util.UUID;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * MobsTask
 * 
 * Checks claims for monsters and removes them if necessary.
 */
public class MobsTask extends BukkitRunnable {
	private final ClaimControl plugin;

	// Constructor
	public MobsTask(ClaimControl plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {

		// check each world
		for (World world : plugin.worlds) {
			if (GriefPrevention.instance.config_claims_enabledWorlds.contains(world)) {

				// check each LivingEntity in the world
				for (LivingEntity entity : world.getLivingEntities()) {

					// check if entity is a monster first
					if (!(entity instanceof Monster)) {
						continue;
					}

					// don't remove this mob if it's already set to be removed
					if (plugin.getEntityUUIDs().contains(entity.getUniqueId())) {
						continue;
					}

					// get entity location
					Location location = entity.getLocation();

					// make sure chunk is loaded at that location
					if (!location.getChunk().isLoaded()) {
						continue;
					}

					// check if location is in a claim
					if (plugin.claim.check(location)) {

						// check if claim has monsters flag
						if (!plugin.flags.getMonsters(plugin.claim.getId(location))) {

							// store entity id so we dont try removing it twice
							UUID id = entity.getUniqueId();
							plugin.addEntityUUID(id);

							// remove entity
							entity.remove();

							// store entity id so we dont try removing it twice
							plugin.removeEntityUUID(id);
						}
					}
				}
			}
		}
	}
}
