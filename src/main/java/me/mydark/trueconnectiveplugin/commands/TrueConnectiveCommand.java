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

    /**
     * Constructor for the TrueConnectiveCommand.
     * Sets the command name and initializes the logger.
     */
    public TrueConnectiveCommand() {
        // Set the command name
        super("trueconnective");
        // get the logger from the main class
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
            Gui trueConnectiveMenu = Gui.gui()
                    .title(Component.text()
                            .content("TrueConnective Management")
                            .color(TextColor.color(0xAD14F5))
                            .decoration(TextDecoration.BOLD, true)
                            .build())
                    .rows(1)
                    .disableAllInteractions()
                    .create();

            GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .name(Component.text().content(" ").build())
                    .asGuiItem();

            GuiItem tcItem = ItemBuilder.from(Material.BLACK_CONCRETE)
                    .name(Component.text()
                            .content("Bewirb dich jetzt!")
                            .color(TextColor.color(0xef3535))
                            .decoration(TextDecoration.BOLD, true)
                            .build())
                    .lore(Component.text()
                            .content("Klicke hier um mehr über TrueConnective zu Erfahren!")
                            .color(TextColor.color(0xD0AEFF))
                            .build())
                    .asGuiItem(event -> {
                        webTrueConnective(player);
                        trueConnectiveMenu.close(player);
                    });

            // fill the menu with filler items and the true connective item in the middle
            for (int i = 0; i < 9; i++) {
                trueConnectiveMenu.setItem(i, i == 4 ? tcItem : filler);
            }

            trueConnectiveMenu.open(player);
            return true;
        }
        log.error("This command can only be executed by a player!");
        return false;
    }

    /**
     * Sends a message to the player with a clickable link to the TrueConnective website.
     *
     * @param player The player to send the message to.
     */
    private void webTrueConnective(Player player) {
        TextComponent webPage = Component.text()
                .content("Klicke hier um mehr über TrueConnective zu erfahren!")
                .color(TextColor.color(0x3F9EFF))
                .decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.openUrl("https://trueconnective.com"))
                .build();
        player.sendMessage(webPage);
    }
}
