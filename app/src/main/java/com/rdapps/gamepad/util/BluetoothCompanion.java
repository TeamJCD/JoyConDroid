package com.rdapps.gamepad.util;

import static com.rdapps.gamepad.log.JoyConLog.log;

public class BluetoothCompanion {
    private static final String TAG = BluetoothCompanion.class.getName();
    private static volatile boolean sLoaded = false;

    /** Values returned by {@link #getBluetoothLinkMode()}, matching the
     * JoyConDroidCompanion shim's /dev/btlinkmode encoding. */
    public static final int LINK_MODE_UNKNOWN = -1;
    public static final int LINK_MODE_ACTIVE = 0;
    public static final int LINK_MODE_HOLD = 1;
    public static final int LINK_MODE_SNIFF = 2;
    public static final int LINK_MODE_PARK = 3;

    private static native String getBluetoothAddressNative();

    private static native int getBluetoothLinkModeNative();

    public static String getBluetoothAddress() {
        try {
            if (!sLoaded) {
                System.loadLibrary("joycondroid_jni");
                sLoaded = true;
            }
            return getBluetoothAddressNative();
        } catch (Throwable t) {
            log(TAG, "getBluetoothAddress failed", t);
            return null;
        }
    }

    /**
     * Returns the current Bluetooth link power mode (Active/Hold/Sniff/Park)
     * as observed by JoyConDroidCompanion's Mode-Change hook, or
     * {@link #LINK_MODE_UNKNOWN} if the module isn't installed or found
     * nothing to hook on this device.
     */
    public static int getBluetoothLinkMode() {
        try {
            if (!sLoaded) {
                System.loadLibrary("joycondroid_jni");
                sLoaded = true;
            }
            return getBluetoothLinkModeNative();
        } catch (Throwable t) {
            log(TAG, "getBluetoothLinkMode failed", t);
            return LINK_MODE_UNKNOWN;
        }
    }
}
