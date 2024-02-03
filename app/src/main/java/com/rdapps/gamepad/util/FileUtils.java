package com.rdapps.gamepad.util;

import static android.provider.OpenableColumns.DISPLAY_NAME;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {
    public String getDisplayNameFromUri(final Context context, final Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            int nameIndex = cursor.getColumnIndex(DISPLAY_NAME);
            cursor.moveToFirst();
            return cursor.getString(nameIndex);
        }
    }
}
