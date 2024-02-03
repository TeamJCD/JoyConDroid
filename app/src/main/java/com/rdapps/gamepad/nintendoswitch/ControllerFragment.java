package com.rdapps.gamepad.nintendoswitch;

import static android.app.Activity.RESULT_OK;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static com.rdapps.gamepad.util.ControllerActionUtils.getAxisMapping;
import static com.rdapps.gamepad.util.ControllerActionUtils.getButtonMapping;
import static com.rdapps.gamepad.util.ControllerActionUtils.getJoystickMapping;
import static com.rdapps.gamepad.util.EventUtils.getCenteredAxis;
import static com.rdapps.gamepad.util.EventUtils.getJoyStickEvent;
import static com.rdapps.gamepad.util.EventUtils.getTouchDownEvent;
import static com.rdapps.gamepad.util.EventUtils.getTouchUpEvent;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Vibrator;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;
import com.erz.joysticklibrary.JoyStick;
import com.rdapps.gamepad.device.ButtonType;
import com.rdapps.gamepad.device.JoystickType;
import com.rdapps.gamepad.led.LedState;
import com.rdapps.gamepad.model.ControllerAction;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.util.Pair;
import com.rdapps.gamepad.util.PreferenceUtils;
import com.rdapps.gamepad.vibrator.VibrationPattern;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

public abstract class ControllerFragment extends Fragment {
    public static final int REQUEST_SELECT_FILE = 1;

    private Context context;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyroscope;

    @Setter
    protected JoyController device;

    private Map<Integer, ButtonType> buttonMap;
    private Map<Pair<Integer, Integer>, ButtonType> axisMap;
    private Map<JoystickType, ControllerAction> joystickMap;

    protected Boolean hapticFeedBackEnabled;
    protected Vibrator vibrator;


    private float prevRightX = 0;
    private float prevRightY = 0;
    private float prevLeftX = 0;
    private float prevLeftY = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public Context getContext() {
        Context context = super.getContext();
        if (Objects.nonNull(context)) {
            this.context = context;
        }
        return this.context;
    }

    public boolean isHapticFeedbackEnabled() {
        if (Objects.nonNull(hapticFeedBackEnabled)) {
            return hapticFeedBackEnabled;
        } else {
            Context context = getContext();
            if (Objects.nonNull(context)) {
                return PreferenceUtils.getHapticFeedBackEnabled(context);
            }
        }
        return false;
    }

    public Optional<Vibrator> getVibrator() {
        if (!isHapticFeedbackEnabled()) {
            return Optional.empty();
        }
        if (Objects.isNull(vibrator)) {
            Context context = getContext();
            if (Objects.nonNull(context)) {
                this.vibrator = context.getSystemService(Vibrator.class);
            }
        }
        return Optional.ofNullable(this.vibrator);
    }

    @Override
    public void onStart() {
        super.onStart();
        buttonMap = getButtonMapping(getContext());
    }

    public Map<Integer, ButtonType> getButtonMap() {
        if (Objects.isNull(buttonMap)) {
            buttonMap = getButtonMapping(getContext());
        }
        return buttonMap;
    }

    public Map<JoystickType, ControllerAction> getJoystickMap() {
        if (Objects.isNull(joystickMap)) {
            joystickMap = getJoystickMapping(getContext());
        }
        return joystickMap;
    }

    public Map<Pair<Integer, Integer>, ButtonType> getAxisMap() {
        if (Objects.isNull(axisMap)) {
            axisMap = getAxisMapping(getContext());
        }
        return axisMap;
    }


    public abstract ImageButton getA();

    public abstract ImageButton getB();

    public abstract ImageButton getX();

    public abstract ImageButton getY();

    public abstract ImageButton getSl();

    public abstract ImageButton getSr();

    public abstract ImageButton getL();

    public abstract ImageButton getR();

    public abstract ImageButton getZl();

    public abstract ImageButton getZr();

    public abstract ImageButton getMinus();

    public abstract ImageButton getPlus();

    public abstract ImageButton getHome();

    public abstract ImageButton getCapture();

    public abstract ImageButton getLeft();

    public abstract ImageButton getRight();

    public abstract ImageButton getUp();

    public abstract ImageButton getDown();

    public abstract ImageButton getSync();

    public abstract JoyStick getLeftJoyStick();

    public abstract JoyStick getRightJoyStick();

    public abstract boolean setLeftStickPress(boolean pressed);

    public abstract boolean setRightStickPress(boolean pressed);

    public abstract boolean reverseJoystickXy();

