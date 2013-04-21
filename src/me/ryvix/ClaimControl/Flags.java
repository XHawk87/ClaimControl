/**
 *   ClaimControl - Provides more control over Grief Prevention claims.
 *   Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 *   The MIT License (MIT) - See LICENSE.txt
 *
 */

package me.ryvix.ClaimControl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Flags {
	// public static String validFlags = "animals|monsters|pvp|entrymsg|exitmsg|charge|time|trust|private|box|allow|deny";
	public static String validFlags = "animals|monsters|pvp|entrymsg|exitmsg|private|allow|deny";
	private static SQLFunctions sql;
	private static ClaimControl plugin;

	/**
	 * Constructor
	 * 
	 * @param claimControl
	 * 
	 * @param sql
	 */
	public Flags(ClaimControl claimControl, SQLFunctions sql) {
		Flags.plugin = claimControl;
		Flags.sql = sql;
	}

	/**
	 * Checks if flag is a valid flag
	 * 
	 * @param input
	 * @return
	 */
	public boolean valid(String input) {
		Pattern flags = Pattern.compile("(?i)^" + validFlags + "$");
		Matcher matcher = flags.matcher(input);
		while (matcher.find()) {
			if (matcher.group().length() != 0) {
				if (flags.matcher(input).find()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * List flags
	 * 
	 * @param loc
	 * @return
	 */
	public String list(Location loc) {
		List<String> flags = new ArrayList<String>();
		String output = "";
		try {

			flags = sql.select(plugin.claim.getId(loc));

			if (flags.size() > 0) {

				// show only one allow and deny
				List<String> newflags = new ArrayList<String>();
				boolean allow = false;
				boolean deny = false;
				for (String flag : flags) {
					if (flag.equals("allow")) {
						if (!allow) {
							allow = true;
						} else {
							continue;
						}
					}
					if (flag.equals("deny")) {
						if (!deny) {
							deny = true;
						} else {
							continue;
						}
					}
					newflags.add(flag);
				}

				output = StringUtils.join(newflags, ", ");
				output = ChatColor.GREEN + "Flags: " + ChatColor.GOLD + output;
			} else {
				output = ChatColor.RED + "No flags found.";
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return ChatColor.RED + "Sorry, there was an error retrieving flags for this claim.";
		}

		return output;
	}

	/**
	 * List values of a flag
	 * 
	 * @param loc
	 * @return
	 */
	public String list(Location loc, String flag) {
		List<String> values = new ArrayList<String>();
		String output = "";
		try {

			values = sql.select(plugin.claim.getId(loc), flag);

			if (values.size() > 0) {
				output = StringUtils.join(values, ", ");
				output = ChatColor.GREEN + flag + ": " + ChatColor.GOLD + output;
			} else {
				output = ChatColor.RED + "No values found for " + flag;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return ChatColor.RED + "Sorry, there was an error retrieving values for " + flag;
		}

		return output;
	}

	/**
	 * Removes a flag from the database
	 * 
	 * @param claimid
	 * @param flag
	 */
	public void removeFlag(Long claimid, String flag) {
		try {
			sql.delete(claimid, flag);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes a flag from the database
	 * 
	 * @param claimid
	 * @param flag
	 * @param value
	 */
	public void removeFlag(Long claimid, String flag, String value) {
		try {
			sql.delete(claimid, flag, value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a flag and value to the database
	 * 
	 * @param claimid
	 * @param flag
	 * @param value
	 */
	public void addFlag(Long claimid, String flag, String value) {
		try {
			sql.insert(claimid, flag, value);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a flag's value from the database
	 * 
	 * @param claimid
	 * @param flag
	 * @return
	 */
	public String getFlag(Long claimid, String flag) {
		try {
			List<String> values = sql.select(claimid, flag);

			if (values.size() == 0) {
				return null;
			}
			return values.get(0);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets a flag's value using a value
	 * 
	 * @param claimid
	 * @param flag
	 * @param value
	 * @return
	 */
	public String getFlag(Long claimid, String flag, String value) {
		try {
			List<String> values = sql.select(claimid, flag, value);

			if (values.size() == 0) {
				return null;
			}
			return values.get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Check if a flag exists for a claim
	 * 
	 * @param claimid
	 * @param flag
	 * @return
	 */
	public boolean hasFlag(Long claimid, String flag) {
		try {
			List<String> values = sql.select(claimid, flag);

			if (values.size() == 0) {
				return false;
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Check if a flag with value exists for a claim
	 * 
	 * @param claimid
	 * @param flag
	 * @return
	 */
	public boolean hasFlag(Long claimid, String flag, String value) {
		try {
			List<String> values = sql.select(claimid, flag, value);

			if (values.size() == 0) {
				return false;
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Set flags
	 */

	public void setAnimals(Long claimid, String value) {
		addFlag(claimid, "animals", value);
	}

	public void setMonsters(Long claimid, String value) {
		addFlag(claimid, "monsters", value);
	}

	public void setPvp(Long claimid, String value) {
		addFlag(claimid, "pvp", value);
	}

	public void setCharge(Long claimid, String value) {
		addFlag(claimid, "charge", value);
	}

	public void setTime(Long claimid, String value) {
		// TODO make sure charge is set
		addFlag(claimid, "time", value);
	}

	public void setTrust(Long claimid, String value) {
		// TODO parse trust
		// accesstrust
		// buildtrust
		// permissiontrust
		// containertrust
		addFlag(claimid, "trust", value);
	}

	public void setEntryMsg(Long claimid, String value) {
		addFlag(claimid, "entrymsg", value);
	}

	public void setExitMsg(Long claimid, String value) {
		addFlag(claimid, "exitmsg", value);
	}

	public void setPrivate(Long claimid, String value) {
		addFlag(claimid, "private", value);
	}

	public void setBox(Long claimid, String value) {
		addFlag(claimid, "box", value);
	}

	public void setAllow(Long claimid, String value) {
		// if a player is added to allow remove them from deny
		if (hasFlag(claimid, "deny", value)) {
			removeFlag(claimid, "deny", value);
		}
		addFlag(claimid, "allow", value);
	}

	public void setDeny(Long claimid, String value) {
		// if a player is added to deny remove them from allow
		if (hasFlag(claimid, "allow", value)) {
			removeFlag(claimid, "allow", value);
		}
		addFlag(claimid, "deny", value);
	}

	/**
	 * Get flags
	 */

	public boolean getAnimals(Long claimid) {
		String result = getFlag(claimid, "animals");
		if (result == null) {
			result = "true";
		}
		return Boolean.valueOf(result);
	}

	public boolean getMonsters(Long claimid) {
		String result = getFlag(claimid, "monsters");
		if (result == null) {
			result = "true";
		}
		return Boolean.valueOf(result);
	}

	public boolean getPvp(Long claimid) {
		String result = getFlag(claimid, "pvp");
		if (result == null) {
			result = "true";
		}
		return Boolean.valueOf(result);
	}

	public double getCharge(Long claimid) {
		String result = getFlag(claimid, "charge");
		if (result == null) {
			result = "0";
		}
		return Double.valueOf(result);
	}

	public String getTime(Long claimid) {
		// TODO parse time
		// 1w1d1h1m
		String result = getFlag(claimid, "time");
		if (result == null) {
			result = "0";
		}
		return result;
	}

	public String getTrust(Long claimid) {
		// TODO parse trust
		// accesstrust
		// buildtrust
		// permissiontrust
		// containertrust
		String result = getFlag(claimid, "trust");
		if (result == null) {
			result = "";
		}
		return result;
	}

	public String getEntryMsg(Long claimid) {
		String result = getFlag(claimid, "entrymsg");
		if (result == null) {
			result = "";
		}
		return result;
	}

	public String getExitMsg(Long claimid) {
		String result = getFlag(claimid, "exitmsg");
		if (result == null) {
			result = "";
		}
		return result;
	}

	public boolean getPrivate(Long claimid) {
		String result = getFlag(claimid, "private");
		// System.out.println("private result: " + result);
		if (result == null) {
			result = "false";
		}
		return Boolean.valueOf(result);
	}

	public boolean getBox(Long claimid) {
		String result = getFlag(claimid, "box");
		if (result == null) {
			result = "false";
		}
		return Boolean.valueOf(result);
	}

	public String getAllow(Long claimid, String player) {
		String result = getFlag(claimid, "allow", player);
		if (result == null) {
			result = "";
		}
		return result;
	}

	public String getDeny(Long claimid, String player) {
		String result = getFlag(claimid, "deny", player);
		if (result == null) {
			result = "";
		}
		return result;
	}
}
