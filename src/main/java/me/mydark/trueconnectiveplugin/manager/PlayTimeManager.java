//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.mydark.trueconnectiveplugin.TrueConnective;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

public class PlayTimeManager {
    private static Logger log;
    private static DatabaseManager databaseManager;
    private static TrueConnective instance;

    private final Map<UUID, BossBar> playerBossBars = new HashMap<>();

    public PlayTimeManager(TrueConnective plugin, DatabaseManager dbmanager) {
        log = TrueConnective.getLog();
        databaseManager = dbmanager;
        instance = plugin;
    }

    /**
     * Checks the player's playtime and kicks them if they exceed the daily limit.
     *
     * @param player The player to check.
     */
    public void checkPlaytime(Player player) {
        int playtime = databaseManager.getPlaytime(player);
        if (player.hasPermission("trueconnective.playtime.bypass")) {
            databaseManager.updatePlaytime(player, playtime + 1);
            return;
        }
        if (player.hasPermission("trueconnective.creator")) {
            if (playtime >= instance.getConfig().getInt("creator.max-playtime")) {
                TextComponent kickMessage = Component.text()
                        .content("Du hast dein tägliches Spielzeitlimit erreicht! \nDu kannst morgen wieder spielen.")
                        .color(TextColor.color(0xff6969))
                        .decoration(TextDecoration.BOLD, true)
                        .build();
                player.kick(kickMessage);
            } else {
                databaseManager.updatePlaytime(player, playtime + 1);
            }
        } else {
            if (playtime >= instance.getConfig().getInt("viewer.max-playtime")) {
                TextComponent kickMessage = Component.text()
                        .content("Du hast dein tägliches Spielzeitlimit erreicht! \nDu kannst morgen wieder spielen.")
                        .color(TextColor.color(0xff6969))
                        .decoration(TextDecoration.BOLD, true)
                        .build();
                player.kick(kickMessage);
            } else {
                databaseManager.updatePlaytime(player, playtime + 1);
            }
        }
    }

    /**
     * Updates the action bar with the remaining playtime for the player.
     *
     * @param player The player to update.
     */
    public void actionBarTask(Player player) {
        int playtime = databaseManager.getPlaytime(player);
        if (player.hasPermission("trueconnective.playtime.bypass")) {
            player.sendActionBar(Component.text()
                    .content("Du hast unbegrenzte Spielzeit!")
                    .color(TextColor.color(0xff6969))
                    .decoration(TextDecoration.BOLD, true)
                    .build());
            return;
        }
        if (player.hasPermission("trueconnective.creator")) {
            player.sendActionBar(formatRemainingTime(instance.getConfig().getInt("creator.max-playtime") - playtime));
        } else {
            player.sendActionBar(formatRemainingTime(instance.getConfig().getInt("viewer.max-playtime") - playtime));
        }
    }

    /**
     * Formats the remaining playtime as a text component.
     *
     * @param minutes The remaining playtime in minutes.
     * @return The formatted text component.
     */
    private TextComponent formatRemainingTime(int minutes) {
        // Calculate remaining ours and minutes
        // int hours = minutes / 60;
        // int remainingMinutes = minutes % 60;

        return Component.text()
                .content("Verbleibende Spielzeit: ")
                .color(TextColor.color(0xDFDFDF))
                .decoration(TextDecoration.BOLD, true)
                .append(Component.text().content(String.valueOf(minutes)).color(TextColor.color(0xEFEFEF)))
                .build();
    }

