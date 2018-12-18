package com.rdapps.gamepad.util;

import com.rdapps.gamepad.log.JoyConLog;

public class ThreadUtil {
    private static final String TAG = ThreadUtil.class.getName();

    public static void safeSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            JoyConLog.log(TAG, "Safe sleep interrupted.", e);
        }
    }
}
