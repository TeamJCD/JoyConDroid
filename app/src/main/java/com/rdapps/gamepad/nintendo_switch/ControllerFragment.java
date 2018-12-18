package com.rdapps.gamepad.nintendo_switch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Vibrator;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.erz.joysticklibrary.JoyStick;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.device.ButtonType;
import com.rdapps.gamepad.device.JoystickType;
import com.rdapps.gamepad.led.LedState;
import com.rdapps.gamepad.model.ControllerAction;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.util.Pair;
import com.rdapps.gamepad.util.PreferenceUtils;
import com.rdapps.gamepad.vibrator.VibrationPattern;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

public abstract class ControllerFragment extends Fragment {

    private Context context;
    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyroscope;

    protected JoyController device;

    private Map<Integer, ButtonType> buttonMap;
    private Map<Pair<Integer, Integer>, ButtonType> axisMap;
    private Map<JoystickType, ControllerAction> joystickMap;

    protected FilePickerDialog dialog;
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


    public void setDevice(JoyController device) {
        this.device = device;
    }

    public abstract Button getA();

    public abstract Button getB();

    public abstract Button getX();

    public abstract Button getY();

    public abstract Button getSL();

    public abstract Button getSR();

    public abstract Button getL();

    public abstract Button getR();

    public abstract Button getZL();

    public abstract Button getZR();

    public abstract Button getMinus();

    public abstract Button getPlus();

    public abstract Button getHome();

    public abstract Button getCapture();

    public abstract Button getLeft();

    public abstract Button getRight();

    public abstract Button getUp();

    public abstract Button getDown();

    public abstract Button getSync();

    public abstract JoyStick getLeftJoyStick();

    public abstract JoyStick getRightJoyStick();

    public abstract boolean setLeftStickPress(boolean pressed);

    public abstract boolean setRightStickPress(boolean pressed);

