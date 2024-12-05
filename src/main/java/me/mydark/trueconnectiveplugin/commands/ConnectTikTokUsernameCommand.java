//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.commands;

import static org.bukkit.Bukkit.getServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.mydark.trueconnectiveplugin.TrueConnective;
import me.mydark.trueconnectiveplugin.manager.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class ConnectTikTokUsernameCommand extends BukkitCommand implements TabCompleter {
    private static Logger log;
    private static DatabaseManager databaseManager;
    // Constructor for the command
    public ConnectTikTokUsernameCommand(DatabaseManager dbmanager) {
        super("ttconect");
        databaseManager = dbmanager;
        // get the logger from the main class
        log = TrueConnective.getLog();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        // First argument is the target player and the second argument is the TikTok username
        log.info("Executing command ttconect with args: {}", (Object[]) args);
        if (args.length != 2) {
            sender.sendMessage("Usage: /ttconect <player> <tiktok_username>");
            return false;
        }

        if (sender instanceof Player player) {
            // Check if the player has permission to execute the command
            if (!player.hasPermission("trueconnective.ttconnect")) {
                player.sendMessage(Component.text()
                        .content("Du hast keine Berechtigung für diesen Befehl!")
                        .color(TextColor.color(0xef2121))
                        .decoration(TextDecoration.BOLD, true)
                        .build());
                return false;
            }
        }

        // Get the target player of all players existing on the server by the given name he doesn't must be online
        OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Component.text()
                    .content("Spieler " + args[0] + " konnte nicht gefunden werden!")
                    .color(TextColor.color(0xEF7573))
                    .decoration(TextDecoration.BOLD, true)
                    .build());
            return false;
        }

        // Validate the TikTok username and store it in the database
        String username = args[1];
        if (!username.matches("^[a-z0-9_]{1,32}$")) {
            sender.sendMessage(Component.text()
                    .content("Invalid TikTok username!")
                    .color(TextColor.color(0xEF7573))
                    .decoration(TextDecoration.BOLD, true)
                    .build());
            return false;
        }

        databaseManager.setTiktokUsername(target, username);
        sender.sendMessage(Component.text()
                .content("TikTok username")
                .color(TextColor.color(0x3F9EFF))
                .append(Component.text()
                        .content(" " + username + " ")
                        .color(TextColor.color(0x3F9EFF))
                        .decoration(TextDecoration.BOLD, true)
                        .build())
                .append(Component.text("successfully connected to player "))
                .append(Component.text()
                        .content(target.getName())
                        .color(TextColor.color(0x3F9EFF))
                        .decoration(TextDecoration.BOLD, true)
                        .build())
                .build());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Return all player names as tab completion even if they are offline
            return Arrays.stream(getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
