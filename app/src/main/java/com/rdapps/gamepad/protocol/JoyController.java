package com.rdapps.gamepad.protocol;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.google.android.gms.common.util.Hex;
import com.rdapps.gamepad.BuildConfig;
import com.rdapps.gamepad.amiibo.AmiiboConfig;
import com.rdapps.gamepad.button.AxisEnum;
import com.rdapps.gamepad.button.ButtonEnum;
import com.rdapps.gamepad.button.ButtonState;
import com.rdapps.gamepad.command.handler.InputHandler;
import com.rdapps.gamepad.command.handler.OutputHandler;
import com.rdapps.gamepad.device.AbstractDevice;
import com.rdapps.gamepad.led.LedState;
import com.rdapps.gamepad.log.JoyConLog;
import com.rdapps.gamepad.memory.ControllerMemory;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;
import com.rdapps.gamepad.sensor.AccelerometerEvent;
import com.rdapps.gamepad.sensor.GyroscopeEvent;
import com.rdapps.gamepad.util.ByteUtils;
import com.rdapps.gamepad.util.ThreadUtil;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.Setter;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.toast.ToastHelper.missingPermission;

public class JoyController extends AbstractDevice {
    private static final String TAG = JoyController.class.getName();

    @Getter
    private final ControllerType controllerType;

    //Memory
    @Getter
    private final ControllerMemory controllerMemory;

    //Button State
    @Getter
    private final ButtonState buttonState;

    //Amiibo
    @Getter
    private final AmiiboConfig amiiboConfig;

    //Executor Service
    @Getter
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledFuture;

    //Config
    private final JoyControllerConfig controllerConfig;

    //Output Handler
    @Setter
    @Getter
    private OutputHandler outputHandler;

    //Input Handler
    @Setter
    @Getter
    private InputHandler inputHandler;

    //State
    @Getter
    private final JoyControllerState state;

    @Getter
    private final Queue<AccelerometerEvent> accelerometerEvents = new LinkedBlockingQueue<>();
    @Getter
    private final Queue<GyroscopeEvent> gyroscopeEvents = new LinkedBlockingQueue<>();

    @Getter
    @Setter
    private Callback callbackFunction;

    @Getter
    @Setter
    private JoyControllerListener listener;

    private final AtomicBoolean isInFullMode;

    JoyController(
            Context context,
            ControllerType controllerType,
            ControllerMemory controllerMemory,
            ButtonState buttonState,
            AmiiboConfig amiiboConfig,
            ScheduledExecutorService executorService,
            JoyControllerConfig controllerConfig,
            JoyControllerState state,
            JoyControllerListener listener) {
        super(
                context,
                controllerType.getBTName(),
                controllerType.getSubClass(),
                controllerType.getHidName(),
                controllerType.getHidDescription(),
                controllerType.getHidProvider(),
                controllerType.getDescriptor()
        );

        this.controllerType = controllerType;
        this.controllerMemory = controllerMemory;
        this.buttonState = buttonState;
        this.amiiboConfig = amiiboConfig;
        this.executorService = executorService;
        this.controllerConfig = controllerConfig;
        this.state = state;
        this.listener = listener;
        this.isInFullMode = new AtomicBoolean(false);
    }

    public void setButton(ButtonEnum button, int value) {
        this.buttonState.setButton(button, value);
    }

    public int getButton(ButtonEnum button) {
        return this.buttonState.getButton(button);
    }

    public void setAxis(AxisEnum axis, int value) {
        this.buttonState.setAxis(axis, value);
    }

    public int getAxis(AxisEnum axis) {
        return this.buttonState.getAxis(axis);
    }

    public void setAmiiboBytes(byte[] bytes) {
        amiiboConfig.setAmiiboBytes(bytes);
    }

    @Override
    public void setRemoteDevice(BluetoothDevice pluggedDevice) {
        super.setRemoteDevice(pluggedDevice);
//        TODO Is handshake needed?
        if (Objects.nonNull(pluggedDevice)) {
            log(TAG, "Handshake sent.");
            startHandShake();
        } else {
            log(TAG, "Reconnect.");
        }
    }

    private void startHandShake() {
        try {
            if (!executorService.isShutdown()) {
                executorService.schedule(
                        inputHandler::sendHandShake,
                        controllerConfig.getWaitBeforeHandshakeMs(),
                        TimeUnit.MILLISECONDS);
            }
        } catch (RejectedExecutionException e) {
            JoyConLog.log(TAG, "Executor Rejected", e);
        }
    }

