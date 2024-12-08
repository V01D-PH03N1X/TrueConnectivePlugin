//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.manager;

import io.github.jwdeveloper.tiktok.TikTokLive;
import me.mydark.trueconnectiveplugin.TrueConnective;
import org.slf4j.Logger;

/**
 * Manager class for handling TikTok related operations.
 */
public class TikTokManager {
    private static Logger log;

    /**
     * Constructor for TikTokManager.
     * Initializes the logger.
     */
    public TikTokManager() {
        log = TrueConnective.getLog();
    }

    /**
     * Checks if the TikTok user is live.
     *
     * @param username The TikTok username to check.
     * @return true if the user is live, false otherwise.
     */
    public boolean checkTikTokLive(String username) {
        log.info("Checking if TikTok user {} is live", username);
        if (username == null) {
            return false;
        }
        return TikTokLive.isLiveOnline(username);
    }
}
