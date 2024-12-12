package me.mydark.trueconnectiveplugin.gui;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TrueConnectiveGui {
    private final Gui trueConnectiveMenu;

    /**
     * Constructor for the TrueConnectiveGui.
     * Initializes the GUI.
     */
    public TrueConnectiveGui() {

        trueConnectiveMenu = Gui.gui()
                .title(Component.text()
                        .content("TrueConnective Management")
                        .color(TextColor.color(0xAD14F5))
                        .decoration(TextDecoration.BOLD, true)
                        .build())
                .rows(1)
                .disableAllInteractions()
                .create();

        GuiItem fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                .name(Component.text().content(" ").build())
                .asGuiItem();

        GuiItem tcItem = ItemBuilder.from(Material.BLACK_CONCRETE)
                .name(Component.text()
                        .content("Erfahre mehr oder bewerbe dich jetzt!")
                        .color(TextColor.color(0xef3535))
                        .decoration(TextDecoration.BOLD, true)
                        .build())
                .asGuiItem(event -> {
                    webTrueConnective((Player) event.getWhoClicked());
                    trueConnectiveMenu.close((Player) event.getWhoClicked());
                });

        trueConnectiveMenu.getFiller().fill(fillerItem);
        trueConnectiveMenu.setItem(3, tcItem);
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

    /**
     * Opens the TrueConnective management GUI for the player.
     */
    public void open(Player player) {
        trueConnectiveMenu.open(player);
    }
}
