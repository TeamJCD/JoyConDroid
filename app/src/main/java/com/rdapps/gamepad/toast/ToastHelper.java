package com.rdapps.gamepad.toast;

import static com.rdapps.gamepad.log.JoyConLog.log;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;
import com.rdapps.gamepad.R;

public class ToastHelper {
    private static final String TAG = ToastHelper.class.getName();

    public static void bluetoothNotAvailable(Context context) {
        Toast.makeText(
                context,
                R.string.bt_not_available,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void cannotSetBluetoothName(Context context, String deviceName) {
        Toast.makeText(
                context,
                context.getString(R.string.bt_name_not_set, deviceName),
                Toast.LENGTH_LONG
        ).show();
    }

    public static void sdpFailed(Context context) {
        Toast.makeText(
                context,
                R.string.bt_sdp_failed,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void couldNotRegisterApp(Context context) {
        Toast.makeText(
                context,
                R.string.bt_register_failed,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void deviceConnected(Context context) {
        Toast.makeText(
                context,
                R.string.connected_to_switch,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void deviceDisconnected(Context context) {
        Toast.makeText(
                context,
                R.string.disconnected_from_switch,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void deviceIsNotCompatible(Context context) {
        Toast.makeText(
                context,
                R.string.device_is_not_compatible,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void incorrectMacAddress(Context context) {
        Toast.makeText(
                context,
                R.string.incorrect_mac_address,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void invalidIntent(Context context) {
        Toast.makeText(
                context,
                R.string.invalid_intent,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void missingPermission(Context context, String permName) {
        try {
            Toast.makeText(
                    context,
                    context.getString(R.string.missingPermission,
                            context.getPackageManager().getPermissionInfo(permName, 0)
                                    .loadLabel(context.getPackageManager())),
                    Toast.LENGTH_LONG
            ).show();
        } catch (PackageManager.NameNotFoundException e) {
            log(TAG, "Permission name not found", e);
        }
    }

    public static void uiLoaded(Context context) {
        Toast.makeText(
                context,
                R.string.custom_ui_loaded,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void uiIsNotFound(Context context) {
        Toast.makeText(
                context,
                R.string.custom_ui_is_not_found,
                Toast.LENGTH_LONG
        ).show();
    }

    public static void uiHasNewerVersion(Context context) {
        Toast.makeText(
                context,
                R.string.custom_ui_has_newer_version,
                Toast.LENGTH_LONG
        ).show();
    }
}
