package com.rdapps.gamepad.log;

import android.util.Log;

import com.rdapps.gamepad.BuildConfig;

import static android.util.Log.ASSERT;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

public class JoyConLog {
    private static final String PREFIX = "JoyCon Droid-";
    private static final int LOG_LEVEL = BuildConfig.LOG_LEVEL;

    public static void log(String tag, String message, Throwable tr) {
        switch (LOG_LEVEL) {
            case VERBOSE:
                Log.v(tag, message, tr);
                break;
            case DEBUG:
                log(tag, message, tr);
                break;
            case INFO:
                Log.i(tag, message, tr);
                break;
            case WARN:
                Log.w(tag, message, tr);
                break;
            case ERROR:
                Log.e(tag, message, tr);
                break;
            case ASSERT:
                Log.wtf(tag, message, tr);
                break;
            default:
                break;
        }
    }

    public static void log(String tag, String message) {
        log(tag, message, false);
    }

    public static void log(String tag, String message, boolean sendLog) {
        tag = PREFIX + tag;
        switch (LOG_LEVEL) {
            case VERBOSE:
                Log.v(tag, message);
                break;
            case DEBUG:
                log(tag, message);
                break;
            case INFO:
                Log.i(tag, message);
                break;
            case WARN:
                Log.w(tag, message);
                break;
            case ERROR:
                Log.e(tag, message);
                break;
            case ASSERT:
                Log.wtf(tag, message);
                break;
            default:
                break;
        }
    }

}
