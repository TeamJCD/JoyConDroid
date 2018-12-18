package com.rdapps.gamepad.util;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.Optional;

public class EventUtils {

    public static MotionEvent getTouchDownEvent() {
        return getTouchEvent(MotionEvent.ACTION_DOWN);
    }

    public static MotionEvent getTouchUpEvent() {
        return getTouchEvent(MotionEvent.ACTION_UP);
    }

    private static MotionEvent getTouchEvent(int action) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
        int metaState = 0;
        return MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                x,
                y,
                metaState
        );
    }

    public static boolean isGamePadSource(int source) {
//        return ((source & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
//                ((source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK);
        return true;
    }

    public static MotionEvent getJoyStickEvent(float x, float y, float radius, float centerX, float centerY) {
        if (Float.compare(x, 0) == 0 && Float.compare(y, 0) == 0) {
            return getTouchEvent(MotionEvent.ACTION_CANCEL);
        } else {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            x = x * radius + centerX;
            y = y * radius + centerY;
            int metaState = 0;
            return MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_MOVE,
                    x,
                    y,
                    metaState
            );
        }
    }

    public static float getCenteredAxis(MotionEvent event,
                                        InputDevice device, int axis) {
        final Optional<InputDevice.MotionRange> range =
                Optional.ofNullable(device)
                        .map(id -> id.getMotionRange(axis, event.getSource()));

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.map(InputDevice.MotionRange::getFlat)
                    .orElse(0f);
            final float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public static int getMaxedAxis(MotionEvent event,
                                       InputDevice device, int axis) {
        final Optional<InputDevice.MotionRange> range =
                Optional.ofNullable(device)
                        .map(id -> id.getMotionRange(axis, event.getSource()));

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range.isPresent()) {
            final float flat = range.map(InputDevice.MotionRange::getFlat)
                    .orElse(0f);
            final float max = range.map(InputDevice.MotionRange::getMax)
                    .orElse(0f);
            final float value = event.getAxisValue(axis);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat && Math.abs(value) > max * 0.8) {
                return value > 0 ? 1 : -1;
            }
        }
        return 0;
    }
}
