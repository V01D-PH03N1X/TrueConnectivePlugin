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

public final class TrueConnective extends JavaPlugin implements Listener {
    @Getter
    private static TrueConnective instance;

    @Getter
    private static Logger log;

    private DatabaseManager databaseManager;
    private TikTokManager tikTokManager;

    private Map<UUID, BukkitTask> playerTasks = new HashMap<>();

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

        // register Events
        getServer().getPluginManager().registerEvents(this, this);

        // register Commands
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("trueconnective", new TrueConnectiveCommand());
        commandMap.register("ttconect", new ConnectTikTokUsernameCommand(databaseManager));
        commandMap.register("playtime", new RemainingPlaytimeCommand(databaseManager, instance));
        commandMap.register("resetplaytime", new ResetPlaytimeCommand(databaseManager));
    }

    @Override
    public void onDisable() {
        // Execute on plugin disable
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (databaseManager.isNewDay(player)) {
            databaseManager.resetPlaytime(player);
        }
        // Check if player has the Permission "Creator"
        if (player.hasPermission("trueconnective.creator")) {
            String tiktokusername = databaseManager.getTiktokUsername(player);
            if (!tikTokManager.checkTikTokLive(tiktokusername)) {
                TextComponent kickMessage = Component.text()
                        .content("Du musst Live sein um den Server zu betreten!")
                        .color(TextColor.color(0xff0000))
                        .decoration(TextDecoration.BOLD, true)
                        .build();
                player.kick(kickMessage);
            }
        }
        // Check if player has the Permission "Viewer"
        if (player.hasPermission("trueconnective.viewer")) {
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
        }

        // Schedule a task to check playtime every minute
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> checkPlaytime(player), 0L, 1200L);
        playerTasks.put(player.getUniqueId(), task);
    }

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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        int playtime = databaseManager.getPlaytime(player);
        databaseManager.updatePlaytime(
                player, playtime + (int) ((System.currentTimeMillis() - player.getLastLogin()) / 60000));

        // Cancel the task when the player leaves
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
