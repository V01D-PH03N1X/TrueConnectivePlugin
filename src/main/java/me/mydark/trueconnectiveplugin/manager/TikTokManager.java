//********************************************************************************************
// Author:      V01D-PH03N1X (PinguBasti), TrueConnective Paul & Stolle GbR
// Project:     TrueConnective Paper Plugin
// Description: Management Plugin for Paper Servers (Minecraft)
//********************************************************************************************
package me.mydark.trueconnectiveplugin.manager;

import io.github.jwdeveloper.tiktok.TikTokLive;
import me.mydark.trueconnectiveplugin.TrueConnective;
import org.slf4j.Logger;

public class TikTokManager {
    private static Logger log;

    public TikTokManager() {
        log = TrueConnective.getLog();
    }
    /**
     * Check if the TikTok user is live or not
     */
    public boolean checkTikTokLive(String username) {
        log.info("Checking if TikTok user {} is live", username);
        if (username == null) {
            return false;
        }
        return TikTokLive.isLiveOnline(username);
    }
}