    public abstract boolean reverseJoyStickXY();

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
        switch (buttonType) {
            case LEFT:
                return dispatchEvent(getLeft(), event);
            case RIGHT:
                return dispatchEvent(getRight(), event);
            case UP:
                return dispatchEvent(getUp(), event);
            case DOWN:
                return dispatchEvent(getDown(), event);
            case B:
                return dispatchEvent(getB(), event);
            case A:
                return dispatchEvent(getA(), event);
            case Y:
                return dispatchEvent(getY(), event);
            case X:
                return dispatchEvent(getX(), event);
            case R:
                return dispatchEvent(getR(), event);
            case ZR:
                return dispatchEvent(getZR(), event);
            case RIGHT_SR:
            case LEFT_SR:
                return dispatchEvent(getSR(), event);
            case L:
                return dispatchEvent(getL(), event);
            case ZL:
                return dispatchEvent(getZL(), event);
            case RIGHT_SL:
            case LEFT_SL:
                return dispatchEvent(getSL(), event);
            case PLUS:
                return dispatchEvent(getPlus(), event);
            case MINUS:
                return dispatchEvent(getMinus(), event);
            case HOME:
                return dispatchEvent(getHome(), event);
            case CAPTURE:
                return dispatchEvent(getCapture(), event);
            case LEFT_STICK:
                return setLeftStickPress(keyEvent.getAction() == KeyEvent.ACTION_DOWN);
            case RIGHT_STICK:
                return setRightStickPress(keyEvent.getAction() == KeyEvent.ACTION_DOWN);
            case SYNC:
                return dispatchEvent(getSync(), event);
            default:
                return false;
        }
    }

    private static boolean dispatchEvent(Button button, MotionEvent event) {
        return Optional.ofNullable(button)
                .map(b -> b.dispatchTouchEvent(event))
                .orElse(false);
    }

    public boolean handleGenericMotionEvent(MotionEvent motionEvent) {
        if (motionEvent == null) {
            return false;
        }

        InputDevice device = motionEvent.getDevice();
        boolean reverse = reverseJoyStickXY();
        Map<JoystickType, ControllerAction> joystickMap = getJoystickMap();
        ControllerAction rightJoystickAction = joystickMap.get(JoystickType.RIGHT_JOYSTICK);
        ControllerAction leftJoystickAction = joystickMap.get(JoystickType.LEFT_JOYSTICK);

        float rightStickX = 0;
        float rightStickY = 0;
        if (rightJoystickAction != null) {
            rightStickX = (reverse ? -1 : 1) * getCenteredAxis(motionEvent, device, reverse ? rightJoystickAction.getYAxis() : rightJoystickAction.getXAxis());
            rightStickY = getCenteredAxis(motionEvent, device, reverse ? rightJoystickAction.getXAxis() : rightJoystickAction.getYAxis());
            rightStickX = rightStickX * rightJoystickAction.getXDirection();
            rightStickY = rightStickY * rightJoystickAction.getYDirection() * -1;
        }

        float leftStickX = 0;
        float leftStickY = 0;
        if (leftJoystickAction != null) {
            leftStickX = (reverse ? -1 : 1) * getCenteredAxis(motionEvent, device, reverse ? leftJoystickAction.getYAxis() : leftJoystickAction.getXAxis());
            leftStickY = getCenteredAxis(motionEvent, device, reverse ? leftJoystickAction.getXAxis() : leftJoystickAction.getYAxis());
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

        boolean processed = Float.compare(leftStickX, prevLeftX) != 0 ||
                Float.compare(leftStickY, prevLeftY) != 0 ||
                Float.compare(rightStickX, prevRightX) != 0 ||
                Float.compare(rightStickY, prevRightY) != 0;

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

    private int prevXKeyCode = -1;
    private int prevYKeyCode = -1;

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

        if (prevXKeyCode != keyCode && prevXKeyCode != -1) {
            handleKey(prevXKeyCode, new KeyEvent(KeyEvent.ACTION_UP, prevXKeyCode));
        }
        prevXKeyCode = keyCode;
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

        if (prevYKeyCode != keyCode && prevYKeyCode != -1) {
            handleKey(prevYKeyCode, new KeyEvent(KeyEvent.ACTION_UP, prevYKeyCode));
        }
        prevYKeyCode = keyCode;
        if (keyCode != -1) {
            handleKey(keyCode, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        }
    }

    public SensorManager getSensorManager() {
        if (Objects.isNull(sensorManager)) {
            sensorManager = Optional.ofNullable(getContext())
                    .map(context -> (SensorManager) context.getSystemService(Context.SENSOR_SERVICE))
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
                sensorManager.registerListener(device, senAccelerometer, SwitchController.SAMPLING_INTERVAL);
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
                sensorManager.registerListener(device, senGyroscope, SwitchController.SAMPLING_INTERVAL);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (dialog == null) {
                        openFileSelectionDialog();
                    }
                } else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(getContext(), getText(R.string.file_permission_is_required), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    protected void vibrate(VibrationPattern vibrationPattern) {
        getVibrator().ifPresent(v -> {
            v.vibrate(vibrationPattern.getVibrationEffect());
        });
    }

    protected void openFileSelectionDialog() {
        Context context = getContext();
        FragmentActivity activity = getActivity();
        if ((null != dialog && dialog.isShowing()) ||
                context == null ||
                activity == null ||
                activity.isFinishing()
        ) {
            //dialog.dismiss();
            return;
        }

        //Create a DialogProperties object.
        DialogProperties properties = new DialogProperties();
        String amiiboFilePath = PreferenceUtils.getAmiiboFilePath(context);
        if (Objects.nonNull(amiiboFilePath)) {
            File file = new File(amiiboFilePath);
            File folder = file.getParentFile();
            if (folder.exists() && folder.isDirectory()) {
                properties.root = folder;
            }
        } else {
            properties.root = Environment.getExternalStorageDirectory();
        }


        //Instantiate FilePickerDialog with Context and DialogProperties.
        dialog = new FilePickerDialog(getContext(), properties);
        dialog.setTitle("Select a File");
        dialog.setPositiveBtnName("Select");
        dialog.setNegativeBtnName("Cancel");
        //properties.selection_mode = DialogConfigs.MULTI_MODE; // for multiple files
        properties.selection_mode = DialogConfigs.SINGLE_MODE; // for single file
        properties.selection_type = DialogConfigs.FILE_SELECT;

        //Method handle selected files.
        dialog.setDialogSelectionListener(this::onSelectedFilePaths);
        dialog.setOnCancelListener(this::onFileSelectorCanceled);
        dialog.setOnDismissListener(this::onFileSelectorDismissed);

        dialog.show();
    }

    public void onSelectedFilePaths(String[] files) {
        if (files.length > 0 || Objects.nonNull(device)) {
            String file = files[0];
            try {
                byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
                PreferenceUtils.setAmiiboFilePath(context, file);
                device.setAmiiboBytes(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onFileSelectorCanceled(DialogInterface dialog) {
        this.dialog = null;
    }

    public void onFileSelectorDismissed(DialogInterface dialog) {
        this.dialog = null;
    }

    public abstract void setPlayerLights(LedState led1, LedState led2, LedState led3, LedState led4);

}
