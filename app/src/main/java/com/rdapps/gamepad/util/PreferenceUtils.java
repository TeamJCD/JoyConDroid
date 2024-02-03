package com.rdapps.gamepad.util;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import androidx.preference.PreferenceManager;
import java.util.Objects;
import java.util.Optional;

public class PreferenceUtils {
    public static final String MAC_FAKE_ADDRESS = "02:00:00:00:00:00";

    private static final String ORIGINAL_NAME = "ORIGINAL_NAME";
    private static final String DO_NOT_SHOW = "DO_NOT_SHOW";
    private static final String LEGAL_ACCEPTED = "LEGAL_ACCEPTED";
    private static final String BUTTON_MAPPING = "BUTTON_MAPPING";
    private static final String BT_ADDRESS = "BT_ADDRESS";
    private static final String DO_NOT_ASK_BT_ADDRESS = "DO_NOT_ASK_BT_ADDRESS";
    private static final String HAS_FILE_PREFIX = "HAS_";

    private static final String ENABLED_ACCELEROMETER = "ENABLED_ACCELEROMETER";
    private static final String ENABLED_GYROSCOPE = "ENABLED_GYROSCOPE";
    private static final String ENABLED_AMIIBO = "ENABLED_AMIIBO";
    private static final String AMIIBO_FILE_NAME = "AMIIBO_FILE_NAME";
    private static final String AMIIBO_BYTES = "AMIIBO_BYTES";
    private static final String HAPTIC_FEEDBACK_ENABLED = "HAPTIC_FEEDBACK_ENABLED";

    private static final String PACKET_RATE = "PACKET_RATE";

    public static void saveOriginalName(Context context, String name) {
        Optional<String> originalName = getOriginalName(context);
        if (!originalName.isPresent()) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(ORIGINAL_NAME, name)
                    .apply();
        }
    }

    public static Optional<String> getOriginalName(Context context) {
        String originalName = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(ORIGINAL_NAME, null);
        return Optional.ofNullable(originalName);
    }

    public static void removeOriginalName(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(ORIGINAL_NAME)
                .apply();
    }

    public static boolean getDoNotShow(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(DO_NOT_SHOW, false);
    }

    public static void setDoNotShow(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(DO_NOT_SHOW, value)
                .apply();
    }

    public static boolean getLegalAccepted(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LEGAL_ACCEPTED, false);
    }

    public static void setLegalAccepted(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LEGAL_ACCEPTED, value)
                .apply();
    }

    static String getButtonMapping(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(BUTTON_MAPPING, null);
    }

    static void setButtonMapping(Context context, String buttonMapping) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(BUTTON_MAPPING, buttonMapping)
                .apply();
    }

    public static String getBluetoothAddress(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(BT_ADDRESS, MAC_FAKE_ADDRESS);
    }

    public static void setBluetoothAddress(Context context, String btAddress) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(BT_ADDRESS, btAddress)
                .apply();
    }

    public static void removeBluetoothAddress(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(BT_ADDRESS)
                .apply();
    }

    public static void doNotAskMacAddress(Context context, boolean ask) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(DO_NOT_ASK_BT_ADDRESS, ask)
                .apply();
    }

    public static boolean shouldAskMacAddress(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(DO_NOT_ASK_BT_ADDRESS, true);
    }

    public static void removeDoNotAskMacAddress(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(DO_NOT_ASK_BT_ADDRESS)
                .apply();
    }

    public static boolean hasFile(Context context, String name) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(HAS_FILE_PREFIX + name, false);
    }

    public static void setFile(Context context, String name, boolean hasFile) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(HAS_FILE_PREFIX + name, hasFile)
                .apply();
    }

    public static boolean getAccelerometerEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ENABLED_ACCELEROMETER, true);
    }

    public static void setAccelerometerEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(ENABLED_ACCELEROMETER, enabled)
                .apply();
    }

    public static void removeAccelerometerEnabled(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(ENABLED_ACCELEROMETER)
                .apply();
    }

    public static boolean getGyroscopeEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ENABLED_GYROSCOPE, true);
    }

    public static void setGyroscopeEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(ENABLED_GYROSCOPE, enabled)
                .apply();
    }

    public static void removeGyroscopeEnabled(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(ENABLED_GYROSCOPE)
                .apply();
    }

    public static boolean getAmiiboEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(ENABLED_AMIIBO, false);
    }

    public static void setAmiiboEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(ENABLED_AMIIBO, enabled)
                .apply();
    }


    public static void removeAmiiboEnabled(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(ENABLED_AMIIBO)
                .apply();
    }

    public static int getPacketRate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PACKET_RATE, 15);
    }

    public static void setPacketRate(Context context, int packetRate) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PACKET_RATE, packetRate)
                .apply();
    }

    public static void removePacketRate(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(PACKET_RATE)
                .apply();
    }

    public static void setAmiiboBytes(Context context, byte[] bytes) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(AMIIBO_BYTES, Base64.encodeToString(bytes, Base64.DEFAULT))
                .apply();
    }

    public static byte[] getAmiiboBytes(Context context) {
        String amiiboBytes64 = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(AMIIBO_BYTES, null);
        if (Objects.nonNull(amiiboBytes64)) {
            return Base64.decode(amiiboBytes64, Base64.DEFAULT);
        }
        return null;
    }

    public static void removeAmiiboBytes(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(AMIIBO_BYTES)
                .apply();
    }

    public static boolean getHapticFeedBackEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(HAPTIC_FEEDBACK_ENABLED, false);
    }

    public static void setHapticFeedBackEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(HAPTIC_FEEDBACK_ENABLED, enabled)
                .apply();
    }

    public static void removeHapticFeedbackEnabled(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(HAPTIC_FEEDBACK_ENABLED)
                .apply();
    }

    public static String getAmiiboFileName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(AMIIBO_FILE_NAME, null);
    }

    public static void setAmiiboFileName(Context context, Uri uri) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(AMIIBO_FILE_NAME, FileUtils.getDisplayNameFromUri(context, uri))
                .apply();
    }


    public static void removeAmiiboFileName(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove(AMIIBO_FILE_NAME)
                .apply();
    }
}
