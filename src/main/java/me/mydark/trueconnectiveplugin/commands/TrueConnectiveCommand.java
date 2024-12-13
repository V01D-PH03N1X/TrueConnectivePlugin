//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.commands;

import lombok.extern.slf4j.Slf4j;
import me.mydark.trueconnectiveplugin.gui.TrueConnectiveGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to open the TrueConnective management GUI.
 */
@Slf4j
public class TrueConnectiveCommand extends BukkitCommand {
    private static final TrueConnectiveGui gui = new TrueConnectiveGui();

    /**
     * Constructor for the TrueConnectiveCommand.
     * Sets the command name and initializes the logger.
     */
    public TrueConnectiveCommand() {
        super("trueconnective");
    }

    /**
     * Executes the trueconnective command.
     * Opens a GUI for the player to interact with TrueConnective management.
     *
     * @param sender The sender of the command.
     * @param commandLabel The label of the command.
     * @param args The arguments passed to the command.
     * @return true if the command was successful, false otherwise.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (player.hasPermission("trueconnective.gui")) {
                gui.open(player);
                return true;
            }
            TextComponent message = Component.text()
                    .content("Du hast keine Berechtigung, um diesen Befehl auszuführen!")
                    .color(TextColor.color(0xff6969))
                    .build();

            player.sendMessage(message);
            return false;
        }
        log.error("This command can only be executed by a player!");
        return false;
    }
}
