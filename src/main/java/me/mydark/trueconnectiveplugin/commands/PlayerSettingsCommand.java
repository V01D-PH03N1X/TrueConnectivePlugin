//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.commands;

import lombok.extern.slf4j.Slf4j;
import me.mydark.trueconnectiveplugin.gui.SettingsGui;
import me.mydark.trueconnectiveplugin.manager.DatabaseManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to open the Settings GUI.
 */
@Slf4j
public class PlayerSettingsCommand extends BukkitCommand {
    private final DatabaseManager databaseManager;

    /**
     * Constructor for the Settings command.
     * Sets the command name and initializes the logger.
     */
    public PlayerSettingsCommand(DatabaseManager databaseManager) {
        super("settings");
        this.databaseManager = databaseManager;
    }

    /**
     * Executes the settings command.
     * Opens a GUI for the player to interact with Settings.
     *
     * @param sender The sender of the command.
     * @param commandLabel The label of the command.
     * @param args The arguments passed to the command.
     * @return true if the command was successful, false otherwise.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            SettingsGui gui = new SettingsGui(databaseManager.getPlayerSettings(player), databaseManager);
            gui.open(player);
            return true;
        }
        log.error("This command can only be executed by a player!");
        return false;
    }
}
