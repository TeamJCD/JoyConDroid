package com.rdapps.gamepad.util;

import android.util.Log;

import com.google.android.gms.common.util.Hex;

public class MacUtils {
    private static final String TAG = MacUtils.class.getName();

    public static byte[] parseMacAddress(String localMacAddress) {
        String[] macBytes = localMacAddress.trim().split(":");
        if (macBytes.length != 6) {
            Log.e(TAG, "MAC address does not have 6 segments");
        }

        byte[] buffer = new byte[6];
        for (int i = 0; i < macBytes.length; i++) {
            String macByte = macBytes[i];
            byte[] bytes = Hex.stringToBytes(macByte);
            if (bytes.length != 1) {
                Log.e(TAG, "MAC segment resulted more than 1 byte");
                throw new IllegalArgumentException("MAC segment resulted more than 1 byte");
            }
            buffer[i] = bytes[0];
        }
        return buffer;
    }
}
