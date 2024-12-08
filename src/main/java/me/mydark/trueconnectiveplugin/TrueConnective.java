//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import me.mydark.trueconnectiveplugin.commands.ConnectTikTokUsernameCommand;
import me.mydark.trueconnectiveplugin.commands.RemainingPlaytimeCommand;
import me.mydark.trueconnectiveplugin.commands.ResetPlaytimeCommand;
import me.mydark.trueconnectiveplugin.commands.TrueConnectiveCommand;
import me.mydark.trueconnectiveplugin.manager.DatabaseManager;
import me.mydark.trueconnectiveplugin.manager.TikTokManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

/**
 * Main class for the TrueConnective plugin.
 * This class handles the initialization and management of the plugin.
 */
public final class TrueConnective extends JavaPlugin implements Listener {
    @Getter
    private static TrueConnective instance;

    @Getter
    private static Logger log;

    private DatabaseManager databaseManager;
    private TikTokManager tikTokManager;

    private Map<UUID, BukkitTask> playerTasks = new HashMap<>();
    private Map<UUID, BukkitTask> actionBarTasks = new HashMap<>();

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, sets up the database, registers events and commands.
     */
    @Override
    public void onEnable() {
        synchronized (this) {
            instance = this;
        }

        log = getSLF4JLogger();
        log.info("Logger Successfully Initialized");

        // Ignoring mkdir return value because it is not needed
        getDataFolder().mkdir();
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        tikTokManager = new TikTokManager();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("trueconnective", new TrueConnectiveCommand());
        commandMap.register("ttconect", new ConnectTikTokUsernameCommand(databaseManager));
        commandMap.register("playtime", new RemainingPlaytimeCommand(databaseManager, instance));
        commandMap.register("resetplaytime", new ResetPlaytimeCommand(databaseManager));
    }

    /**
     * Called when the plugin is disabled.
     * Executes any necessary cleanup.
     */
    @Override
    public void onDisable() {
        // Execute on plugin disable
    }

    /**
     * Event handler for player join events.
     * Resets playtime if it's a new day and checks if the player is live on TikTok.
     *
     * @param event The player join event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (databaseManager.isNewDay(player)) {
            databaseManager.resetPlaytime(player);
        }
        // Check if player has the Permission "Creator"
        if (player.hasPermission("trueconnective.creator")) {
            String tiktokusername = databaseManager.getTiktokUsername(player).orElse(null);
            if (!tikTokManager.checkTikTokLive(tiktokusername)) {
                TextComponent kickMessage = Component.text()
                        .content("Du musst Live sein um den Server zu betreten!")
                        .color(TextColor.color(0xff0000))
                        .decoration(TextDecoration.BOLD, true)
                        .build();
                player.kick(kickMessage);
            }
        }

        TextComponent welcomeMessageViewer = Component.text()
                .content("Willkommen auf dem Minecraft Server von")
                .color(TextColor.color(0x3F9EFF))
                .decoration(TextDecoration.BOLD, true)
                .append(Component.text()
                        .content(" TrueConnective ")
                        .color(TextColor.color(0xEFEFEF))
                        .decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.openUrl("https://trueconnective.com")))
                .build();

        TextComponent infoMessageViewer = Component.text()
                .content("Du möchtest mehr über TrueConnective erfahren? ")
                .color(TextColor.color(0x3F9EFF))
                .append(Component.text()
                        .content("Klicke hier!")
                        .color(TextColor.color(0xFF9E3f))
                        .decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.openUrl("https://trueconnective.com")))
                .build();

        player.sendMessage(welcomeMessageViewer);
        player.sendMessage(infoMessageViewer);

        // Schedule a task to check playtime every minute
        BukkitTask playtimeCheck =
                Bukkit.getScheduler().runTaskTimer(this, () -> checkPlaytime(player), 0L, 1200L); // 1200L = 1 minute
        playerTasks.put(player.getUniqueId(), playtimeCheck);

        // Schedule a task to update the action bar every second
        BukkitTask task =
                Bukkit.getScheduler().runTaskTimer(this, () -> actionBarTask(player), 0L, 20L); // 20L = 1 second
        actionBarTasks.put(player.getUniqueId(), task);
    }

    /**
     * Event handler for player quit events.
     * Updates the player's playtime and cancels any scheduled tasks.
     *
     * @param event The player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Calculate playtime based on the last login
        long lastLogin = player.getLastLogin();
        long currentTime = System.currentTimeMillis();
        int playtime = databaseManager.getPlaytime(player);
        int additionalPlaytime = (int) ((currentTime - lastLogin) / 60000); // Minutes

        // Update playtime in the database
        databaseManager.updatePlaytime(player, playtime + additionalPlaytime);

        // Cancel scheduled tasks when the player leaves the server
        BukkitTask playTimeCheck = playerTasks.remove(playerUUID);
        BukkitTask actionBarTask = actionBarTasks.remove(playerUUID);

        if (playTimeCheck != null) {
            playTimeCheck.cancel();
        }
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
    }

    /**
     * Checks the player's playtime and kicks them if they exceed the daily limit.
     *
     * @param player The player to check.
     */
    private void checkPlaytime(Player player) {
        int playtime = databaseManager.getPlaytime(player);
        if (player.hasPermission("trueconnective.creator")) {
            if (playtime >= getConfig().getInt("creator.max-playtime")) {
                TextComponent kickMessage = Component.text()
                        .content("Du hast dein tägliches Spielzeitlimit erreicht! Du kannst morgen wieder spielen.")
                        .color(TextColor.color(0xff0000))
                        .decoration(TextDecoration.BOLD, true)
                        .build();
                player.kick(kickMessage);
            } else {
                databaseManager.updatePlaytime(player, playtime + 1);
            }
        } else {
            if (playtime >= getConfig().getInt("viewer.max-playtime")) {
                TextComponent kickMessage = Component.text()
                        .content("Du hast dein tägliches Spielzeitlimit erreicht! Du kannst morgen wieder spielen.")
                        .color(TextColor.color(0xff0000))
                        .decoration(TextDecoration.BOLD, true)
                        .build();
                player.kick(kickMessage);
            } else {
                databaseManager.updatePlaytime(player, playtime + 1);
            }
        }
    }

    /**
     * Updates the action bar with the remaining playtime for the player.
     *
     * @param player The player to update.
     */
    private void actionBarTask(Player player) {
        int playtime = databaseManager.getPlaytime(player);
        if (player.hasPermission("trueconnective.creator")) {
            player.sendActionBar(formatRemainingTime(getConfig().getInt("creator.max-playtime") - playtime));
        } else {
            player.sendActionBar(formatRemainingTime(getConfig().getInt("viewer.max-playtime") - playtime));
        }
    }

    /**
     * Formats the remaining playtime as a text component.
     *
     * @param minutes The remaining playtime in minutes.
     * @return The formatted text component.
     */
    private TextComponent formatRemainingTime(int minutes) {
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return Component.text()
                .content("Verbleibende Spielzeit: ")
                .color(TextColor.color(0x3F9EFF))
                .append(Component.text()
                        .content(hours + " : " + remainingMinutes)
                        .color(TextColor.color(0x1f5Eff))
                        .decoration(TextDecoration.BOLD, true))
                .build();
    }
}