    /**
     * The Task for the scheduled update of the Bossbar.
     * @param player the player which should see the BossBar.
     */
    public void playtimeBossbarTask(Player player) {
        UUID playerUUID = player.getUniqueId();
        int playtime = databaseManager.getPlaytime(player);
        int maxPlaytime;

        BossBar playtimeBossBar;
        if (player.hasPermission("trueconnective.playtime.bypass")) {
            if (playerBossBars.get(playerUUID) != null) {
                updateBossBar(
                        playerBossBars.get(playerUUID),
                        Component.text()
                                .content("Du hast unbegrenzte Spielzeit!")
                                .color(TextColor.color(0xff6969))
                                .decoration(TextDecoration.BOLD, true)
                                .build(),
                        (float) 0.01);
            } else {
                playtimeBossBar = formatPlaytimeBossBar(
                        Component.text()
                                .content("Du hast unbegrenzte Spielzeit!")
                                .color(TextColor.color(0xff6969))
                                .decoration(TextDecoration.BOLD, true)
                                .build(),
                        (float) 0.01);
                playerBossBars.put(playerUUID, playtimeBossBar);
                player.showBossBar(playtimeBossBar);
            }
            return;
        }
        if (player.hasPermission("trueconnective.creator")) {
            maxPlaytime = instance.getConfig().getInt("creator.max-playtime");
            int remainingPlaytime = maxPlaytime - playtime;
            float progress = getPercentage(maxPlaytime, remainingPlaytime);

            if (playerBossBars.get(playerUUID) != null) {
                updateBossBar(playerBossBars.get(playerUUID), formatRemainingTime(remainingPlaytime), progress);
            } else {
                playtimeBossBar = formatPlaytimeBossBar(formatRemainingTime(remainingPlaytime), progress);
                playerBossBars.put(playerUUID, playtimeBossBar);
                player.showBossBar(playtimeBossBar);
            }
        } else {
            maxPlaytime = instance.getConfig().getInt("viewer.max-playtime");
            int remainingPlaytime = maxPlaytime - playtime;
            float progress = getPercentage(maxPlaytime, remainingPlaytime);

            if (playerBossBars.get(playerUUID) != null) {
                updateBossBar(playerBossBars.get(playerUUID), formatRemainingTime(remainingPlaytime), progress);
            } else {
                playtimeBossBar = formatPlaytimeBossBar(formatRemainingTime(remainingPlaytime), progress);
                playerBossBars.put(playerUUID, playtimeBossBar);
                player.showBossBar(playtimeBossBar);
            }
        }
    }

    /**
     * This Formats the BossBar to be a "default" PlayTimeBossBar.
     * @param title The title of the BossBar
     * @param progress the Progress of the Playtime
     * @return formatted BossBar Object.
     */
    private BossBar formatPlaytimeBossBar(TextComponent title, float progress) {
        BossBar playtimeBossBar;

        if (progress < 0.33)
            playtimeBossBar = BossBar.bossBar(title, progress, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        else if (progress > 0.33 && progress < 0.66)
            playtimeBossBar = BossBar.bossBar(title, progress, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        else playtimeBossBar = BossBar.bossBar(title, progress, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);

        return playtimeBossBar;
    }

    /**
     * Updates a existing PlayTimeBossBar
     * @param playerBossBar the existing PlayTimeBossBar Object.
     * @param title the new title of the Bossbar. (needed for the TextUpdate of the Playtime)
     * @param progress the new Progress of the Playtime.
     */
    private void updateBossBar(BossBar playerBossBar, TextComponent title, float progress) {
        playerBossBar.progress(progress);
        playerBossBar.name(title);
        if (progress == 0) {
            playerBossBar.name(Component.text()
                    .content("Du wirst in wenigen Sekunden gekickt!")
                    .color(TextColor.color(0xff6969)));
            return;
        }
        if (progress < 0.33) playerBossBar.color(BossBar.Color.RED);
        if (progress > 0.33 && progress < 0.66) playerBossBar.color(BossBar.Color.YELLOW);
        if (progress > 0.66) playerBossBar.color(BossBar.Color.GREEN);
    }

    public void removeBossBar(Player player) {
        player.hideBossBar(playerBossBars.get(player.getUniqueId()));
        playerBossBars.remove(player.getUniqueId());
    }

    /**
     * Calculates the Percentage of the value in the range 0-1 (1 = 100%)
     * @param max The maximal value (100%)
     * @param value The actual value that you want to be calculated
     * @return The percentage as float value between 0 and 1
     */
    private float getPercentage(int max, int value) {
        if (value == 0) {
            log.warn("Playtime value is zero, returning 0 because the playtime is over");
            return 0;
        }
        if (max == 0) {
            log.warn("Max value is zero, returning 0 because of division by zero");
            return 1;
        }
        return ((float) ((float) value / ((float) max / 100)) / 100);
    }
}
