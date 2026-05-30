package com.rdapps.gamepad;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.rdapps.gamepad.util.BluetoothCompanion;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.util.Objects;

public class JoyConDroidApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);

        if (Objects.isNull(PreferenceUtils.getBluetoothAddress(this))) {
            String btAddress = BluetoothCompanion.getBluetoothAddress();
            if (Objects.nonNull(btAddress)) {
                PreferenceUtils.setBluetoothAddress(this, btAddress);
            }
        }
    }
}
