package com.rdapps.gamepad.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.rdapps.gamepad.model.CustomUiItem;
import com.rdapps.gamepad.protocol.ControllerType;
import java.util.ArrayList;
import java.util.List;

public class CustomUiDbHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "customUI.db";
    public static final String TABLE_NAME = "CustomUI";
    public static final String COLUMN_ID = "CustomId";
    public static final String COLUMN_UI_ID = "CustomUIId";
    public static final String COLUMN_NAME = "CustomUIName";
    public static final String COLUMN_TYPE = "CustomUIType";
    public static final String COLUMN_VERSION = "CustomUIVersion";
    public static final String COLUMN_APP_VERSION = "CustomUIAppVersion";
    public static final String COLUMN_PATH = "CustomUIPath";


    public CustomUiDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_UI_ID + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_VERSION + " INTEGER,"
                + COLUMN_APP_VERSION + " INTEGER,"
                + COLUMN_PATH + " TEXT" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertUi(
            String authorityPath, String id, String name, String type,
            Integer version, Integer appVersion) {
        final SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PATH, authorityPath);
        contentValues.put(COLUMN_UI_ID, id);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_TYPE, type);
        contentValues.put(COLUMN_VERSION, version);
        contentValues.put(COLUMN_APP_VERSION, appVersion);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public CustomUiItem getCustomUi(String authorityPath, String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{
                        COLUMN_PATH, COLUMN_NAME, COLUMN_TYPE, COLUMN_VERSION, COLUMN_APP_VERSION },
                COLUMN_PATH + "=? AND " + COLUMN_UI_ID + "=?",
                new String[]{authorityPath, id},
                null,
                null,
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String authority = cursor.getString(0);
            String name = cursor.getString(1);
            String type = cursor.getString(2);
            int version = cursor.getInt(3);
            int appVersion = cursor.getInt(4);
            CustomUiItem customUiItem = new CustomUiItem();
            customUiItem.setName(name);
            customUiItem.setType(ControllerType.valueOf(type));
            customUiItem.setVersion(version);
            customUiItem.setAppVersion(appVersion);
            customUiItem.setUrl(authority);
            cursor.close();
            db.close();
            return customUiItem;
        } else {
            return null;
        }
    }

    public List<CustomUiItem> getCustomUis() {
        List<CustomUiItem> customUiItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{
                        COLUMN_PATH, COLUMN_NAME, COLUMN_TYPE, COLUMN_VERSION, COLUMN_APP_VERSION },
                null,
                null,
                null,
                null,
                null);
        while (cursor.moveToNext()) {
            String authority = cursor.getString(0);
            String name = cursor.getString(1);
            String type = cursor.getString(2);
            int version = cursor.getInt(3);
            int appVersion = cursor.getInt(4);
            CustomUiItem customUiItem = new CustomUiItem();
            customUiItem.setName(name);
            customUiItem.setType(ControllerType.valueOf(type));
            customUiItem.setVersion(version);
            customUiItem.setAppVersion(appVersion);
            customUiItem.setUrl(authority);
            customUiItems.add(customUiItem);
        }
        cursor.close();
        db.close();
        return customUiItems;
    }

    public void deleteCustomUi(String path, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,
                COLUMN_PATH + "=? AND " + COLUMN_UI_ID + "=?",
                new String[]{path, id});
    }
}
