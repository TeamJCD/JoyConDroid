package com.rdapps.gamepad.amiibo;

import android.content.Context;
import com.rdapps.gamepad.util.PreferenceUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AmiiboConfig {

    private Context appContext;

    public void setAmiiboBytes(byte[] bytes) {
        PreferenceUtils.setAmiiboBytes(appContext, bytes);
    }

    public byte[] getAmiiboBytes() {
        return PreferenceUtils.getAmiiboBytes(appContext);
    }

    public void removeAmiiboBytes() {
        PreferenceUtils.removeAmiiboBytes(appContext);
    }
}
