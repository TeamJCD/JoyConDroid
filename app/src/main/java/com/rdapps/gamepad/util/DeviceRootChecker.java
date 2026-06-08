package com.rdapps.gamepad.util;

import android.content.Context;
import com.rdapps.gamepad.log.JoyConLog;
import java.io.File;

/**
 * Utility to detect device root status and determine operating mode
 */
public class DeviceRootChecker {
    private static final String TAG = DeviceRootChecker.class.getName();

    private static final String[] ROOT_PATHS = {
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/tmp/su",
            "/data/adb/magisk/su"
    };

    public static boolean isDeviceRooted() {
        for (String path : ROOT_PATHS) {
            if (new File(path).exists()) {
                JoyConLog.log(TAG, "Device rooted - found: " + path);
                return true;
            }
        }
        return false;
    }

    public static OperatingMode getOperatingMode(Context context) {
        if (isDeviceRooted()) {
            return OperatingMode.ROOTED_HID_MODE;
        } else if (isAccessibilityServiceEnabled(context)) {
            return OperatingMode.NON_ROOTED_ACCESSIBILITY_MODE;
        } else {
            return OperatingMode.NO_MODE_AVAILABLE;
        }
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        try {
            int accessibilityEnabled = android.provider.Settings.Secure.getInt(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED,
                    0
            );
            return accessibilityEnabled == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public enum OperatingMode {
        ROOTED_HID_MODE("HID Device (Rooted)"),
        NON_ROOTED_ACCESSIBILITY_MODE("Accessibility Service (Non-Rooted)"),
        NO_MODE_AVAILABLE("No Mode Available");

        private final String description;

        OperatingMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
