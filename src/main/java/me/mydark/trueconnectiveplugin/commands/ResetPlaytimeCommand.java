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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ResetPlaytimeCommand extends BukkitCommand implements TabCompleter {
    private static Logger log;
    private static DatabaseManager databaseManager;

    public ResetPlaytimeCommand(DatabaseManager dbmanager) {
        super("resetplaytime");
        log = TrueConnective.getLog();
        databaseManager = dbmanager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("trueconnective.playtime.reset")) {
                TextComponent noPermission = Component.text()
                        .content("Du hast keine Berechtigung für diesen Befehl!")
                        .color(TextColor.color(0xef2121))
                        .build();
                player.sendMessage(noPermission);
                return false;
            } else {
                if (args.length != 1) {
                    // Reset the playtime of the player
                    databaseManager.resetPlaytime(player);
                    TextComponent playtimeReset = Component.text()
                            .content("Deine Spielzeit wurde zurückgesetzt!")
                            .color(TextColor.color(0x21ef21))
                            .build();
                    player.sendMessage(playtimeReset);
                    return true;
                } else {
                    Player target = player.getServer().getPlayer(args[0]);
                    if (target == null) {
                        TextComponent playerNotFound = Component.text()
                                .content("Spieler " + args[0] + " konnte nicht gefunden werden!")
                                .color(TextColor.color(0xef2121))
                                .build();
                        player.sendMessage(playerNotFound);
                        return false;
                    } else {
                        // Reset the playtime of the target player
                        databaseManager.resetPlaytime(target);
                        TextComponent playtimeReset = Component.text()
                                .content("Spielzeit von " + target.getName() + " wurde zurückgesetzt!")
                                .color(TextColor.color(0x21ef21))
                                .build();
                        player.sendMessage(playtimeReset);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            // Return all player names as tab completion even if they are offline
            return Arrays.stream(getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
