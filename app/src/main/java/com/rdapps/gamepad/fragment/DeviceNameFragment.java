package com.rdapps.gamepad.fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.rdapps.gamepad.R;
import com.rdapps.gamepad.util.PreferenceUtils;

import java.util.Objects;
import java.util.Optional;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.toast.ToastHelper.missingPermission;

public class DeviceNameFragment extends Fragment implements ResettableSettingFragment, View.OnClickListener {

    private static final String TAG = DeviceNameFragment.class.getName();


    private TextView textView;
    private BluetoothAdapter bluetoothAdapter;


    public static DeviceNameFragment getInstance() {
        DeviceNameFragment fileSelectorFragment = new DeviceNameFragment();
        Bundle bundle = new Bundle();
        fileSelectorFragment.setArguments(bundle);
        return fileSelectorFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_file_selector, container, false);
        ((TextView) view.findViewById(R.id.title)).setText(R.string.device_name);
        textView = view.findViewById(R.id.selected_file_path);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        view.findViewById(R.id.select_button).setOnClickListener(this);
        setDeviceName();
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    private void setDeviceName() {
        Context context = getContext();
        if (Objects.isNull(context) || Objects.isNull(textView)) {
            return;
        }
        Optional<String> originalName = PreferenceUtils.getOriginalName(context);
        if (originalName.isPresent()) {
            textView.setText(originalName.get());
        } else if (Objects.nonNull(bluetoothAdapter)) {
            try {
                String name = bluetoothAdapter.getName();
                textView.setText(name);
            } catch (SecurityException ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    missingPermission(context, Manifest.permission.BLUETOOTH_CONNECT);
                    log(TAG, "Missing permission", ex);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        if (Objects.isNull(context) || Objects.isNull(textView) || Objects.isNull(bluetoothAdapter)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.device_name);
        View inflate = getLayoutInflater().inflate(R.layout.alert_device_name, null);
        final EditText deviceEditText = inflate.findViewById(R.id.deviceEditText);
        builder.setView(inflate);
        builder.setPositiveButton(getText(R.string.set), (e, w) -> {
            Editable editable = deviceEditText.getText();
            if (Objects.nonNull(editable)) {
                try {
                    String deviceName = editable.toString();
                    PreferenceUtils.removeOriginalName(context);
                    PreferenceUtils.saveOriginalName(context, deviceName);
                    bluetoothAdapter.setName(deviceName);
                    setDeviceName();
                } catch (SecurityException ex) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        missingPermission(context, Manifest.permission.BLUETOOTH_CONNECT);
                        log(TAG, "Missing permission", ex);
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    public void reset() {
        Context context = getContext();
        if (Objects.nonNull(context)) {
            Optional<String> originalName = PreferenceUtils.getOriginalName(context);
            originalName.ifPresent(bluetoothAdapter::setName);
            PreferenceUtils.removeOriginalName(context);
        }
        setDeviceName();
    }
}
