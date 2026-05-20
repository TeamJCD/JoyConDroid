package com.rdapps.gamepad;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.toast.ToastHelper.missingPermission;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreferenceCompat;
import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.device.DeviceName;
import com.rdapps.gamepad.memory.ControllerMemory;
import com.rdapps.gamepad.memory.RafSpiMemory;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.toast.ToastHelper;
import com.rdapps.gamepad.util.ByteUtils;
import com.rdapps.gamepad.util.MacUtils;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.io.IOUtils;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getName();

    private static final String KEY_ENABLED_ACCELEROMETER = "ENABLED_ACCELEROMETER";
    private static final String KEY_ENABLED_GYROSCOPE = "ENABLED_GYROSCOPE";
    private static final String KEY_HAPTIC_FEEDBACK_ENABLED = "HAPTIC_FEEDBACK_ENABLED";
    private static final String KEY_ENABLED_AMIIBO = "ENABLED_AMIIBO";
    private static final String KEY_AMIIBO_FILE = "amiibo_file";
    private static final String KEY_PACKET_RATE = "PACKET_RATE";
    private static final String KEY_LEFT_BODY = "left_joycon_body_color";
    private static final String KEY_LEFT_BUTTON = "left_joycon_button_color";
    private static final String KEY_RIGHT_BODY = "right_joycon_body_color";
    private static final String KEY_RIGHT_BUTTON = "right_joycon_button_color";
    private static final String KEY_BT_ADDRESS = "BT_ADDRESS";
    private static final String KEY_DEVICE_NAME = "DEVICE_NAME";

    private ControllerMemory leftEeprom;
    private ControllerMemory rightEeprom;
    private BluetoothAdapter bluetoothAdapter;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() != Activity.RESULT_OK
                                || result.getData() == null) {
                            return;
                        }

                        Context ctx = requireContext();
                        Uri uri = result.getData().getData();

                        try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
                            PreferenceUtils.setAmiiboFileName(ctx, uri);
                            PreferenceUtils.setAmiiboFileUri(ctx, uri);
                            PreferenceUtils.setAmiiboBytes(ctx, IOUtils.toByteArray(is));

                            updateAmiiboFileSummary();

                            Toast.makeText(ctx, R.string.amiibo_file_preset, Toast.LENGTH_LONG)
                                    .show();
                        } catch (IOException e) {
                            Toast.makeText(ctx, R.string.amiibo_file_cannot_be, Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey);

        Context ctx = requireContext();

        try {
            leftEeprom = new ControllerMemory(new RafSpiMemory(ctx,
                    ControllerType.LEFT_JOYCON.getBtName(),
                    ControllerType.LEFT_JOYCON.getMemoryResource()));
        } catch (IOException e) {
            log(TAG, "Left EEPROM load failed", e);
        }

        try {
            rightEeprom = new ControllerMemory(new RafSpiMemory(ctx,
                    ControllerType.RIGHT_JOYCON.getBtName(),
                    ControllerType.RIGHT_JOYCON.getMemoryResource()));
        } catch (IOException e) {
            log(TAG, "Right EEPROM load failed", e);
        }

        bluetoothAdapter = ctx.getSystemService(BluetoothManager.class).getAdapter();

        setupAmiiboSwitch();
        setupAmiiboFile();
        setupColorPreferences();
        setupMacAddress();
        setupDeviceName();
    }

    private void setupAmiiboSwitch() {
        SwitchPreferenceCompat amiiboSwitch = findPreference(KEY_ENABLED_AMIIBO);
        amiiboSwitch.setOnPreferenceChangeListener((pref, newValue) -> {
            if (!(boolean) newValue) {
                Context ctx = requireContext();
                PreferenceUtils.removeAmiiboBytes(ctx);
                PreferenceUtils.removeAmiiboFileName(ctx);
                PreferenceUtils.removeAmiiboFileUri(ctx);
                setAmiiboFileVisible(false);
                updateAmiiboFileSummary();
                return true;
            }

            showMtuOrAmiiboWarning((SwitchPreferenceCompat) pref);
            return false;
        });
    }

    private void setupAmiiboFile() {
        setAmiiboFileVisible(PreferenceUtils.getAmiiboEnabled(requireContext()));
        updateAmiiboFileSummary();

        Preference amiiboFile = findPreference(KEY_AMIIBO_FILE);
        amiiboFile.setOnPreferenceClickListener(pref -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/octet-stream");
            filePickerLauncher.launch(intent);
            return true;
        });
    }

    private void setupColorPreferences() {
        setupColorPref(KEY_LEFT_BODY, leftEeprom, true);
        setupColorPref(KEY_LEFT_BUTTON, leftEeprom, false);
        setupColorPref(KEY_RIGHT_BODY, rightEeprom, true);
        setupColorPref(KEY_RIGHT_BUTTON, rightEeprom, false);
    }

    private void setupColorPref(String key, ControllerMemory eeprom, boolean isBody) {
        Preference pref = findPreference(key);

        if (eeprom != null) {
            pref.setIcon(makeColorSwatch(isBody ? eeprom.getBodyColor() : eeprom.getButtonColor()));
        }

        pref.setOnPreferenceClickListener(p -> {
            showColorPicker(eeprom, isBody, p);
            return true;
        });
    }

    private void setupMacAddress() {
        EditTextPreference pref = findPreference(KEY_BT_ADDRESS);

        pref.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            editText.setSelection(editText.length());
        });

        pref.setOnPreferenceChangeListener((p, newValue) -> {
            String val = ((String) newValue).trim();
            if (val.isEmpty()) {
                ((EditTextPreference) p).setText(null);
                return false;
            }

            try {
                MacUtils.parseMacAddress(val);
                return true;
            } catch (IllegalArgumentException e) {
                ToastHelper.incorrectMacAddress(requireContext());
                return false;
            }
        });
    }

    private void setupDeviceName() {
        EditTextPreference pref = findPreference(KEY_DEVICE_NAME);

        pref.setSummaryProvider(p -> {
            String stored = ((EditTextPreference) p).getText();

            if (stored != null && !stored.isEmpty()) {
                return stored;
            }

            if (bluetoothAdapter != null) {
                try {
                    String name = bluetoothAdapter.getName();
                    return name != null ? name : "";
                } catch (SecurityException e) {
                    return "";
                }
            }

            return "";
        });

        pref.setOnBindEditTextListener(editText -> {
            if (editText.length() == 0 && bluetoothAdapter != null) {
                try {
                    String btName = bluetoothAdapter.getName();
                    if (btName != null) {
                        editText.setText(btName);
                    }
                } catch (SecurityException e) {
                    log(TAG, "Cannot read BT name for pre-fill", e);
                }
            }
            editText.setSelection(editText.length());
        });

        pref.setOnPreferenceChangeListener((p, newValue) -> {
            String name = ((String) newValue).trim();

            if (name.isEmpty()) {
                ((EditTextPreference) p).setText(null);
                return false;
            }

            Context ctx = requireContext();
            if (bluetoothAdapter != null) {
                try {
                    PreferenceUtils.saveOriginalName(ctx, bluetoothAdapter.getName());
                    bluetoothAdapter.setName(name);
                } catch (SecurityException e) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        missingPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT);
                        log(TAG, "Missing permission", e);
                    }
                }
            }

            return true;
        });
    }

    private void showColorPicker(ControllerMemory eeprom, boolean isBody, Preference pref) {
        if (eeprom == null) {
            return;
        }

        int night = requireContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        ColorPickerDialog dialog = ColorPickerDialog.createColorPickerDialog(requireContext(),
                night == Configuration.UI_MODE_NIGHT_YES
                        ? ColorPickerDialog.DARK_THEME : ColorPickerDialog.LIGHT_THEME);

        dialog.setTitle(pref.getTitle());
        dialog.setInitialColor(isBody ? eeprom.getBodyColor() : eeprom.getButtonColor());

        dialog.setOnColorPickedListener((color, hex) -> {
            dialog.setNegativeActionText(getString(android.R.string.cancel));
            dialog.setPositiveActionText(getString(android.R.string.ok));

            if (isBody) {
                eeprom.setBodyColor(color);
            } else {
                eeprom.setButtonColor(color);
            }

            pref.setIcon(makeColorSwatch(color));
        });

        dialog.show();
    }

    private void showMtuOrAmiiboWarning(SwitchPreferenceCompat amiiboSwitch) {
        DeviceName.with(requireContext()).request((info, error) -> {
            if (info != null && "Samsung".equalsIgnoreCase(info.manufacturer)) {
                showAmiiboExperimentalWarning(amiiboSwitch);
            } else {
                showMtuSizeWarning(amiiboSwitch);
            }
        });
    }

    private void showMtuSizeWarning(SwitchPreferenceCompat amiiboSwitch) {
        Context ctx = requireContext();
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.mtu_warning_title)
                .setMessage(R.string.mtu_warning_text)
                .setPositiveButton(R.string.continue_option, (d, i) ->
                        showAmiiboExperimentalWarning(amiiboSwitch))
                .setNegativeButton(android.R.string.cancel, (d, i) ->
                        amiiboSwitch.setChecked(false))
                .setOnCancelListener(d -> amiiboSwitch.setChecked(false))
                .show();
    }

    private void showAmiiboExperimentalWarning(SwitchPreferenceCompat amiiboSwitch) {
        Context ctx = requireContext();
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.amiibo_experimental_title)
                .setMessage(R.string.amiibo_experimental_text)
                .setPositiveButton(R.string.continue_option, (d, i) -> {
                    amiiboSwitch.setChecked(true);
                    setAmiiboFileVisible(true);
                })
                .setNegativeButton(android.R.string.cancel, (d, i) ->
                        amiiboSwitch.setChecked(false))
                .setOnCancelListener(d -> amiiboSwitch.setChecked(false))
                .show();
    }

    public void reset() {
        Context ctx = requireContext();

        PreferenceUtils.removeAccelerometerEnabled(ctx);
        PreferenceUtils.removeGyroscopeEnabled(ctx);
        PreferenceUtils.removeHapticFeedbackEnabled(ctx);
        PreferenceUtils.removeAmiiboEnabled(ctx);
        PreferenceUtils.removeAmiiboBytes(ctx);
        PreferenceUtils.removeAmiiboFileName(ctx);
        PreferenceUtils.removeAmiiboFileUri(ctx);
        PreferenceUtils.removePacketRate(ctx);

        EditTextPreference macPref = findPreference(KEY_BT_ADDRESS);
        if (macPref != null) {
            macPref.setText(null);
        }

        Optional<String> originalName = PreferenceUtils.getOriginalName(ctx);
        if (originalName.isPresent() && bluetoothAdapter != null) {
            try {
                bluetoothAdapter.setName(originalName.get());
            } catch (SecurityException e) {
                log(TAG, "Missing BT permission on reset", e);
            }
        }

        PreferenceUtils.removeOriginalName(ctx);

        EditTextPreference namePref = findPreference(KEY_DEVICE_NAME);
        if (namePref != null) {
            namePref.setText(null);
        }

        if (leftEeprom != null) {
            leftEeprom.setBodyColor(ByteUtils.byteArrayToColor(
                    new byte[]{0x0A, (byte) 0xB9, (byte) 0xE6}));
            leftEeprom.setButtonColor(ByteUtils.byteArrayToColor(
                    new byte[]{0x00, 0x1E, 0x1E}));
        }

        if (rightEeprom != null) {
            rightEeprom.setBodyColor(ByteUtils.byteArrayToColor(
                    new byte[]{(byte) 0xFF, 0x3C, 0x28}));
            rightEeprom.setButtonColor(ByteUtils.byteArrayToColor(
                    new byte[]{0x1E, 0x0A, 0x0A}));
        }

        ((SwitchPreferenceCompat) findPreference(KEY_ENABLED_ACCELEROMETER)).setChecked(true);
        ((SwitchPreferenceCompat) findPreference(KEY_ENABLED_GYROSCOPE)).setChecked(true);
        ((SwitchPreferenceCompat) findPreference(KEY_HAPTIC_FEEDBACK_ENABLED)).setChecked(false);
        ((SwitchPreferenceCompat) findPreference(KEY_ENABLED_AMIIBO)).setChecked(false);
        setAmiiboFileVisible(false);
        updateAmiiboFileSummary();

        SeekBarPreference pkRate = findPreference(KEY_PACKET_RATE);
        pkRate.setValue(15);

        setupColorPreferences();
    }

    private void setAmiiboFileVisible(boolean visible) {
        Preference pref = findPreference(KEY_AMIIBO_FILE);
        if (pref != null) {
            pref.setVisible(visible);
        }
    }

    private void updateAmiiboFileSummary() {
        Preference pref = findPreference(KEY_AMIIBO_FILE);
        if (pref == null) {
            return;
        }

        String name = PreferenceUtils.getAmiiboFileName(requireContext());
        pref.setSummary(name != null ? name : "");
    }

    private GradientDrawable makeColorSwatch(int color) {
        float dp = requireContext().getResources().getDisplayMetrics().density;

        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setSize((int) (24 * dp), (int) (24 * dp));
        d.setCornerRadius(4 * dp);
        d.setColor(color);
        return d;
    }
}
