//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.utils;

import java.io.File;
import java.sql.*;
import lombok.Getter;
import me.mydark.trueconnectiveplugin.TrueConnective;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

public class DatabaseManager {

    @Getter
    private Connection connection;

    private static Logger log;

    public DatabaseManager(TrueConnective instance) {
        log = TrueConnective.getLog();

        // Initialize Database and Tables.
        initializeDatabase(instance);
        initializeTikTokTable();
    }

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
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/TrueConnective/trueconnective.db");
            log.info("Database connection established");
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Database connection failed: {}", e.getMessage());
        }
    }

    private void initializeTikTokTable() {
        try {
            Statement statement = connection.createStatement();
            // Create TikTokPlayers table if not exists with columns uuid as primary key and TikTok Username.
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS TikTokPlayers (uuid TEXT PRIMARY KEY, username TEXT)");
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to initialize TikTok table: {}", e.getMessage());
        }
    }

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
}