    public synchronized void startFullReportMode() {
        stopFullReportMode();
        isInFullMode.set(true);
        executorService.execute(() -> {
            do {
                long startTime = System.nanoTime();
                boolean result = inputHandler.sendFullReport();
                log(TAG, "Result: " + result);
                ControllerType controllerType = getControllerType();
                long delay = getDelay();
                long endTime = System.nanoTime();
                long wait = ((startTime + delay) - endTime)/ 1000_000L;
                if (wait > 0) {
                    ThreadUtil.safeSleep((int) wait);
                }
            } while (isInFullMode.get());
        });
    }

    private long getDelay() {
        long delay = 1_000_000_000 / controllerConfig.getPacketRate();
        JoyConLog.log(TAG, "Delay: " + delay);
        return delay;
    }

    public synchronized void stopFullReportMode() {
        isInFullMode.set(false);
        if (Objects.nonNull(scheduledFuture) && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
        }
    }

    public void setPlayerLights(byte lightsByte) {
        state.setPlayerLights(lightsByte);
        if (Objects.nonNull(listener)) {
            LedState[] leds = new LedState[]{
                    LedState.OFF,
                    LedState.OFF,
                    LedState.OFF,
                    LedState.OFF
            };

            int i = 0;
            while (lightsByte > 0) {
                if ((lightsByte & 1) == 1) {
                    leds[i % 4] = (i / 4 == 0) ? LedState.ON : LedState.BLINK;
                }
                i ++;
                lightsByte = (byte)(lightsByte >>> 1);
            }

            listener.setPlayerLights(leds[0], leds[1], leds[2], leds[3]);
        }
    }

    public void set6AxisSensorEnabled(boolean enabled) {
        state.setAxisSensorEnabled(enabled);
        //TODO Enable 6 Axis Sensor
    }

    public void setVibrationEnabled(boolean enabled) {
        state.setVibrationEnabled(enabled);
        //TODO Enable Vibration
    }

    @Override
    public void onGetReport(BluetoothDevice rDevice, byte type, byte id, int bufferSize) {
        log(TAG, "Get Report Type: " + type + " Id: " + id + " bufferSize: " + bufferSize, true);
    }

    @Override
    public void onSetReport(BluetoothDevice rDevice, byte type, byte id, byte[] data) {
        log(TAG, "Set Report Type: " + type + " Id: " + id + " data: " + Hex.bytesToStringUppercase(data), true);
    }

    @Override
    public void onSetProtocol(BluetoothDevice rDevice, byte protocol) {
        log(TAG, "Set Protocol Protocol: " + ByteUtils.encodeHexString(protocol), true);
    }

    @Override
    public void onInterruptData(BluetoothDevice rDevice, byte reportId, byte[] data) {
        //log(TAG, "Interrupt Data Report ID: " + ByteUtils.encodeHexString(reportId) + " data: " + Hex.bytesToStringUppercase(data));
        OutputReport outputReport = new OutputReport(reportId, data);
        if (ByteUtils.asList(BuildConfig.DEBUG_OUTPUT).contains(outputReport.getReportId())) {
            log(TAG, outputReport.toString());
        }
        outputHandler.handleOutputReport(outputReport);
    }

    @Override
    public void connectingDevice(BluetoothDevice pluggingDevice) {

    }

    @Override
    public void stop() {
        stopFullReportMode();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (Objects.nonNull(event) && Objects.nonNull(event.sensor)) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isAccelerometerEnabled()) {
                accelerometerEvents.add(AccelerometerEvent.createFromSensorEvent(event));
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && isGyroscopeEnabled()) {
                gyroscopeEvents.add(GyroscopeEvent.createFromSensorEvent(event));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean sendReport(InputReport report) {
        if (ByteUtils.asList(BuildConfig.DEBUG_INPUT).contains(report.getReportId())) {
            log(TAG, report.toString());
        }
        BluetoothHidDevice proxy = getProxy();
        BluetoothDevice remoteDevice = getRemoteDevice();
        if (Objects.nonNull(proxy) && Objects.nonNull(remoteDevice)) {
            try {
                return proxy.sendReport(remoteDevice, report.getReportId(), report.build());
            } catch (SecurityException ex) {
                missingPermission(context, Manifest.permission.BLUETOOTH_CONNECT);
                log(TAG, "Missing permission", ex);
            }
        } else {
            log(TAG, "Could not send Report: " + report.toString());
            stopFullReportMode();
        }
        return false;
    }

    public void showAmiiboPicker() {
        if (Objects.nonNull(listener)) {
            listener.showAmiiboPicker();
        }
    }

    public boolean isInFullMode() {
        return isInFullMode.get();
    }
}
