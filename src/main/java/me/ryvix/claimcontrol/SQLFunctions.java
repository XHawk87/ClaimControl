/**
 * ClaimControl - Provides more control over Grief Prevention claims.
 * Copyright (C) 2013 Ryan Rhode - rrhode@gmail.com
 *
 * The MIT License (MIT) - See LICENSE.txt
 *
 */
package me.ryvix.claimcontrol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.SQLite;

public class SQLFunctions {

	private static ClaimControl plugin;
	private static Database sql;

	public SQLFunctions(ClaimControl plugin, String prefix, String host, int port, String database, String username, String password) {
		SQLFunctions.plugin = plugin;
		sql = new MySQL(plugin.getLogger(), prefix, host, port, database, username, password);
		connect();
	}

	public SQLFunctions(ClaimControl plugin, String prefix, String absolutePath, String filename) {
		SQLFunctions.plugin = plugin;
		sql = new SQLite(plugin.getLogger(), prefix, absolutePath, filename);
		connect();
	}

	/**
	 * Connect to database
	 */
	public static void connect() {
		if (!sql.isOpen()) {
			sql.open();
		}
	}

	/**
	 * Close database connection
	 */
	public void close() {
		// sqlite isOpen causes error on disable so we do it separate
		if (plugin.dbType.equals("mysql")) {
			if (sql.isOpen()) {
				sql.close();
			}

		} else if (plugin.dbType.equals("sqlite")) {
			sql.close();
		}
	}

	/**
	 * Close a PreparedStatement
	 *
	 * @param statement
	 */
	private void close(PreparedStatement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());
			}
		}
	}

	/**
	 * Close a ResultSet
	 *
	 * @param results
	 */
	private void close(ResultSet results) {
		if (results != null) {
			try {
				results.close();
			} catch (SQLException e) {
				plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());
			}
		}
	}

	/**
	 * Create MySQL table
	 *
	 * @throws SQLException
	 */
	public void createTable() {

		// check for existing table
		if (!sql.isTable("flags")) {

			// create table
			String query = "";
			String dbType = plugin.config.getString("config.database");
			if (dbType.equalsIgnoreCase("mysql")) {
				query = "CREATE TABLE flags(id INT(15) AUTO_INCREMENT KEY, claimid INT(15), flag VARCHAR(20), value VARCHAR(100))";
			} else if (dbType.equalsIgnoreCase("sqlite")) {
				query = "CREATE TABLE flags(claimid INT(15), flag VARCHAR(20), value VARCHAR(100))";
			}

			PreparedStatement statement = null;

			try {
				statement = sql.prepare(query);
				sql.query(statement);

			} catch (SQLException e) {
				plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

			} finally {
				close(statement);
			}

		}
	}

	/**
	 * Insert flag into database
	 *
	 * @param claimid
	 * @param flag
	 * @param value
	 * @throws SQLException
	 */
	public void insert(Long claimid, String flag, String value) throws SQLException {
		PreparedStatement statement = null;

		try {

			String query = "INSERT INTO flags (claimid, flag, value) VALUES (?, ?, ?)";
			statement = sql.prepare(query);

			statement.setFloat(1, claimid);
			statement.setString(2, flag);
			statement.setString(3, value);
			sql.query(statement);

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

		} finally {
			close(statement);
		}
	}

	/**
	 * Update flag in database
	 *
	 * @param claimid
	 * @param flag
	 * @param value
	 * @throws SQLException
	 */
	public void update(int claimid, String flag, String value) throws SQLException {
		PreparedStatement statement = null;

		try {

			String query = "UPDATE flags SET value='?' WHERE claimid=? AND flag='?'";
			statement = sql.prepare(query);
			statement.setString(1, value);
			statement.setInt(2, claimid);
			statement.setString(3, flag);
			sql.query(statement);

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

		} finally {
			close(statement);
		}
	}

	/**
	 * Delete flag from database
	 *
	 * @param claimid
	 * @param flag
	 * @throws SQLException
	 */
	public void delete(Long claimid, String flag) throws SQLException {
		PreparedStatement statement = null;

		try {

			String query = "DELETE FROM flags WHERE claimid=? AND flag=?";
			statement = sql.prepare(query);
			statement.setFloat(1, claimid);
			statement.setString(2, flag);
			sql.query(statement);

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

		} finally {
			close(statement);
		}
	}

	/**
	 * Delete flag from database by player
	 *
	 * @param claimid
	 * @param flag
	 * @throws SQLException
	 */
	public void delete(Long claimid, String flag, String value) throws SQLException {
		PreparedStatement statement = null;

		try {

			String query = "DELETE FROM flags WHERE claimid=? AND flag=? AND value=?";
			statement = sql.prepare(query);
			statement.setFloat(1, claimid);
			statement.setString(2, flag);
			statement.setString(3, value);
			sql.query(statement);

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

		} finally {
			close(statement);
		}
	}

	/**
	 * Select all flags a claim has
	 *
	 * @param long1
	 * @return List<String>
	 * @throws SQLException
	 */
	public List<String> select(Long claimid) throws SQLException {
		PreparedStatement statement = null;
		ResultSet results = null;

		try {

			String query = "SELECT flag FROM flags WHERE claimid=?";
			statement = sql.prepare(query);
			statement.setLong(1, claimid);

			results = sql.query(statement);

			List<String> flags = new ArrayList<String>();
			while (results.next()) {
				flags.add(results.getString("flag"));
			}

			return flags;

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

		} finally {
			close(statement);
			close(results);
		}

		return null;
	}

	/**
	 * Select all values from a flag
	 *
	 * @param long1
	 * @param flag
	 * @return List<String>
	 * @throws SQLException
	 */
	public List<String> select(Long claimid, String flag) throws SQLException {
		PreparedStatement statement = null;
		ResultSet results = null;

		try {

			String query = "SELECT value FROM flags WHERE claimid=? AND flag=?";
			statement = sql.prepare(query);
			statement.setFloat(1, claimid);
			statement.setString(2, flag);

			results = sql.query(statement);

			List<String> flags = new ArrayList<String>();
			while (results.next()) {
				flags.add(results.getString("value"));
			}

			return flags;

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

		} finally {
			close(statement);
			close(results);
		}

		return null;
	}

	/**
	 * Select one flag a claim has - with player
	 *
	 * @param long1
	 * @param flag
	 * @return List<String>
	 * @throws SQLException
	 */
	public List<String> select(Long claimid, String flag, String player) throws SQLException {
		PreparedStatement statement = null;
		ResultSet results = null;

		try {

			String query = "SELECT value FROM flags WHERE claimid=? AND flag=? AND value=?";
			statement = sql.prepare(query);
			statement.setFloat(1, claimid);
			statement.setString(2, flag);
			statement.setString(3, player);

			results = sql.query(statement);

			List<String> flags = new ArrayList<String>();
			while (results.next()) {
				flags.add(results.getString("value"));
			}

			return flags;

		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "SQL Error: {0}", e.getMessage());

		} finally {
			close(statement);
			close(results);
		}

		return null;
	}
}
