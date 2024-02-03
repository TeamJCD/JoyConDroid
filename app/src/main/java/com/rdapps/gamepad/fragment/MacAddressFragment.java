package com.rdapps.gamepad.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.toast.ToastHelper;
import com.rdapps.gamepad.util.MacUtils;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.util.Objects;

public class MacAddressFragment extends Fragment
        implements ResettableSettingFragment, View.OnClickListener {

    private static final String TAG = MacAddressFragment.class.getName();


    private TextView textView;


    public static MacAddressFragment getInstance() {
        MacAddressFragment fileSelectorFragment = new MacAddressFragment();
        Bundle bundle = new Bundle();
        fileSelectorFragment.setArguments(bundle);
        return fileSelectorFragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.component_file_selector, container, false);
        ((TextView) view.findViewById(R.id.title)).setText(R.string.mac_address);
        textView = view.findViewById(R.id.selected_file_path);
        view.findViewById(R.id.select_button).setOnClickListener(this);
        setMacAddressText();
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    private void setMacAddressText() {
        Context context = getContext();
        if (Objects.isNull(context) || Objects.isNull(textView)) {
            return;
        }
        String macAddress = PreferenceUtils.getBluetoothAddress(context);
        if (Objects.nonNull(macAddress)) {
            textView.setText(macAddress);
        } else {
            textView.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        if (Objects.isNull(context) || Objects.isNull(textView)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.set_bt_address);
        View inflate = getLayoutInflater().inflate(R.layout.alert_bluetooth_address, null);
        final EditText macEditText = inflate.findViewById(R.id.macEditText);
        builder.setView(inflate);
        builder.setNegativeButton(getText(R.string.clear), (e, w) -> {
            PreferenceUtils.removeBluetoothAddress(context);
            setMacAddressText();
        });
        builder.setPositiveButton(getText(R.string.set), (e, w) -> {
            Editable editable = macEditText.getText();
            if (Objects.nonNull(editable)) {
                try {
                    String btStr = editable.toString();
                    MacUtils.parseMacAddress(btStr);
                    PreferenceUtils.setBluetoothAddress(context, btStr);
                    setMacAddressText();
                    return;
                } catch (IllegalArgumentException exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
                ToastHelper.incorrectMacAddress(context);
            }
        });
        builder.show();
    }

    @Override
    public void reset() {
        Context context = getContext();
        if (Objects.nonNull(context)) {
            PreferenceUtils.removeBluetoothAddress(context);
        }
        setMacAddressText();
    }
}
