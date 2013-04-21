/**
 *   ClaimControl - Provides more control over Grief Prevention claims.
 *   Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 *   The MIT License (MIT) - See LICENSE.txt
 *
 */

package me.ryvix.ClaimControl;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.milkbowl.vault.economy.Economy;
import me.ryvix.ClaimControl.Claim;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
//import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ClaimControl extends JavaPlugin {

	// Initialize variables
	public Configuration config;
	private SQLFunctions sql;
	public Flags flags;
	public Claim claim;
	public Economy econ;
	private ConcurrentHashMap<String, CheckPlayer> checkPlayers = new ConcurrentHashMap<String, CheckPlayer>();
	private BukkitTask claimTask;
	private BukkitTask mobsTask;
	public List<World> worlds;
	public String dbType;

	/**
	 * Runs when plugin is enabled
	 * 
	 */
	public void onEnable() {

		// check for GriefPrevention plugin
		if (this.getServer().getPluginManager().getPlugin("GriefPrevention") == null) {
			this.getLogger().severe("ClamControl requires the Grief Prevention plugin but it wasn't found, disabling ClamControl!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// check for Vault plugin
		/*
		 * if (this.getServer().getPluginManager().getPlugin("Vault") == null) { this.getLogger().severe("ClamControl requires the Vault plugin but it wasn't found, disabling ClamControl!");
		 * getServer().getPluginManager().disablePlugin(this); return; }
		 */

		// setup Vault Economy, requires economy plugin
		/*
		 * if (!setupEconomy()) { this.getLogger().severe("ClamControl requires an economy plugin supported by Vault but one wasn't found, disabling ClamControl!");
		 * getServer().getPluginManager().disablePlugin(this); return; }
		 */

		// create config file
		try {
			File configFile = new File(getDataFolder(), "config.yml");
			if (!configFile.exists()) {
				getDataFolder().mkdir();
				this.getConfig().options().copyDefaults(true);
				this.getConfig().options().header("ClaimControl config file\n" + "message: The message to display when denying entry. Default Access denied!");
				this.getConfig().options().copyHeader(true);
				this.saveConfig();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// load config file
		loadConfig();

		// register events
		CCEventHandler ccEventHandler = new CCEventHandler(this);
		this.getServer().getPluginManager().registerEvents(ccEventHandler, this);
	}

	/**
	 * Setup Vault Economy
	 * 
	 * @return
	 */
	/*
	 * private boolean setupEconomy() {
	 * 
	 * RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class); if (rsp == null) { return false; }
	 * 
	 * econ = rsp.getProvider();
	 * 
	 * return econ != null; }
	 */

	/**
	 * Runs when plugin is disabled
	 * 
	 */
	public void onDisable() {

		// stop tasks
		stopTasks();

		// close connection
		sql.close();

		// null some variables
		config = null;
		sql = null;
		flags = null;
		claim = null;
		econ = null;
		checkPlayers = new ConcurrentHashMap<String, CheckPlayer>();
		claimTask = null;
		mobsTask = null;
		worlds = null;
		dbType = null;
	}

	/**
	 * Load config values
	 * 
	 */
	public void loadConfig() {

		// get config file
		FileConfiguration getConfig = getConfig();
		File configFile = new File(this.getDataFolder() + "/config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);

		// add defaults
		if (!config.contains("config.default_flags.entrymsg")) {
			getConfig().addDefault("config.default.entrymsg", "");
		}
		if (!config.contains("config.default_flags.exitmsg")) {
			getConfig().addDefault("config.default.exitmsg", "");
		}
		if (!config.contains("config.default_flags.monsters")) {
			getConfig().addDefault("config.default.monsters", "true");
		}
		if (!config.contains("config.default_flags.private")) {
			getConfig().addDefault("config.default.private", "false");
		}
		if (!config.contains("config.default_flags.pvp")) {
			getConfig().addDefault("config.default.pvp", "true");
		}
		if (!config.contains("config.check_claims_ticks")) {
			getConfig().addDefault("config.check_claims_ticks", 60);
		}
		if (!config.contains("config.remove_monsters_ticks")) {
			getConfig().addDefault("config.remove_monsters_ticks", 60);
		}
		if (!config.contains("config.database")) {
			getConfig().addDefault("config.database", "sqlite");
		}
		if (!config.contains("config.mysql.url.host")) {
			getConfig().addDefault("config.mysql.url.host", "localhost");
		}
		if (!config.contains("config.mysql.url.port")) {
			getConfig().addDefault("config.mysql.url.port", 3306);
		}
		if (!config.contains("config.mysql.url.database")) {
			getConfig().addDefault("config.mysql.url.database", "database");
		}
		if (!config.contains("config.mysql.password")) {
			getConfig().addDefault("config.mysql.password", "");
		}
		if (!config.contains("config.mysql.username")) {
			getConfig().addDefault("config.mysql.username", "");
		}
		getConfig.options().copyDefaults(true);

		// add header
		getConfig().options().header("ClaimControl config file\n\n");
		getConfig().options().copyHeader(true);

		// save file
		saveConfig();

		// set values
		setVariables();

		// start tasks
		startTasks();

		// make sure an sql table is created
		sql.createTable();
	}

	/**
	 * Set config variables
	 * 
	 */
	public void setVariables() {

		worlds = getServer().getWorlds();
		dbType = config.getString("config.database").toLowerCase();

		// sql
		dbType = config.getString("config.database");
		if (dbType.equals("mysql")) {
			sql = new SQLFunctions(this, "[ClaimControl] ", config.getString("config.mysql.url.host"), config.getInt("config.mysql.url.port"), config.getString("config.mysql.url.database"), config.getString("config.mysql.username"), config.getString("config.mysql.password"));
		} else if (dbType.equals("sqlite")) {
			sql = new SQLFunctions(this, "[ClaimControl] ", this.getDataFolder().getAbsolutePath(), "ClaimControl");
		}

		// flags functions
		flags = new Flags(this, sql);

		// claim functions
		claim = new Claim(this);
	}

	/**
	 * Start runnable tasks
	 */
	private void startTasks() {
		// Claim task
		long claimTaskTime = Long.valueOf(config.getInt("config.check_claims_ticks"));
		if (claimTaskTime > 0) {
			this.getLogger().info("Starting claim check task. check_claims_ticks: " + config.getInt("config.check_claims_ticks"));
			// claimTask = new ClaimTask(this).runTaskTimerAsynchronously(this, 0, claimTaskTime);
			claimTask = new ClaimTask(this).runTaskTimer(this, 0, claimTaskTime);
		}

		// Mobs task
		long mobsTaskTime = Long.valueOf(config.getInt("config.remove_monsters_ticks"));
		if (mobsTaskTime > 0) {
			this.getLogger().info("Starting monster removal task. remove_monsters_ticks: " + config.getInt("config.remove_monsters_ticks"));
			mobsTask = new MobsTask(this).runTaskTimer(this, 0, mobsTaskTime);
		}
	}

	/**
	 * Stop runnable tasks
	 */
	private void stopTasks() {
		// Claim task
		long claimTaskTime = Long.valueOf(config.getInt("config.check_claims_ticks"));
		if (claimTaskTime > 0) {
			claimTask.cancel();
		}

		// Mobs task
		long mobsTaskTime = Long.valueOf(config.getInt("config.remove_monsters_ticks"));
		if (mobsTaskTime > 0) {
			mobsTask.cancel();
		}
	}

	/**
	 * Get players to check for claim permissions
	 * 
	 * @return
	 */
	public ConcurrentHashMap<String, CheckPlayer> getCheckPlayers() {
		return checkPlayers;
	}

	/**
	 * Get a player to check for claim permissions
	 * 
	 * @param playerName
	 * @return
	 */
	public CheckPlayer getCheckPlayer(String playerName) {
		return checkPlayers.get(playerName);
	}

	/**
	 * Add a player to check for claim permissions
	 * 
	 * @param playerName
	 * @param checkPlayer
	 */
	public void addCheckPlayer(String playerName, CheckPlayer checkPlayer) {
		// only add player if they don't already exist
		if (getCheckPlayer(playerName) == null) {
			this.checkPlayers.putIfAbsent(playerName, checkPlayer);
		}
	}

	/**
	 * Remove a player to check for claim permissions
	 * 
	 * @param playerName
	 */
	public void removeCheckPlayer(String playerName) {
		this.checkPlayers.remove(playerName);
	}

	/**
	 * Commands
	 * 
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		// /claimcontrol (alias /cc)
		if (cmd.getName().equalsIgnoreCase("claimcontrol")) {

			// help
			boolean showHelp = false;
			if (args.length == 0) {
				showHelp = true;
			} else if (args[0] != null && args[0].equalsIgnoreCase("help")) {
				showHelp = true;
			}
			if (showHelp) {
				sender.sendMessage(ChatColor.BLACK + "                     -+[[" + ChatColor.DARK_AQUA + " ClaimControl Help " + ChatColor.BLACK + "]]+-");
				sender.sendMessage(ChatColor.GRAY + "Parameters in <angle brackets> are required.");
				sender.sendMessage(ChatColor.GRAY + "Parameters in [square brackets] may be optional.");
				sender.sendMessage(ChatColor.YELLOW + "/claimcontrol flags" + ChatColor.WHITE + " See help on the flags available to use.");
				if (sender.hasPermission("claimcontrol.add")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol add <flag> <value>" + ChatColor.WHITE + " Add a value to a flag.");
				}
				if (sender.hasPermission("claimcontrol.remove")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol remove <flag> [value]" + ChatColor.WHITE + " Remove flag or a value from a flag.");
				}
				if (sender.hasPermission("claimcontrol.list")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol list [flagname]" + ChatColor.WHITE + " List the flags in the claim you are currently in or if a flag is given it will list the values for the given flag.");
				}
				if (sender.hasPermission("claimcontrol.flags.allow")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol allow <player>" + ChatColor.WHITE + " A shortcut to add a player to the allow list.");
				}
				if (sender.hasPermission("claimcontrol.flags.deny")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol deny <player>" + ChatColor.WHITE + " A shortcut to add a player to the deny list.");
				}
				if (sender.hasPermission("claimcontrol.flags.entrymsg")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol entrymsg <value>" + ChatColor.WHITE + " A shortcut to set the entrymsg flag.");
				}
				if (sender.hasPermission("claimcontrol.flags.exitmsg")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol exitmsg <value>" + ChatColor.WHITE + " A shortcut to set the exitmsg flag.");
				}
				if (sender.hasPermission("claimcontrol.flags.monsters")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol monsters" + ChatColor.WHITE + " A shortcut to toggle the monsters flag.");
				}
				if (sender.hasPermission("claimcontrol.flags.private")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol private" + ChatColor.WHITE + " A shortcut to toggle the private flag.");
				}
				if (sender.hasPermission("claimcontrol.flags.pvp")) {
					sender.sendMessage(ChatColor.YELLOW + "/claimcontrol pvp" + ChatColor.WHITE + " A shortcut to toggle the pvp flag.");
				}
				sender.sendMessage(ChatColor.BLACK + "               -+[[]][[]][[]][[]][[]][[]][[]][[]][[]][[]]+-");
				return true;
			}

			if (args[0].equalsIgnoreCase("flags")) {
				sender.sendMessage(ChatColor.BLACK + "                            -+[[" + ChatColor.DARK_AQUA + " Flags " + ChatColor.BLACK + "]]+-");
				/*
				 * if (sender.hasPermission("claimcontrol.flags.animals")) { sender.sendMessage(ChatColor.YELLOW + "animals" + ChatColor.WHITE +
				 * " Any animals that enter the claim without this flag will disappear. Values: true, false"); }
				 */
				if (sender.hasPermission("claimcontrol.flags.monsters")) {
					sender.sendMessage(ChatColor.YELLOW + "monsters" + ChatColor.WHITE + " Any monsters that enter the claim without this flag will disappear. Values: true, false. Default: false");
				}
				if (sender.hasPermission("claimcontrol.flags.pvp")) {
					sender.sendMessage(ChatColor.YELLOW + "pvp" + ChatColor.WHITE + " Adding this flag will enable PvP in the claim. Values: true, false. Default: false");
				}
				if (sender.hasPermission("claimcontrol.flags.entrymsg")) {
					sender.sendMessage(ChatColor.YELLOW + "entrymsg" + ChatColor.WHITE + " Set an entry message. Example: /claimcontrol add entrymsg Your entry message.");
				}
				if (sender.hasPermission("claimcontrol.flags.exitmsg")) {
					sender.sendMessage(ChatColor.YELLOW + "exitmsg" + ChatColor.WHITE + " Set an exit message. Example: /claimcontrol add exitmsg Your exit message.");
				}
				/*
				 * if (sender.hasPermission("claimcontrol.flags.charge")) { sender.sendMessage(ChatColor.YELLOW + "charge" + ChatColor.WHITE +
				 * " Charge players to enter. Example: /claimcontrol add charge 5"); } if (sender.hasPermission("claimcontrol.flags.time")) { sender.sendMessage(ChatColor.YELLOW + "time" +
				 * ChatColor.WHITE + " Set a time for the charge flag. After it expires they must pay again. Example: /claimcontrol add time 1w3d3h7m"); } if
				 * (sender.hasPermission("claimcontrol.flags.trust")) { sender.sendMessage(ChatColor.YELLOW + "trust" + ChatColor.WHITE +
				 * " Set the trust level they receive when they pay. Values: none, trust, container, access, permission"); }
				 */
				if (sender.hasPermission("claimcontrol.flags.allow")) {
					sender.sendMessage(ChatColor.YELLOW + "allow" + ChatColor.WHITE + " Allow a player to enter a private claim, bypassing any charges. Example: /claimcontrol add allow PlayerName");
				}
				if (sender.hasPermission("claimcontrol.flags.deny")) {
					sender.sendMessage(ChatColor.YELLOW + "deny" + ChatColor.WHITE + " Deny a player to enter a public claim. Example: /claimcontrol add deny PlayerName");
				}
				if (sender.hasPermission("claimcontrol.flags.private")) {
					sender.sendMessage(ChatColor.YELLOW + "private" + ChatColor.WHITE + " Set a claim as private, preventing anyone from entering unless you allow them. Note that you can use /claimcontrol private to toggle this value. Values: true, false. Default: false");
				}
				/*
				 * if (sender.hasPermission("claimcontrol.flags.box")) { sender.sendMessage(ChatColor.YELLOW + "box" + ChatColor.WHITE +
				 * " Set a claim as a box, preventing anyone from exiting unless you allow them. Values: true, false"); }
				 */
				sender.sendMessage(ChatColor.BLACK + "               -+[[]][[]][[]][[]][[]][[]][[]][[]][[]][[]]+-");
				return true;
			}

			// reload the plugin
			if (args[0].equalsIgnoreCase("reload")) {
				getServer().getPluginManager().disablePlugin(this);
				getServer().getPluginManager().enablePlugin(this);
				getLogger().info("Reloaded");
				if ((sender instanceof Player)) {
					sender.sendMessage(ChatColor.GREEN + "ClaimControl has been reloaded");
				}
				return true;
			}

			// player commands
			if (sender instanceof Player) {
				Player player = (Player) sender;
				Location loc = player.getLocation();

				// check if the claim is protected
				Boolean isClaim = claim.check(loc);
				if (!isClaim) {
					player.sendMessage(ChatColor.RED + "You are not in a claim.");
					return true;
				}

				Long claimid = claim.getId(loc);

				if (args.length == 1) {
					// /claimcontrol args[0]

					/**
					 * Player listing flags in a claim
					 * 
					 * @usage /claimcontrol list
					 * @permission claimcontrol.list
					 */
					if (args[0].equalsIgnoreCase("list")) {
						if (player.hasPermission("claimcontrol.list")) {

							// check if player owns the claim they are in
							String owner = claim.getOwner(loc);
							if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.list.others")) {
								player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
								return true;
							}

							String output = flags.list(loc);
							player.sendMessage(output);

							return true;
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						}
					}

					/**
					 * Toggles
					 */

					// toggle private flag
					if (args[0].equalsIgnoreCase("private") && player.hasPermission("claimcontrol.flags.private")) {

						// check if player owns the claim they are in
						String owner = claim.getOwner(loc);
						if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
							player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
							return true;
						}

						if (flags.getPrivate(claimid)) {
							flags.removeFlag(claimid, "private");
							flags.setPrivate(claimid, "false");
							player.sendMessage(ChatColor.GREEN + "This claim was set as public.");
						} else {
							flags.removeFlag(claimid, "private");
							flags.setPrivate(claimid, "true");
							player.sendMessage(ChatColor.GREEN + "This claim was set as private.");
						}
						return true;
					}

					// toggle pvp flag
					if (args[0].equalsIgnoreCase("pvp") && player.hasPermission("claimcontrol.flags.pvp")) {

						// check if player owns the claim they are in
						String owner = claim.getOwner(loc);
						if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
							player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
							return true;
						}

						if (flags.getPvp(claimid)) {
							flags.removeFlag(claimid, "pvp");
							flags.setPvp(claimid, "false");
							player.sendMessage(ChatColor.GREEN + "This claim was set as Non-PvP.");
						} else {
							flags.removeFlag(claimid, "pvp");
							flags.setPvp(claimid, "true");
							player.sendMessage(ChatColor.GREEN + "This claim was set as PvP.");
						}
						return true;
					}

					// toggle monsters flag
					if (args[0].equalsIgnoreCase("monsters") && player.hasPermission("claimcontrol.flags.monsters")) {

						// check if player owns the claim they are in
						String owner = claim.getOwner(loc);
						if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
							player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
							return true;
						}

						if (flags.getMonsters(claimid)) {
							flags.removeFlag(claimid, "monsters");
							flags.setMonsters(claimid, "false");
							player.sendMessage(ChatColor.GREEN + "This claim was set as a monster free zone.");
						} else {
							flags.removeFlag(claimid, "monsters");
							flags.setMonsters(claimid, "true");
							player.sendMessage(ChatColor.GREEN + "This claim was set to allow monsters.");
						}
						return true;
					}

				} else if (args.length == 2) {
					// /claimcontrol args[0] args[1]

					/**
					 * Player trying to add a new flag but not enough args
					 * 
					 * @usage /claimcontrol add <flag>
					 * @permission claimcontrol.add
					 */
					if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("set")) {
						if (player.hasPermission("claimcontrol.add")) {

							// check if player owns the claim they are in
							String owner = claim.getOwner(loc);
							if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
								player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
								return true;
							}

							player.sendMessage(ChatColor.RED + "Usage: /claimcontrol add <flag> <value>");
							return true;
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						}
					}

					// shortcut for allow flag
					if (args[0].equalsIgnoreCase("allow") && player.hasPermission("claimcontrol.flags.allow")) {

						// check if player owns the claim they are in
						String owner = claim.getOwner(loc);
						if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
							player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
							return true;
						}

						if (flags.hasFlag(claimid, "allow", args[1])) {
							player.sendMessage(ChatColor.RED + "That player is already allowed.");
						} else {
							flags.setAllow(claimid, args[1]);
							player.sendMessage(ChatColor.GREEN + args[1] + " was added to the allow list.");
						}
						return true;
					}

					// shortcut for deny flag
					if (args[0].equalsIgnoreCase("deny") && player.hasPermission("claimcontrol.flags.deny")) {

						// check if player owns the claim they are in
						String owner = claim.getOwner(loc);
						if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
							player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
							return true;
						}

						if (flags.hasFlag(claimid, "deny", args[1])) {
							player.sendMessage(ChatColor.RED + "That player is already denied.");
						} else {
							flags.setDeny(claimid, args[1]);
							player.sendMessage(ChatColor.GREEN + args[1] + " was added to the deny list.");
						}
						return true;
					}

					/**
					 * Player trying to remove a flag but not enough args
					 * 
					 * @usage /claimcontrol remove <flag>
					 * @permission claimcontrol.remove
					 */
					if (args[0].equalsIgnoreCase("remove")) {
						if (player.hasPermission("claimcontrol.remove")) {

							// check if player owns the claim they are in
							String owner = claim.getOwner(loc);
							if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
								player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
								return true;
							}

							// remove animals flag
							if (args[1].equalsIgnoreCase("animals")) {
								flags.removeFlag(claimid, "animals");
								player.sendMessage(ChatColor.RED + "Animals flag removed!");
								return true;
							}

							// remove monsters flag
							if (args[1].equalsIgnoreCase("monsters")) {
								flags.removeFlag(claimid, "monsters");
								player.sendMessage(ChatColor.RED + "Monsters flag removed!");
								return true;
							}

							// remove pvp flag
							if (args[1].equalsIgnoreCase("pvp")) {
								flags.removeFlag(claimid, "pvp");
								player.sendMessage(ChatColor.RED + "PvP flag removed!");
								return true;
							}

							// remove entry message flag
							if (args[1].equalsIgnoreCase("entrymsg")) {
								flags.removeFlag(claimid, "entrymsg");
								player.sendMessage(ChatColor.RED + "Entry message removed!");
								return true;
							}

							// remove exit message flag
							if (args[1].equalsIgnoreCase("exitmsg")) {
								flags.removeFlag(claimid, "exitmsg");
								player.sendMessage(ChatColor.RED + "Entry message removed!");
								return true;
							}

							// remove charge flag
							if (args[1].equalsIgnoreCase("charge")) {
								flags.removeFlag(claimid, "charge");
								player.sendMessage(ChatColor.RED + "Charge flag removed!");
								return true;
							}

							// remove time flag
							if (args[1].equalsIgnoreCase("time")) {
								flags.removeFlag(claimid, "time");
								player.sendMessage(ChatColor.RED + "Time flag removed!");
								return true;
							}

							// remove trust flag
							if (args[1].equalsIgnoreCase("trust")) {
								flags.removeFlag(claimid, "trust");
								player.sendMessage(ChatColor.RED + "Trust flag removed!");
								return true;
							}

							// remove allow flag
							if (args[1].equalsIgnoreCase("allow")) {
								flags.removeFlag(claimid, "allow");
								player.sendMessage(ChatColor.RED + "Allow flag removed!");
								return true;
							}

							// remove deny flag
							if (args[1].equalsIgnoreCase("deny")) {
								flags.removeFlag(claimid, "deny");
								player.sendMessage(ChatColor.RED + "Deny flag removed!");
								return true;
							}

							// remove private flag
							if (args[1].equalsIgnoreCase("private")) {
								flags.removeFlag(claimid, "private");
								player.sendMessage(ChatColor.RED + "Private flag removed!");
								return true;
							}

							// remove box flag
							if (args[1].equalsIgnoreCase("box")) {
								flags.removeFlag(claimid, "box");
								player.sendMessage(ChatColor.RED + "Box flag removed!");
								return true;
							}

						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						}
					}

					/**
					 * Player listing flags in a claim
					 * 
					 * @usage /claimcontrol list <flag>
					 * @permission claimcontrol.list
					 */
					if (args[0].equalsIgnoreCase("list")) {
						if (player.hasPermission("claimcontrol.list")) {

							// check if player owns the claim they are in
							String owner = claim.getOwner(loc);
							if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.list.others")) {
								player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
								return true;
							}

							// validate flag
							if (!flags.valid(args[1])) {
								player.sendMessage(ChatColor.RED + "Valid flags: " + Flags.validFlags);
								return true;
							} else {

								String output = flags.list(loc, args[1]);
								player.sendMessage(output);

								return true;
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						}
					}

				} else if (args.length == 3) {
					// /claimcontrol args[0] args[1] args[2]

					/**
					 * Player adding a new flag
					 * 
					 * @usage /claimcontrol add <flag> <value>
					 * @permission claimcontrol.add
					 */
					if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("set")) {
						if (player.hasPermission("claimcontrol.add")) {

							// check if player owns the claim they are in
							String owner = claim.getOwner(loc);
							if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
								player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
								return true;
							}

							// validate flag
							if (!flags.valid(args[1])) {
								player.sendMessage(ChatColor.RED + "Valid flags: " + Flags.validFlags);
								return true;
							} else

							// there can be multiple allow and deny flags
							if (args[1].equalsIgnoreCase("allow") || args[1].equalsIgnoreCase("deny")) {

								// check if claim has the flag and value
								if (flags.hasFlag(claimid, args[1], args[2])) {
									player.sendMessage(ChatColor.RED + args[1] + " already has " + args[2] + "!");
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("allow") && player.hasPermission("claimcontrol.flags.allow")) {
									flags.setAllow(claimid, args[2]);
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								} else

								// deny someone from enterying a public claim
								if (args[1].equalsIgnoreCase("deny") && player.hasPermission("claimcontrol.flags.deny")) {
									flags.setDeny(claimid, args[2]);
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								}

							} else {

								// check if claim has the flag
								if (flags.hasFlag(claimid, args[1])) {
									player.sendMessage(ChatColor.RED + args[1] + " already has " + args[2] + "!");
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("animals") && player.hasPermission("claimcontrol.flags.animals")) {
									flags.setAnimals(claimid, "true");
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								}

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("monsters") && player.hasPermission("claimcontrol.flags.monsters")) {
									flags.setMonsters(claimid, "true");
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("pvp") && player.hasPermission("claimcontrol.flags.pvp")) {
									flags.setPvp(claimid, "true");
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("charge") && player.hasPermission("claimcontrol.flags.charge")) {
									flags.setCharge(claimid, args[2]);
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("time") && player.hasPermission("claimcontrol.flags.time")) {
									flags.setTime(claimid, args[2]);
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("entrymsg") && player.hasPermission("claimcontrol.flags.entrymsg")) {
									String entryMsg = StringUtils.join(args, " ", 2, args.length);
									flags.setEntryMsg(claimid, entryMsg);
									player.sendMessage(ChatColor.GREEN + "Entry message set to: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes("&".charAt(0), entryMsg));
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("exitmsg") && player.hasPermission("claimcontrol.flags.exitmsg")) {
									String exitMsg = StringUtils.join(args, " ", 2, args.length);
									flags.setExitMsg(claimid, exitMsg);
									player.sendMessage(ChatColor.GREEN + "Exit message set to: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes("&".charAt(0), exitMsg));
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("private") && player.hasPermission("claimcontrol.flags.private")) {
									flags.setPrivate(claimid, "true");
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								} else

								// allow someone into a private claim
								if (args[1].equalsIgnoreCase("box") && player.hasPermission("claimcontrol.flags.box")) {
									flags.setBox(claimid, "true");
									player.sendMessage(ChatColor.GREEN + args[2] + " added to " + args[1] + "!");
									return true;
								}
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						}
					}

					/**
					 * Player removing a flag
					 * 
					 * @usage /claimcontrol remove <flag> <value>
					 * @permission claimcontrol.flags.remove
					 */
					if (args[0].equalsIgnoreCase("remove")) {
						if (player.hasPermission("claimcontrol.remove")) {

							// check if player owns the claim they are in
							String owner = claim.getOwner(loc);
							if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
								player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
								return true;
							}

							// check if claim has the flag
							if (!flags.hasFlag(claimid, args[1], args[2])) {
								player.sendMessage(ChatColor.RED + args[2] + " does not exist in " + args[1]);
								return true;
							}

							flags.removeFlag(claimid, args[1], args[2]);

							player.sendMessage(ChatColor.GREEN + args[2] + " removed from " + args[1]);
							return true;

						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
							return true;
						}
					}

				}

				if (args.length >= 2) {
					// /claimcontrol args[0] args[1]...

					// set entry message flag
					if (args[0].equalsIgnoreCase("entrymsg") && player.hasPermission("claimcontrol.flags.entrymsg")) {

						// check if player owns the claim they are in
						String owner = claim.getOwner(loc);
						if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
							player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
							return true;
						}

						flags.removeFlag(claimid, "entrymsg");
						String entryMsg = StringUtils.join(args, " ", 1, args.length);
						flags.setEntryMsg(claimid, entryMsg);
						player.sendMessage(ChatColor.GREEN + "Entry message set to: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes("&".charAt(0), entryMsg));
						return true;
					}

					// set exit message flag
					if (args[0].equalsIgnoreCase("exitmsg") && player.hasPermission("claimcontrol.flags.exitmsg")) {

						// check if player owns the claim they are in
						String owner = claim.getOwner(loc);
						if (!owner.equalsIgnoreCase(player.getName()) && !player.hasPermission("claimcontrol.admin")) {
							player.sendMessage(ChatColor.RED + "The owner of this claim is " + owner + ".");
							return true;
						}

						flags.removeFlag(claimid, "exitmsg");
						String exitMsg = StringUtils.join(args, " ", 1, args.length);
						flags.setExitMsg(claimid, exitMsg);
						player.sendMessage(ChatColor.GREEN + "Exit message set to: " + ChatColor.RESET + ChatColor.translateAlternateColorCodes("&".charAt(0), exitMsg));
						return true;
					}

				}

			} else {
				// console commands
			}

		}

		sender.sendMessage(ChatColor.RED + "You have entered incorrect arguements! Type /claimcontrol for help.");
		return true;
	}
}