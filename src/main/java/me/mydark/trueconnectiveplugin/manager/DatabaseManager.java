//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.manager;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import me.mydark.trueconnectiveplugin.TrueConnective;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

public class DatabaseManager {

    @Getter
    private Connection connection;

    private static Logger log;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructor for DatabaseManager.
     * Initializes the database and tables.
     *
     * @param instance The instance of the TrueConnective plugin.
     */
    public DatabaseManager(TrueConnective instance) {
        log = TrueConnective.getLog();

        // Initialize Database and Tables.
        initializeDatabase(instance);
    }

    /**
     * Initializes the database and tables.
     *
     * @param plugin The instance of the TrueConnective plugin.
     */
    private void initializeDatabase(TrueConnective plugin) {
        File pluginFolder = plugin.getDataFolder(); // this is the default plugin directory

        // Create directory if it is not existing
        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdirs()) {
                log.info("Creating Plugin directory: {}", pluginFolder.getAbsolutePath());
            } else {
                log.error("Couldn't create plugin directory: {}", pluginFolder.getAbsolutePath());
            }
        }
        // Connect to the database.
        connect();

        initializeTikTokTable();
        initializePlayerTimesTable();
    }

    /**
     * Establishes a connection to the SQLite database.
     */
    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/TrueConnective/trueconnective.db");
            log.info("Database connection established");
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Database connection failed: {}", e.getMessage());
        }
    }

    /*
     * Database TikTok Area
     */

    /**
     * Initializes the TikTokPlayers table if it doesn't exist.
     */
    private void initializeTikTokTable() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS TikTokPlayers (uuid TEXT PRIMARY KEY, username TEXT)");
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to initialize TikTok table: {}", e.getMessage());
        }
    }

    /**
     * Retrieves the TikTok username for a player.
     *
     * @param player The player whose TikTok username is to be retrieved.
     * @return The TikTok username of the player, or null if not found.
     */
    public String getTiktokUsername(Player player) {
        String uuid = player.getUniqueId().toString();
        String username = null;
        // Get TikTok Username from database using player's UUID. If not found, return null.
        try {
            PreparedStatement statement =
                    connection.prepareStatement("SELECT username FROM TikTokPlayers WHERE uuid = ?");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                username = resultSet.getString("username");
                log.info("Player {} has TikTok username {}", player.getName(), username);
                statement.close();
                return username;
            } else {
                log.info("Player {} has no TikTok username", player.getName());
                statement.close();
                return null;
            }
        } catch (SQLException e) {
            log.error("Failed to get TikTok username: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Sets the TikTok username for a player.
     *
     * @param player   The player whose TikTok username is to be set.
     * @param username The TikTok username to be set.
     */
    public void setTiktokUsername(OfflinePlayer player, String username) {
        String uuid = player.getUniqueId().toString();
        // Set TikTok Username in database using player's UUID.
        try {
            PreparedStatement statement =
                    connection.prepareStatement("INSERT OR REPLACE INTO TikTokPlayers (uuid, username) VALUES (?, ?)");
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.executeUpdate();
            statement.close();
            log.info("Player {} has set TikTok username {}", player.getName(), username);
        } catch (SQLException e) {
            log.error("Failed to set TikTok username: {}", e.getMessage());
        }
    }

    /*
     * Database PlayerTimes Area
     */

    /**
     * Initializes the PlayerTimes table if it doesn't exist.
     */
    private void initializePlayerTimesTable() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS PlayerTimes (uuid TEXT PRIMARY KEY, playtime INTEGER, last_login DATE)");
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to initialize PlayerTimes table: {}", e.getMessage());
        }
    }

    /**
     * Retrieves the playtime for a player.
     *
     * @param player The player whose playtime is to be retrieved.
     * @return The playtime of the player in minutes.
     */
    public int getPlaytime(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        int playtime = 0;
        try {
            PreparedStatement statement =
                    connection.prepareStatement("SELECT playtime FROM PlayerTimes WHERE uuid = ?");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                playtime = resultSet.getInt("playtime");
            }
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to get playtime: {}", e.getMessage());
        }
        return playtime;
    }

    /**
     * Updates the playtime for a player.
     *
     * @param player   The player whose playtime is to be updated.
     * @param playtime The new playtime to be set.
     */
    public void updatePlaytime(OfflinePlayer player, int playtime) {
        String uuid = player.getUniqueId().toString();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR REPLACE INTO PlayerTimes (uuid, playtime, last_login) VALUES (?, ?, ?)");
            statement.setString(1, uuid);
            statement.setInt(2, playtime);
            statement.setString(3, LocalDate.now().format(DATE_FORMATTER));
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to update playtime: {}", e.getMessage());
        }
    }

    /**
     * Resets the playtime for a player.
     *
     * @param player The player whose playtime is to be reset.
     */
    public void resetPlaytime(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        try {
            PreparedStatement statement =
                    connection.prepareStatement("UPDATE PlayerTimes SET playtime = 0, last_login = ? WHERE uuid = ?");
            statement.setString(1, LocalDate.now().format(DATE_FORMATTER));
            statement.setString(2, uuid);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to reset playtime: {}", e.getMessage());
        }
    }

    /**
     * Checks if it is a new day since the player's last login.
     *
     * @param player The player to check.
     * @return True if it is a new day, false otherwise.
     */
    public boolean isNewDay(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        boolean isNewDay = false;
        try {
            PreparedStatement statement =
                    connection.prepareStatement("SELECT last_login FROM PlayerTimes WHERE uuid = ?");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String lastLoginStr = resultSet.getString("last_login");
                try {
                    LocalDate lastLogin = LocalDate.parse(lastLoginStr, DATE_FORMATTER);
                    isNewDay = !lastLogin.equals(LocalDate.now());
                } catch (DateTimeParseException e) {
                    log.error("Error parsing last login date: {}", e.getMessage());
                    isNewDay = true; // Treat as new day if parsing fails
                }
            } else {
                isNewDay = true;
            }
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to check if new day: {}", e.getMessage());
        }
        return isNewDay;
    }
}
