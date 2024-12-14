//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.commands;

import me.mydark.trueconnectiveplugin.manager.PlayTimeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AddPlaytimeCommand extends BukkitCommand {
    private final PlayTimeManager playTimeManager;

    public AddPlaytimeCommand(PlayTimeManager playTimeManager) {
        super("addplaytime");
        this.playTimeManager = playTimeManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /addplaytime <player> <seconds>");
            return false;
        }

        Player player = sender.getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage("Player not found.");
            return false;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid number of seconds.");
            return false;
        }

        playTimeManager.addPlaytime(player, seconds);
        sender.sendMessage("Added " + seconds + " seconds to " + player.getName() + "'s playtime.");
        return true;
    }
}
