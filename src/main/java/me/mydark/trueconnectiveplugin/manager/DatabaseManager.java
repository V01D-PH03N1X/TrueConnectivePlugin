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
import java.util.Optional;
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

    private static final String CREATE_TIKTOK_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS TikTokPlayers (uuid TEXT PRIMARY KEY, username TEXT)";
    private static final String CREATE_PLAYER_TIMES_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS PlayerTimes (uuid TEXT PRIMARY KEY, playtime INTEGER, last_login DATE)";
    private static final String SELECT_TIKTOK_USERNAME_SQL = "SELECT username FROM TikTokPlayers WHERE uuid = ?";
    private static final String INSERT_OR_REPLACE_TIKTOK_USERNAME_SQL =
            "INSERT OR REPLACE INTO TikTokPlayers (uuid, username) VALUES (?, ?)";
    private static final String SELECT_PLAYTIME_SQL = "SELECT playtime FROM PlayerTimes WHERE uuid = ?";
    private static final String INSERT_OR_REPLACE_PLAYTIME_SQL =
            "INSERT OR REPLACE INTO PlayerTimes (uuid, playtime, last_login) VALUES (?, ?, ?)";
    private static final String UPDATE_PLAYTIME_SQL =
            "UPDATE PlayerTimes SET playtime = 0, last_login = ? WHERE uuid = ?";
    private static final String SELECT_LAST_LOGIN_SQL = "SELECT last_login FROM PlayerTimes WHERE uuid = ?";

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
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TIKTOK_TABLE_SQL);
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
    public Optional<String> getTiktokUsername(Player player) {
        String uuid = player.getUniqueId().toString();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_TIKTOK_USERNAME_SQL)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    log.info("Player {} has TikTok username {}", player.getName(), username);
                    return Optional.of(username);
                } else {
                    log.info("Player {} has no TikTok username", player.getName());
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get TikTok username: {}", e.getMessage());
            return Optional.empty();
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
        try (PreparedStatement statement = connection.prepareStatement(INSERT_OR_REPLACE_TIKTOK_USERNAME_SQL)) {
            statement.setString(1, uuid);
            statement.setString(2, username);
            statement.executeUpdate();
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
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_PLAYER_TIMES_TABLE_SQL);
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
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PLAYTIME_SQL)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("playtime");
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get playtime: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Updates the playtime for a player.
     *
     * @param player   The player whose playtime is to be updated.
     * @param playtime The new playtime to be set.
     */
    public void updatePlaytime(OfflinePlayer player, int playtime) {
        String uuid = player.getUniqueId().toString();
        try (PreparedStatement statement = connection.prepareStatement(INSERT_OR_REPLACE_PLAYTIME_SQL)) {
            statement.setString(1, uuid);
            statement.setInt(2, playtime);
            statement.setString(3, LocalDate.now().format(DATE_FORMATTER));
            statement.executeUpdate();
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
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_PLAYTIME_SQL)) {
            statement.setString(1, LocalDate.now().format(DATE_FORMATTER));
            statement.setString(2, uuid);
            statement.executeUpdate();
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
        try (PreparedStatement statement = connection.prepareStatement(SELECT_LAST_LOGIN_SQL)) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String lastLoginStr = resultSet.getString("last_login");
                    try {
                        LocalDate lastLogin = LocalDate.parse(lastLoginStr, DATE_FORMATTER);
                        return !lastLogin.equals(LocalDate.now());
                    } catch (DateTimeParseException e) {
                        log.error("Error parsing last login date: {}", e.getMessage());
                        return true; // Treat as new day if parsing fails
                    }
                } else {
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to check if new day: {}", e.getMessage());
        }
        return false;
    }
}
