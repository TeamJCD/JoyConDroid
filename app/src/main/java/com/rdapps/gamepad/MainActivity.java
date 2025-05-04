package com.rdapps.gamepad;

import static android.Manifest.permission.BLUETOOTH_ADVERTISE;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;
import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_TURNING_ON;
import static com.rdapps.gamepad.ControllerActivity.CONTROLLER_TYPE;
import static com.rdapps.gamepad.UserGuideActivity.PATH;
import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.protocol.ControllerType.LEFT_JOYCON;
import static com.rdapps.gamepad.toast.ToastHelper.missingPermission;
import static com.rdapps.gamepad.util.PreferenceUtils.MAC_FAKE_ADDRESS;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.service.BluetoothBroadcastReceiver;
import com.rdapps.gamepad.toast.ToastHelper;
import com.rdapps.gamepad.util.MacUtils;
import com.rdapps.gamepad.util.PreferenceUtils;
import com.rdapps.gamepad.versionchecker.Version;
import com.rdapps.gamepad.versionchecker.VersionCheckerClient;
import com.rdapps.gamepad.versionchecker.VersionCheckerService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @RequiresApi(Build.VERSION_CODES.S)
    private static final String[] RUNTIME_PERMISSIONS_S
            = new String[] { BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT, BLUETOOTH_SCAN };

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private static final String[] RUNTIME_PERMISSIONS_T
            = new String[] { POST_NOTIFICATIONS };

    private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;

    private final ActivityResultLauncher<Intent> legalActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != InfoAndLegalActivity.ACCEPT) {
                            finish();
                        } else {
                            if (!PreferenceUtils.getDoNotShow(this)) {
                                showGuide();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_drawer);
        Toolbar mainToolbar = findViewById(R.id.mainMenuToolbar);
        setSupportActionBar(mainToolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mainToolbar, R.string.ok, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this::onOptionsItemSelected);

        if (!BuildConfig.VM) {
            if (!PreferenceUtils.getLegalAccepted(this)) {
                showInitialInfoAndLegal();
            } else if (!PreferenceUtils.getDoNotShow(this)) {
                showGuide();
            }
        }

        checkUpdate();
        checkPermissions();
    }

    private void checkUpdate() {
        try {
            if (BuildConfig.CHECK_UPDATE) {
                VersionCheckerService versionCheckerService = VersionCheckerClient.getService();
                versionCheckerService.getVersion().enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Version> call, Response<Version> response) {
                        Optional.ofNullable(response.body())
                                .filter(version -> version.getVersionCode() != null
                                        && BuildConfig.VERSION_CODE < version.getVersionCode())
                                .ifPresent(version -> showUpdateDialog(version.getVersion()));
                    }

                    @Override
                    public void onFailure(Call<Version> call, Throwable t) {
                        log(TAG, t.getMessage(), true);
                    }
                });
            }
        } catch (Exception e) {
            log(TAG, e.getMessage(), true);
        }
    }

    private void showUpdateDialog(String version) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.update_available);
        builder.setMessage(R.string.update_message);
        builder.setNegativeButton(R.string.later, (dialog, which) -> {
        });
        builder.setPositiveButton(R.string.update, (dialog, which) -> {
            String url = "https://github.com/TeamJCD/JoyConDroid/releases/download/" + version
                    + "/JoyConDroid-" + version + ".apk";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        builder.create().show();
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Arrays.stream(RUNTIME_PERMISSIONS_S)
                    .filter(p -> ContextCompat.checkSelfPermission(this, p)
                            != PackageManager.PERMISSION_GRANTED)
                    .forEach(permissions::add);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Arrays.stream(RUNTIME_PERMISSIONS_T)
                    .filter(p -> ContextCompat.checkSelfPermission(this, p)
                            != PackageManager.PERMISSION_GRANTED)
                    .forEach(permissions::add);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (id == R.id.action_user_guide) {
            showGuide();
            return true;
        } else if (id == R.id.action_revert_bluetooth) {
            revertBluetoothConfig();
            return true;
        } else if (id == R.id.action_info) {
            showInfoAndLegal();
            return true;
        } else if (id == R.id.action_custom_left_joycon) {
            showCustomUi();
            return true;
        } else if (id == R.id.action_button_mapping) {
            showButtonMapping();
            return true;
        } else if (id == R.id.action_settings) {
            showSettings();
            return true;
        } else if (id == R.id.action_faq) {
            showFaq();
            return true;
        } else if (id == R.id.action_discord) {
            showDiscord();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void showDiscord() {
        String url = "https://discord.gg/5SFhf5C";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void showFaq() {
        Intent intent = new Intent(this, UserGuideActivity.class);
        intent.putExtra(PATH, "faq");
        startActivity(intent);
    }

    private void showCustomUi() {
        Intent intent = new Intent(this, CustomUiActivity.class);
        startActivity(intent);
    }

    public void showCustomUi(View v) {
        showCustomUi();
    }

    private void showButtonMapping() {
        Intent intent = new Intent(this, ButtonMappingActivity.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log(TAG, "Config Changed");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Objects.nonNull(bluetoothBroadcastReceiver)) {
            unregisterReceiver(bluetoothBroadcastReceiver);
        }
    }

    public void showLeftJoyCon(View v) {
        showController(LEFT_JOYCON);
    }

    public void showRightJoyCon(View v) {
        showController(ControllerType.RIGHT_JOYCON);
    }

    public void showProController(View v) {
        showController(ControllerType.PRO_CONTROLLER);
    }

    private void showController(ControllerType type) {
        String bluetoothAddress = PreferenceUtils.getBluetoothAddress(this);
        boolean ask = PreferenceUtils.shouldAskMacAddress(this);
        if (MAC_FAKE_ADDRESS.equals(bluetoothAddress) && ask) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.set_bt_address);
            View inflate = getLayoutInflater().inflate(R.layout.alert_bluetooth_address, null);
            final EditText macEditText = inflate.findViewById(R.id.macEditText);
            builder.setView(inflate);
            builder.setNegativeButton(getText(R.string.later), (e, w) -> showJoyconImpl(type));
            builder.setNeutralButton(R.string.never_ask_again, (e, w) -> {
                PreferenceUtils.doNotAskMacAddress(this, false);
                showJoyconImpl(type);
            });
            builder.setPositiveButton(getText(R.string.set), (e, w) -> {
                Editable editable = macEditText.getText();
                if (Objects.nonNull(editable)) {
                    try {
                        String btStr = editable.toString();
                        MacUtils.parseMacAddress(btStr);
                        PreferenceUtils.setBluetoothAddress(getApplicationContext(), btStr);
                        showJoyconImpl(type);
                        return;
                    } catch (IllegalArgumentException exception) {
                        Log.e(TAG, exception.getMessage(), exception);
                    }
                    ToastHelper.incorrectMacAddress(this);
                }
            });
            builder.show();
        } else {
            showJoyconImpl(type);
        }
    }

    private void showJoyconImpl(Serializable type) {
        Intent intent = new Intent(this, ControllerActivity.class);
        intent.putExtra(CONTROLLER_TYPE, type);
        startActivity(intent);
    }

    public void showGuide() {
        startActivity(new Intent(this, UserGuideActivity.class));
    }

    public void showInfoAndLegal() {
        startActivity(new Intent(this, InfoAndLegalActivity.class));
    }

    public void showInitialInfoAndLegal() {
        Intent intent = new Intent(this, InfoAndLegalActivity.class);
        legalActivityResultLauncher.launch(intent);
    }

    public void revertBluetoothConfig() {
        Optional<String> originalNameOpt = PreferenceUtils.getOriginalName(this);
        if (!originalNameOpt.isPresent()) {
            Toast.makeText(this, R.string.bt_config_was_not_changed, Toast.LENGTH_LONG).show();
            return;
        }

        Optional<BluetoothAdapter> bluetoothAdapter = Optional.ofNullable(
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE))
                .map(BluetoothManager::getAdapter);
        if (bluetoothAdapter.isPresent()) {
            try {
                bluetoothAdapter.get().setName(originalNameOpt.get());
                PreferenceUtils.removeOriginalName(this);
                PreferenceUtils.removeBluetoothAddress(this);
                PreferenceUtils.removeDoNotAskMacAddress(this);
                Toast.makeText(this, R.string.bt_config_is_reverted, Toast.LENGTH_LONG).show();
            } catch (SecurityException ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    missingPermission(getApplicationContext(), BLUETOOTH_CONNECT);
                    log(TAG, "Missing permission", ex);
                }
            }
        } else {
            bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver(new MainActBbrListener());
            registerReceiver(bluetoothBroadcastReceiver, new IntentFilter(ACTION_STATE_CHANGED));
        }
    }

    private class MainActBbrListener extends BluetoothBroadcastReceiver.BbrListener {
        public void stateChangedTo(int state) {
            switch (state) {
                case STATE_TURNING_ON:
                    log(TAG, "Bluetooth turning on");
                    break;
                case STATE_ON:
                    log(TAG, "Bluetooth on");
                    revertBluetoothConfig();
                    break;
                case STATE_TURNING_OFF:
                    log(TAG, "Bluetooth turning off");
                    break;
                case STATE_OFF:
                    log(TAG, "Bluetooth off");
                    break;
                default:
            }
        }
    }
}
