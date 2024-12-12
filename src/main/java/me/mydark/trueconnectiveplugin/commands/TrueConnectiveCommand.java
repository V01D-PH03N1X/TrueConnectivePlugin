//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.commands;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.mydark.trueconnectiveplugin.TrueConnective;
import me.mydark.trueconnectiveplugin.gui.TrueConnectiveGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Command to open the TrueConnective management GUI.
 */
public class TrueConnectiveCommand extends BukkitCommand {
    private static Logger log;
    private static final TrueConnectiveGui gui = new TrueConnectiveGui();


    /**
     * Constructor for the TrueConnectiveCommand.
     * Sets the command name and initializes the logger.
     */
    public TrueConnectiveCommand() {
        super("trueconnective");
        log = TrueConnective.getLog();
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
            gui.open(player);
            return true;
        }
        log.error("This command can only be executed by a player!");
        return false;
    }


}