    public boolean handleKey(int keyCode, KeyEvent keyEvent) {
        MotionEvent event;
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            event = getTouchDownEvent();
        } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            event = getTouchUpEvent();
        } else {
            return false;
        }

        Map<Integer, ButtonType> buttonMap = getButtonMap();
        ButtonType buttonType = buttonMap.get(keyCode);
        if (Objects.isNull(buttonType)) {
            return false;
        }

        return dispatchButton(keyEvent, event, buttonType);
    }

    private boolean dispatchButton(KeyEvent keyEvent, MotionEvent event, ButtonType buttonType) {
        return switch (buttonType) {
            case LEFT -> dispatchEvent(getLeft(), event);
            case RIGHT -> dispatchEvent(getRight(), event);
            case UP -> dispatchEvent(getUp(), event);
            case DOWN -> dispatchEvent(getDown(), event);
            case B -> dispatchEvent(getB(), event);
            case A -> dispatchEvent(getA(), event);
            case Y -> dispatchEvent(getY(), event);
            case X -> dispatchEvent(getX(), event);
            case R -> dispatchEvent(getR(), event);
            case ZR -> dispatchEvent(getZr(), event);
            case RIGHT_SR, LEFT_SR -> dispatchEvent(getSr(), event);
            case L -> dispatchEvent(getL(), event);
            case ZL -> dispatchEvent(getZl(), event);
            case RIGHT_SL, LEFT_SL -> dispatchEvent(getSl(), event);
            case PLUS -> dispatchEvent(getPlus(), event);
            case MINUS -> dispatchEvent(getMinus(), event);
            case HOME -> dispatchEvent(getHome(), event);
            case CAPTURE -> dispatchEvent(getCapture(), event);
            case LEFT_STICK -> setLeftStickPress(keyEvent.getAction() == KeyEvent.ACTION_DOWN);
            case RIGHT_STICK -> setRightStickPress(keyEvent.getAction() == KeyEvent.ACTION_DOWN);
            case SYNC -> dispatchEvent(getSync(), event);
            default -> false;
        };
    }

    private static boolean dispatchEvent(ImageButton button, MotionEvent event) {
        return Optional.ofNullable(button)
                .map(b -> b.dispatchTouchEvent(event))
                .orElse(false);
    }

    public boolean handleGenericMotionEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return false;
        }

        InputDevice device = motionEvent.getDevice();
        boolean reverse = reverseJoystickXy();
        Map<JoystickType, ControllerAction> joystickMap = getJoystickMap();
        ControllerAction rightJoystickAction = joystickMap.get(JoystickType.RIGHT_JOYSTICK);
        ControllerAction leftJoystickAction = joystickMap.get(JoystickType.LEFT_JOYSTICK);

        float rightStickX = 0;
        float rightStickY = 0;
        if (rightJoystickAction != null) {
            rightStickX = (reverse ? -1 : 1) * getCenteredAxis(motionEvent, device,
                    reverse ? rightJoystickAction.getYAxis() : rightJoystickAction.getXAxis());
            rightStickY = getCenteredAxis(motionEvent, device,
                    reverse ? rightJoystickAction.getXAxis() : rightJoystickAction.getYAxis());
            rightStickX = rightStickX * rightJoystickAction.getXDirection();
            rightStickY = rightStickY * rightJoystickAction.getYDirection() * -1;
        }

        float leftStickX = 0;
        float leftStickY = 0;
        if (leftJoystickAction != null) {
            leftStickX = (reverse ? -1 : 1) * getCenteredAxis(motionEvent, device,
                    reverse ? leftJoystickAction.getYAxis() : leftJoystickAction.getXAxis());
            leftStickY = getCenteredAxis(motionEvent, device,
                    reverse ? leftJoystickAction.getXAxis() : leftJoystickAction.getYAxis());
            leftStickX = leftStickX * leftJoystickAction.getXDirection();
            leftStickY = leftStickY * leftJoystickAction.getYDirection() * -1;
        }

        JoyStick leftJoyStick = getLeftJoyStick();
        if (leftJoyStick != null) {
            float radius = leftJoyStick.getRadius();
            float centerX = leftJoyStick.getCenterX();
            float centerY = leftJoyStick.getCenterY();
            MotionEvent joyStickEvent = getJoyStickEvent(leftStickX, leftStickY,
                    radius, centerX, centerY);
            leftJoyStick.dispatchTouchEvent(joyStickEvent);
        }

        JoyStick rightJoyStick = getRightJoyStick();
        if (rightJoyStick != null) {
            float radius = rightJoyStick.getRadius();
            float centerX = rightJoyStick.getCenterX();
            float centerY = rightJoyStick.getCenterY();
            rightJoyStick.dispatchTouchEvent(
                    getJoyStickEvent(rightStickX, rightStickY,
                            radius, centerX, centerY));
        }

        final boolean processed = Float.compare(leftStickX, prevLeftX) != 0
                || Float.compare(leftStickY, prevLeftY) != 0
                || Float.compare(rightStickX, prevRightX) != 0
                || Float.compare(rightStickY, prevRightY) != 0;

        prevLeftX = leftStickX;
        prevLeftY = leftStickY;
        prevRightX = rightStickX;
        prevRightY = rightStickY;

        if (!processed) {
            processAxisHatX(motionEvent);
            processAxisHatY(motionEvent);
        }

        Map<Pair<Integer, Integer>, ButtonType> axisMap = getAxisMap();
        for (Map.Entry<Pair<Integer, Integer>, ButtonType> axisEntry : axisMap.entrySet()) {
            Pair<Integer, Integer> key = axisEntry.getKey();
            float centeredAxis = getCenteredAxis(motionEvent, device, key.getKey());
            MotionEvent event;
            KeyEvent keyEvent;
            if (Math.signum(centeredAxis) == key.getValue()) {
                event = getTouchDownEvent();
                keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, 0);
            } else {
                event = getTouchUpEvent();
                keyEvent = new KeyEvent(KeyEvent.ACTION_UP, 0);
            }
            dispatchButton(keyEvent, event, axisEntry.getValue());
        }
        return true;
    }

    private int prevXkeyCode = -1;
    private int prevYkexCode = -1;

    private void processAxisHatX(MotionEvent motionEvent) {
        float xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
        int keyCode;
        if (Float.compare(xaxis, -1.0f) == 0) {
            keyCode = KEYCODE_DPAD_LEFT;
        } else if (Float.compare(xaxis, 1.0f) == 0) {
            keyCode = KEYCODE_DPAD_RIGHT;

        } else {
            keyCode = -1;
        }

        if (prevXkeyCode != keyCode && prevXkeyCode != -1) {
            handleKey(prevXkeyCode, new KeyEvent(KeyEvent.ACTION_UP, prevXkeyCode));
        }
        prevXkeyCode = keyCode;
        if (keyCode != -1) {
            handleKey(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        }
    }

    private void processAxisHatY(MotionEvent motionEvent) {
        float yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);
        int keyCode;
        if (Float.compare(yaxis, -1.0f) == 0) {
            keyCode = KEYCODE_DPAD_UP;
        } else if (Float.compare(yaxis, 1.0f) == 0) {
            keyCode = KEYCODE_DPAD_DOWN;
        } else {
            keyCode = -1;
        }

        if (prevYkexCode != keyCode && prevYkexCode != -1) {
            handleKey(prevYkexCode, new KeyEvent(KeyEvent.ACTION_UP, prevYkexCode));
        }
        prevYkexCode = keyCode;
        if (keyCode != -1) {
            handleKey(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        }
    }

    public SensorManager getSensorManager() {
        if (Objects.isNull(sensorManager)) {
            sensorManager = Optional.ofNullable(getContext())
                    .map(context -> (SensorManager)
                            context.getSystemService(Context.SENSOR_SERVICE))
                    .orElse(null);
        }
        return sensorManager;
    }

    public void registerAccelerometerListener() {
        if (Objects.isNull(device)) {
            return;
        }
        SensorManager sensorManager = getSensorManager();
        if (Objects.nonNull(sensorManager)) {
            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (Objects.nonNull(senAccelerometer)
                    && device.isAccelerometerEnabled()
                    && PreferenceUtils.getAccelerometerEnabled(getContext())
            ) {
                sensorManager.registerListener(
                        device, senAccelerometer, SwitchController.SAMPLING_INTERVAL);
            }
        }
    }

    public void unregisterAccelerometerListener() {
        if (Objects.isNull(device)) {
            return;
        }
        SensorManager sensorManager = getSensorManager();
        if (Objects.nonNull(sensorManager)) {
            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (Objects.nonNull(senAccelerometer) && !device.isAccelerometerEnabled()) {
                sensorManager.unregisterListener(device, senAccelerometer);
            }
        }
    }

    public void registerGyroscopeListener() {
        if (Objects.isNull(device)) {
            return;
        }
        SensorManager sensorManager = getSensorManager();
        if (Objects.nonNull(sensorManager)) {
            senGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (Objects.nonNull(senGyroscope)
                    && device.isGyroscopeEnabled()
                    && PreferenceUtils.getGyroscopeEnabled(getContext())
            ) {
                sensorManager.registerListener(
                        device, senGyroscope, SwitchController.SAMPLING_INTERVAL);
            }
        }
    }

    public void unregisterGyroscopeListener() {
        if (Objects.isNull(device)) {
            return;
        }
        SensorManager sensorManager = getSensorManager();
        if (Objects.nonNull(sensorManager)) {
            senGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (Objects.nonNull(senGyroscope) && !device.isGyroscopeEnabled()) {
                sensorManager.unregisterListener(device, senGyroscope);
            }
        }
    }

    public void showAmiiboPicker() {
    }

    protected void vibrate(VibrationPattern vibrationPattern) {
        getVibrator().ifPresent(v -> v.vibrate(vibrationPattern.getVibrationEffect()));
    }

    protected void openFileSelectionDialog() {
        openFileSelectionDialog(true);
    }

    protected void openFileSelectionDialog(boolean binaryOnly) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(binaryOnly ? "application/octet-stream" : "*/*");

        startActivityForResult(intent, REQUEST_SELECT_FILE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Context context = getContext();
                Uri uri = data.getData();
                try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    PreferenceUtils.setAmiiboFileName(context, uri);
                    device.setAmiiboBytes(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public abstract void setPlayerLights(
            LedState led1, LedState led2, LedState led3, LedState led4);

}
