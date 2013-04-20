/**
 *   ClaimControl - Provides more control over Grief Prevention claims.
 *   Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 *   The MIT License (MIT) - See LICENSE.txt
 *
 */

package me.ryvix.ClaimControl;

import org.bukkit.Location;

/**
 * CheckPlayer object This will store player names and their last locations.
 * Used for saving their locations for each check to see where they were.
 */
public class CheckPlayer {
	private String name;
	private Location location;

	// constructor
	public CheckPlayer(String n, Location l) {
		this.setName(n);
		this.setLocation(l);
	}

	// get player name
	public String getName() {
		return name;
	}

	// set player name
	synchronized void setName(String name) {
		this.name = name;
	}

	//
	public Location getLocation() {
		return location;
	}

	synchronized void setLocation(Location location) {
		this.location = location;
	}
}
