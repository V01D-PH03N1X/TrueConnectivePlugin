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

/**
 * Command to get the remaining playtime of a player.
 */
public class RemainingPlaytimeCommand extends BukkitCommand implements TabCompleter {
    private static Logger log;
    private static DatabaseManager databaseManager;
    private static TrueConnective instance;

    /**
     * Constructor for RemainingPlaytimeCommand.
     *
     * @param dbmanager The DatabaseManager instance to interact with the database.
     * @param plugin The TrueConnective plugin instance.
     */
    public RemainingPlaytimeCommand(DatabaseManager dbmanager, TrueConnective plugin) {
        super("playtime");
        log = TrueConnective.getLog();
        databaseManager = dbmanager;
        instance = plugin;
    }

    /**
     * Executes the playtime command.
     *
     * @param sender The sender of the command.
     * @param commandLabel The label of the command.
     * @param args The arguments passed to the command.
     * @return true if the command was successful, false otherwise.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("trueconnective.playtime.get")) {
                TextComponent noPermission = Component.text()
                        .content("Du hast keine Berechtigung für diesen Befehl!")
                        .color(TextColor.color(0xef2121))
                        .build();
                player.sendMessage(noPermission);
                return false;
            } else {
                if (args.length != 1) {
                    // Get the remaining playtime of the player
                    if (player.hasPermission("trueconnective.creator")) {
                        int remainingPlaytime = (instance.getConfig().getInt("creator.max-playtime")
                                - databaseManager.getPlaytime(player));
                        TextComponent remainingPlaytimeMessage = Component.text()
                                .content("Du hast noch " + remainingPlaytime + " Minuten Spielzeit!")
                                .color(TextColor.color(0x21ef21))
                                .build();
                        player.sendMessage(remainingPlaytimeMessage);
                        return true;
                    } else {
                        int remainingPlaytime = (instance.getConfig().getInt("viewer.max-playtime")
                                - databaseManager.getPlaytime(player));
                        TextComponent remainingPlaytimeMessage = Component.text()
                                .content("Du hast noch " + remainingPlaytime + " Minuten Spielzeit!")
                                .color(TextColor.color(0x21ef21))
                                .build();
                        player.sendMessage(remainingPlaytimeMessage);
                        return true;
                    }
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
                        // Get the remaining playtime of the target player
                        if (target.hasPermission("trueconnective.creator")) {
                            int remainingPlaytime = (instance.getConfig().getInt("creator.max-playtime")
                                    - databaseManager.getPlaytime(player));
                            TextComponent remainingPlaytimeMessage = Component.text()
                                    .content("Spieler " + target.getName() + " hat noch " + remainingPlaytime
                                            + " Minuten Spielzeit!")
                                    .color(TextColor.color(0x21ef21))
                                    .build();
                            player.sendMessage(remainingPlaytimeMessage);
                            return true;
                        }
                        int remainingPlaytime = (instance.getConfig().getInt("viewer.max-playtime")
                                - databaseManager.getPlaytime(player));
                        TextComponent remainingPlaytimeMessage = Component.text()
                                .content("Spieler " + target.getName() + " hat noch " + remainingPlaytime
                                        + " Minuten Spielzeit!")
                                .color(TextColor.color(0x21ef21))
                                .build();
                        player.sendMessage(remainingPlaytimeMessage);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Provides tab completion for the playtime command.
     *
     * @param sender The sender of the command.
     * @param command The command being executed.
     * @param label The label of the command.
     * @param args The arguments passed to the command.
     * @return A list of possible completions for the final argument, or null to default to the command executor.
     */
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
