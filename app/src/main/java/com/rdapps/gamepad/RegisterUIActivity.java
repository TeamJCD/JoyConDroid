package com.rdapps.gamepad;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.rdapps.gamepad.log.JoyConLog;
import com.rdapps.gamepad.model.CustomUiItem;
import com.rdapps.gamepad.sql.CustomUiDbHandler;
import com.rdapps.gamepad.toast.ToastHelper;
import com.rdapps.gamepad.util.UnzipUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class RegisterUIActivity extends Activity {
    private static final String TAG = RegisterUIActivity.class.getName();

    public static final String ACTION_NAME = "com.rdapps.gamepad.registerui";
    public static final String EXTRA_UI_AUTHORITY = "JC_UI_AUTHORITY";

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String TYPE = "TYPE";
    private static final String VERSION = "VERSION";
    private static final String APP_VERSION = "APP_VERSION";
    private static final String ENTRY = "ENTRY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        Bundle extras = intent.getExtras();


        if (ACTION_NAME.equals(action) && Objects.nonNull(extras)) {
            String authority = extras.getString(EXTRA_UI_AUTHORITY);
            if (Objects.nonNull(authority)) {
                Uri content = Uri.parse("content://" + authority + "/");
                Cursor query = getContentResolver()
                        .query(content, new String[]{ID, NAME, TYPE, VERSION, ENTRY, APP_VERSION}, null, null, null);

                if (Objects.nonNull(query)) {
                    try {
                        int count = query.getCount();
                        if (count > 0) {
                            File parent = new File(getFilesDir(), authority);
                            if (!parent.exists() || !parent.isDirectory()) {
                                parent.mkdir();
                            }

                            try (CustomUiDbHandler customUIDBHandler = new CustomUiDbHandler(this)) {
                                for (int i = 0; i < count; i++) {
                                    query.moveToPosition(i);

                                    String id = query.getString(0);
                                    String name = query.getString(1);
                                    String type = query.getString(2);
                                    int version = query.getInt(3);
                                    String entry = query.getString(4);
                                    int appVersion = query.getInt(5);
                                    File folder = new File(parent, name + "_" + id);
                                    String path = Uri.fromFile(new File(folder, entry)).toString();

                                    CustomUiItem customUI = customUIDBHandler.getCustomUi(path, id);
                                    if (Objects.nonNull(customUI)) {
                                        int oldVersion = customUI.getVersion();
                                        if (oldVersion >= version) {
                                            ToastHelper.uiHasNewerVersion(this);
                                            finish();
                                            return;
                                        } else {
                                            customUIDBHandler.deleteCustomUi(path, id);
                                            FileUtils.deleteDirectory(folder);
                                            folder.mkdir();
                                        }
                                    }

                                    Uri uri = content.buildUpon()
                                            .appendQueryParameter(ID, id)
                                            .appendQueryParameter(NAME, name)
                                            .appendQueryParameter(TYPE, type)
                                            .appendQueryParameter(VERSION, Integer.toString(version))
                                            .appendQueryParameter(APP_VERSION, Integer.toString(appVersion))
                                            .appendQueryParameter(ENTRY, entry)
                                            .build();

                                    AssetFileDescriptor uiBundle = getContentResolver()
                                            .openAssetFileDescriptor(uri, "r");

                                    if (Objects.isNull(uiBundle)) {
                                        ToastHelper.uiIsNotFound(this);
                                        finish();
                                        return;
                                    }


                                    UnzipUtil unzipUtil = new UnzipUtil(uiBundle, folder.getAbsolutePath());
                                    unzipUtil.unzip();
                                    customUIDBHandler.insertUi(path, id, name, type, version, appVersion);
                                }
                            }

                            ToastHelper.uiLoaded(this);
                            finish();
                            return;
                        }
                    } catch (FileNotFoundException e) {
                        JoyConLog.log(TAG, "File Not Found", e);
                    } catch (IOException e) {
                        JoyConLog.log(TAG, "IO Exception", e);
                    } finally {
                        query.close();
                    }
                }
            }
        }

        ToastHelper.invalidIntent(this);
        finish();
    }
}
