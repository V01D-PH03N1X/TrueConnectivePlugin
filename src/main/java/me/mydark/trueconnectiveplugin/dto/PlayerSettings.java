//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.mydark.trueconnectiveplugin.manager.DatabaseManager;
import org.bukkit.entity.Player;

@Slf4j
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSettings {
    private boolean isActionbarEnabled = true;
    private boolean isBossbarEnabled = true;

    public void setActionbarEnabled(Player target, DatabaseManager databaseManager, boolean isActionbarEnabled) {
        this.isActionbarEnabled = isActionbarEnabled;
        onChange(databaseManager, target);
    }

    public void setBossbarEnabled(Player target, DatabaseManager databaseManager, boolean isBossbarEnabled) {
        this.isBossbarEnabled = isBossbarEnabled;
        onChange(databaseManager, target);
    }

    private void onChange(DatabaseManager databaseManager, Player target) {
        // Update the player's settings in the database
        log.info(
                "Updating player settings for {} with actionbar: {} and bossbar: {}",
                target.getName(),
                isActionbarEnabled,
                isBossbarEnabled);
        databaseManager.setPlayerSettings(target, this);
    }
}
