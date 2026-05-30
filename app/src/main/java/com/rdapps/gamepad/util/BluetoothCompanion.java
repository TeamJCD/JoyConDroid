package com.rdapps.gamepad.util;

import static com.rdapps.gamepad.log.JoyConLog.log;

public class BluetoothCompanion {
    private static final String TAG = BluetoothCompanion.class.getName();
    private static volatile boolean sLoaded = false;

    private static native String getBluetoothAddressNative();

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
}
