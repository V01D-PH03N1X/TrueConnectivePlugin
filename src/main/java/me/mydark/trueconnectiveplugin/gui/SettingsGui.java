//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import java.util.UUID;
import me.mydark.trueconnectiveplugin.TrueConnective;
import me.mydark.trueconnectiveplugin.dto.PlayerSettings;
import me.mydark.trueconnectiveplugin.manager.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class SettingsGui {
    private static final TrueConnective plugin = TrueConnective.getInstance();
    private final PlayerSettings playerSettings;
    private final DatabaseManager databaseManager;
    private static Gui gui;

    public SettingsGui(PlayerSettings playerSettings, DatabaseManager databaseManager) {
        this.playerSettings = playerSettings;
        this.databaseManager = databaseManager;

        TextComponent title = Component.text()
                .content("Einstellungen")
                .color(TextColor.color(0xFF6969))
                .decoration(TextDecoration.BOLD, true)
                .build();

        gui = Gui.gui().title(title).rows(1).disableAllInteractions().create();

        TextComponent actionbarName = Component.text()
                .content("Actionbar")
                .color(TextColor.color(0x3F9EFF))
                .decoration(TextDecoration.BOLD, true)
                .build();
        TextComponent actionbarLore = Component.text()
                .content("Klicke um Actionbar zu aktivieren/deaktivieren!")
                .color(TextColor.color(0xdfdfdf))
                .build();
        GuiItem actionbar = ItemBuilder.from(Material.FEATHER)
                .name(actionbarName)
                .lore(actionbarLore)
                .asGuiItem(event -> {
                    toggleActionbar((Player) event.getWhoClicked());
                });

        TextComponent bossbarName = Component.text()
                .content("Bossbar")
                .color(TextColor.color(0x3F9EFF))
                .decoration(TextDecoration.BOLD, true)
                .build();
        TextComponent bossbarLore = Component.text()
                .content("Klicke um Bossbar zu aktivieren/deaktivieren!")
                .color(TextColor.color(0xdfdfdf))
                .build();
        GuiItem bossbar = ItemBuilder.from(Material.WITHER_SKELETON_SKULL)
                .name(bossbarName)
                .lore(bossbarLore)
                .asGuiItem(event -> {
                    toggleBossbar((Player) event.getWhoClicked());
                });

        gui.setItem(3, actionbar);
        gui.setItem(5, bossbar);

        GuiItem filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                .name(Component.text().content(" ").build())
                .asGuiItem();
        gui.getFiller().fill(filler);
        gui.setCloseGuiAction(event -> {
            TextComponent closeMessage = Component.text()
                    .content("Einstellungen geschlossen!")
                    .color(TextColor.color(0x3F9EFF))
                    .decoration(TextDecoration.BOLD, true)
                    .build();
            TextComponent actionbarMessage = Component.text()
                    .content("Actionbar: ")
                    .color(TextColor.color(0xDFDFDF))
                    .append(Component.text(playerSettings.isActionbarEnabled() ? "aktiviert" : "deaktiviert")
                            .color(
                                    playerSettings.isActionbarEnabled()
                                            ? TextColor.color(0x69FF69)
                                            : TextColor.color(0xFF6969)))
                    .build();
            TextComponent bossbarMessage = Component.text()
                    .content("Bossbar: ")
                    .color(TextColor.color(0xDFDFDF))
                    .append(Component.text(playerSettings.isBossbarEnabled() ? "aktiviert" : "deaktiviert")
                            .color(
                                    playerSettings.isBossbarEnabled()
                                            ? TextColor.color(0x69FF69)
                                            : TextColor.color(0xFF6969)))
                    .build();

            event.getPlayer().sendMessage(closeMessage);
            event.getPlayer().sendMessage(actionbarMessage);
            event.getPlayer().sendMessage(bossbarMessage);
        });
    }

    public void open(Player target) {
        gui.open(target);
    }

    private void toggleActionbar(Player target) {
        playerSettings.setActionbarEnabled(target, databaseManager, !playerSettings.isActionbarEnabled());
        UUID playerUUID = target.getUniqueId();

        // Remove existing task if present
        if (plugin.getActionBarTasks().containsKey(playerUUID)) {
            BukkitTask task = plugin.getActionBarTasks().remove(playerUUID);
            task.cancel();
        }

        // Add new task if enabled
        if (playerSettings.isActionbarEnabled()) {
            BukkitTask task = plugin.getServer()
                    .getScheduler()
                    .runTaskTimer(
                            plugin, () -> plugin.getPlayTimeManager().actionBarTask(target), 0L, 20L); // 20L = 1 second
            plugin.getActionBarTasks().put(playerUUID, task);
        }
    }

    private void toggleBossbar(Player target) {
        playerSettings.setBossbarEnabled(target, databaseManager, !playerSettings.isBossbarEnabled());
        UUID playerUUID = target.getUniqueId();

        // Remove existing task if present
        if (plugin.getBossBarTasks().containsKey(playerUUID)) {
            BukkitTask task = plugin.getBossBarTasks().remove(playerUUID);
            task.cancel();
            plugin.getPlayTimeManager().removeBossBar(target);
        }

        // Add new task if enabled
        if (playerSettings.isBossbarEnabled()) {
            BukkitTask task = plugin.getServer()
                    .getScheduler()
                    .runTaskTimer(
                            plugin,
                            () -> plugin.getPlayTimeManager().playtimeBossbarTask(target),
                            0L,
                            20L); // 20L = 1 second
            plugin.getBossBarTasks().put(playerUUID, task);
        }
    }
}
