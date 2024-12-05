/*******************************************************************************
 * Copyright (c) 2023 MyDarkMe / V01D-PH03N1X. All Rights reserved.
 ******************************************************************************/
package me.mydark.trueconnectiveplugin;

import me.mydark.trueconnectiveplugin.commands.TrueConnectiveCommand;
import me.mydark.trueconnectiveplugin.utils.DatabaseManager;
import me.mydark.trueconnectiveplugin.utils.TikTokManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public final class TrueConnectivePlugin extends JavaPlugin implements Listener {
    public static Logger log;
    private DatabaseManager databaseManager;
    private TikTokManager tikTokManager;

    @Override
    public void onEnable() {
        log = getSLF4JLogger();
        log.info("Logger Successfully Initialized");

        databaseManager = new DatabaseManager();
        tikTokManager = new TikTokManager();

        // register Commands
        CommandMap commandMap = Bukkit.getCommandMap();
        commandMap.register("trueconnective", new TrueConnectiveCommand());
    }

    @Override
    public void onDisable() {
        // Execute on plugin disable
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Send player welcome message from our plugin to the joined player.
        final TextComponent welcomeMessage = Component.text()
                .content("Willkommen auf dem ")
                .color(TextColor.color(0xefefef))
                .append(Component.text()
                        .content("TrueConnective ")
                        .decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.openUrl("https://trueconnective.com")))
                .append(Component.text("Server!").decoration(TextDecoration.BOLD, false))
                .build();

        event.getPlayer().sendMessage(welcomeMessage);

        // Check if player has the Permission Group "Creator"
        if (event.getPlayer().hasPermission("trueconnective.creator")) {
            String tiktokusername = databaseManager.getTiktokUsername(event.getPlayer());
            if (!tikTokManager.checkTikTokLive(tiktokusername)) {
                TextComponent kickMessage = Component.text()
                        .content("Du musst Live sein um den Server zu betreten!")
                        .color(TextColor.color(0xff0000))
                        .decoration(TextDecoration.BOLD, true)
                        .build();
                event.getPlayer().kick(kickMessage);
            }
        }
    }
}
