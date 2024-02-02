package com.rdapps.gamepad;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class JoyConDroidApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
