package com.rdapps.gamepad.protocol;

import android.content.Context;
import com.rdapps.gamepad.util.PreferenceUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JoyControllerConfig {
    private static final long WAIT_BEFORE_HANDSHAKE_MS = 1200;

    private Context appContext;

    public long getWaitBeforeHandshakeMs() {
        return WAIT_BEFORE_HANDSHAKE_MS;
    }

    public int getPacketRate() {
        return PreferenceUtils.getPacketRate(appContext);
    }
}
