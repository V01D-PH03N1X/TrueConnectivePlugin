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
import me.mydark.trueconnectiveplugin.commands.*;
import me.mydark.trueconnectiveplugin.dto.PlayerSettings;
import me.mydark.trueconnectiveplugin.manager.DatabaseManager;
import me.mydark.trueconnectiveplugin.manager.PlayTimeManager;
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

    @Getter
    private PlayTimeManager playTimeManager;

    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();

    @Getter
    private final Map<UUID, BukkitTask> actionBarTasks = new HashMap<>();

    @Getter
    private final Map<UUID, BukkitTask> bossBarTasks = new HashMap<>();

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
        playTimeManager = new PlayTimeManager(this, databaseManager);

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("trueconnective", "trueconnective", new TrueConnectiveCommand());
        commandMap.register("ttconect", "trueconnective", new ConnectTikTokUsernameCommand(databaseManager));
        commandMap.register("playtime", "trueconnective", new RemainingPlaytimeCommand(databaseManager, instance));
        commandMap.register("resetplaytime", "trueconnective", new ResetPlaytimeCommand(databaseManager));
        commandMap.register("settings", "trueconnective", new PlayerSettingsCommand(databaseManager));
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

        if (player.hasPermission("trueconnective.creator")) kickNotLiveCreator(player);

        sendInfoMessages(player);

        // Schedule a task to check playtime every minute
        BukkitTask playtimeCheck = Bukkit.getScheduler()
                .runTaskTimer(this, () -> playTimeManager.checkPlaytime(player), 0L, 1200L); // 1200L = 1 minute
        playerTasks.put(player.getUniqueId(), playtimeCheck);

        // Schedule tasks for action bar and boss bar if enabled in player settings
        PlayerSettings playerSettings = databaseManager.getPlayerSettings(player);
        String playerUUID = player.getUniqueId().toString();

        if (playerSettings.isActionbarEnabled()) {
            BukkitTask task = Bukkit.getScheduler()
                    .runTaskTimer(this, () -> playTimeManager.actionBarTask(player), 0L, 20L); // 20L = 1 second
            actionBarTasks.put(player.getUniqueId(), task);
        }

        if (playerSettings.isBossbarEnabled()) {
            BukkitTask task = Bukkit.getScheduler()
                    .runTaskTimer(this, () -> playTimeManager.playtimeBossbarTask(player), 0L, 20L); // 20L = 1 second
            bossBarTasks.put(player.getUniqueId(), task);
        }
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

        removePlayerTasks(player);
    }

    private void removeActionbarTask(Player target) {
        UUID playerUUID = target.getUniqueId();
        BukkitTask actionBarTask;

        if (actionBarTasks.containsKey(playerUUID)) {
            actionBarTask = actionBarTasks.remove(playerUUID);
            actionBarTask.cancel();
        }
    }

    private void removeBossbarTasks(Player target) {
        UUID playerUUID = target.getUniqueId();
        BukkitTask bossBarTask;

        if (bossBarTasks.containsKey(playerUUID)) {
            bossBarTask = bossBarTasks.remove(playerUUID);
            playTimeManager.removeBossBar(target);
            bossBarTask.cancel();
        }
    }

    private void removePlayerTasks(Player target) {
        UUID playerUUID = target.getUniqueId();
        if (actionBarTasks.containsKey(playerUUID)) {
            removeActionbarTask(target);
        }
        if (bossBarTasks.containsKey(playerUUID)) {
            removeBossbarTasks(target);
        }
        if (playerTasks.containsKey(playerUUID)) {
            BukkitTask playtimeCheck = playerTasks.remove(playerUUID);
            playtimeCheck.cancel();
        }
    }

    private void kickNotLiveCreator(Player target) {
        String tiktokusername = databaseManager.getTiktokUsername(target).orElse(null);
        if (!tikTokManager.checkTikTokLive(tiktokusername)) {
            TextComponent kickMessage = Component.text()
                    .content("Du musst Live sein um den Server zu betreten!")
                    .color(TextColor.color(0xff6969))
                    .decoration(TextDecoration.BOLD, true)
                    .build();
            target.kick(kickMessage);
        }
    }

    private void sendInfoMessages(Player target) {
        TextComponent welcomeMessageViewer = Component.text()
                .content("Willkommen auf dem Minecraft Server von")
                .color(TextColor.color(0xEFEFEF))
                .append(Component.text()
                        .content(" TrueConnective!")
                        .color(TextColor.color(0xEFEFEF))
                        .decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.openUrl("https://trueconnective.com")))
                .build();

        TextComponent infoMessageViewer = Component.text()
                .content("Du möchtest mehr über TrueConnective erfahren? Dann nutze /trueconnective \n")
                .color(TextColor.color(0xEFEFEF))
                .append(Component.text()
                        .content("Dann klicke hier!")
                        .color(TextColor.color(0xFF9E3f))
                        .decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.openUrl("https://trueconnective.com")))
                .build();

        target.sendMessage(welcomeMessageViewer);
        target.sendMessage(infoMessageViewer);
    }
}
