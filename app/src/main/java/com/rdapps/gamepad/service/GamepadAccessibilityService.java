package com.rdapps.gamepad.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import com.rdapps.gamepad.log.JoyConLog;
import java.util.Objects;

/**
 * Accessibility Service for non-rooted devices
 * Provides limited controller input simulation
 */
public class GamepadAccessibilityService extends AccessibilityService {
    private static final String TAG = GamepadAccessibilityService.class.getName();
    private static GamepadAccessibilityService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        JoyConLog.log(TAG, "Accessibility Service connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        }
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
        JoyConLog.log(TAG, "Accessibility Service interrupted");
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public static GamepadAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isServiceRunning() {
        return Objects.nonNull(instance);
    }
}
