package com.rdapps.gamepad.amiibo;

import static com.rdapps.gamepad.log.JoyConLog.log;

import android.content.Context;
import android.net.Uri;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AmiiboConfig {
    private static final String TAG = AmiiboConfig.class.getName();

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

    public void saveAmiiboFileToDisk(byte[] bytes) {
        Uri uri = PreferenceUtils.getAmiiboFileUri(appContext);
        if (Objects.isNull(uri)) {
            return;
        }
        try (OutputStream os = appContext.getContentResolver().openOutputStream(uri, "wt")) {
            os.write(bytes);
            log(TAG, "Amiibo file written back to disk");
        } catch (IOException | SecurityException e) {
            log(TAG, "Failed to write amiibo file back to disk: " + e.getMessage(), true);
        }
    }
}
