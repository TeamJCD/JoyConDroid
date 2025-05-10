package com.rdapps.gamepad.service;

import static android.bluetooth.BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED;
import static android.bluetooth.BluetoothProfile.HID_DEVICE;
import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.protocol.ControllerType.LEFT_JOYCON;
import static com.rdapps.gamepad.protocol.ControllerType.PRO_CONTROLLER;
import static com.rdapps.gamepad.toast.ToastHelper.bluetoothNotAvailable;
import static com.rdapps.gamepad.toast.ToastHelper.cannotSetBluetoothName;
import static com.rdapps.gamepad.toast.ToastHelper.couldNotRegisterApp;
import static com.rdapps.gamepad.toast.ToastHelper.deviceConnected;
import static com.rdapps.gamepad.toast.ToastHelper.deviceDisconnected;
import static com.rdapps.gamepad.toast.ToastHelper.deviceIsNotCompatible;
import static com.rdapps.gamepad.toast.ToastHelper.missingPermission;
import static com.rdapps.gamepad.toast.ToastHelper.sdpFailed;
import static com.rdapps.gamepad.util.ByteUtils.hexStringToByteArray;
import static com.rdapps.gamepad.util.PreferenceUtils.MAC_FAKE_ADDRESS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import com.rdapps.gamepad.ControllerActivity;
import com.rdapps.gamepad.R;
import com.rdapps.gamepad.led.LedState;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerBuilder;
import com.rdapps.gamepad.protocol.JoyControllerListener;
import com.rdapps.gamepad.util.PreferenceUtils;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.SneakyThrows;

public class BluetoothControllerService extends Service implements BluetoothProfile.ServiceListener,
        JoyControllerListener {

    private static final int HID_PROFILE_TIME_OUT_SECONDS = 10;

    public static final String DEVICE_TYPE = "DEVICE_TYPE";
    public static final String NINTENDO_SWITCH = "Nintendo Switch";

    private static final String INTENT_DISCONNECT = "INTENT_DISCONNECT";


    private static final int NOTIFICATION_ID = 1332;
    private static final String TAG = "BluetoothControllerService";

    private static final int QOS_TOKEN_RATE = 21720; // 362 bytes * 1000000 us / 16667 us
    private static final int QOS_TOKEN_BUCKET_SIZE = 362;
    private static final int QOS_PEAK_BANDWIDTH = 21720;
    private static final int QOS_LATENCY = 16667;
    private static final int QOS_DELAY_VARIATION = 16667;

    private Handler mainHandler;

    private final Object registerLock = new Object();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHidDevice bluetoothHidDevice;
    private ExecutorService bluetoothHidExecutor;

    private ScheduledExecutorService timeoutScheduler;
    private ScheduledFuture<?> hidFuture;

    State state;
    boolean appRegistered;
    boolean serviceConnected;
    boolean deviceConnected;
    ControllerType controllerType;

    ControllerActivity controllerActivity;
    JoyController switchController;


    private Optional<BluetoothDevice> getConnectedNintendoSwitch() {
        return Optional.ofNullable(bluetoothAdapter)
                .map(BluetoothAdapter::getBondedDevices)
                .map(Set::stream)
                .map(stream -> stream.filter(device -> {
                    try {
                        return NINTENDO_SWITCH.equalsIgnoreCase(device.getName());
                    } catch (SecurityException ex) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            missingPermission(getApplicationContext(),
                                    Manifest.permission.BLUETOOTH_CONNECT);
                            log(TAG, "Missing permission", ex);
                        }
                        return false;
                    }
                }))
                .flatMap(Stream::findFirst);
    }

    private boolean setBluetoothAdapterName(String deviceName) {
        try {
            String name = bluetoothAdapter.getName();
            if (!Objects.equals(name, deviceName)) {
                PreferenceUtils.saveOriginalName(getApplicationContext(), name);
                boolean setNameSuccess = bluetoothAdapter.setName(deviceName);
                if (!setNameSuccess) {
                    mainHandler.post(() ->
                            cannotSetBluetoothName(getApplicationContext(), deviceName));
                    return false;
                }
            }
            return true;
        } catch (SecurityException ex) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                missingPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT);
                log(TAG, "Missing permission", ex);
            }
            return false;
        }
    }

    private boolean revertBluetoothAdapterName() {
        Optional<String> originalNameOpt = PreferenceUtils.getOriginalName(getApplicationContext());
        if (originalNameOpt.isPresent()) {
            try {
                if (bluetoothAdapter.setName(originalNameOpt.get())) {
                    PreferenceUtils.removeOriginalName(getApplicationContext());
                    return true;
                }
            } catch (SecurityException ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    missingPermission(getApplicationContext(),
                            Manifest.permission.BLUETOOTH_CONNECT);
                    log(TAG, "Missing permission", ex);
                }
            }
        }
        return false;
    }

    @SuppressLint("HardwareIds")
    private String getBluetoothMacAddress() {
        String address = bluetoothAdapter.getAddress();

        if (Objects.isNull(address)) {
            address = PreferenceUtils.getBluetoothAddress(getApplicationContext());
        }

        if (MAC_FAKE_ADDRESS.equalsIgnoreCase(address)) {
            address = randomMacAddress();
        }

        return address;
    }

    private String randomMacAddress() {
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);

        //zeroing last 2 bytes to make it unicast and locally adminstrated
        macAddr[0] = (byte) (macAddr[0] & (byte) 254);

        StringBuilder sb = new StringBuilder(18);
        for (byte b : macAddr) {

            if (sb.length() > 0) {
                sb.append(":");
            }

            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class<?>[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            log(TAG, "Unpair Failed: ", e);
        }
    }

    public boolean isConnected() {
        return deviceConnected;
    }

    public void setControllerActivity(ControllerActivity controllerActivity) {
        this.controllerActivity = controllerActivity;

        if (controllerActivity != null) {
            if (state == State.DISCOVERY) {
                controllerActivity.startHidDeviceDiscovery();
            }
        }
    }

    @SneakyThrows
    @Override
    public void onCreate() {
        log(TAG, "onCreate");

        appRegistered = false;
        serviceConnected = false;
        deviceConnected = false;
        state = State.INITIAL;
        bluetoothAdapter = getApplicationContext().getSystemService(BluetoothManager.class)
                .getAdapter();

        //this.getClass().getMethod("setBluetoothClass", new Class[]{}).invoke(this);
        bluetoothHidExecutor = Executors.newCachedThreadPool();
        bluetoothHidDevice = null;
        timeoutScheduler = Executors.newScheduledThreadPool(1);
        mainHandler = new Handler(getApplicationContext().getMainLooper());

        if (Objects.isNull(bluetoothAdapter)) {
            bluetoothNotAvailable(getApplicationContext());
        } else {
            bluetoothAdapter.getProfileProxy(
                    getApplicationContext(),
                    this,
                    HID_DEVICE
            );
            hidFuture = timeoutScheduler.schedule(this::checkHidProfile,
                    HID_PROFILE_TIME_OUT_SECONDS, TimeUnit.SECONDS);
        }

        startForeground();
    }

    private void checkHidProfile() {
        try {
            if (Objects.isNull(bluetoothHidDevice)) {
                mainHandler.post(() -> {
                    try {
                        if (controllerActivity != null) {
                            controllerActivity.onDeviceNotCompatible();
                        }
                        deviceIsNotCompatible(getApplicationContext());
                    } catch (Exception e) {
                        log(TAG, "HID Not Found Check Internal", e);
                    }
                });
            }
        } catch (Exception e) {
            log(TAG, "HID Not Found Check", e);
        }
    }

    private void cancelHidCheck() {
        try {
            if (Objects.nonNull(timeoutScheduler)) {
                hidFuture.cancel(true);
                timeoutScheduler.shutdownNow();
            }
        } catch (Exception e) {
            log(TAG, "Cancelling HID Check", e);
        }
    }

    private void startForeground() {
        startForeground(NOTIFICATION_ID, createNotification(LEFT_JOYCON));
    }

    private Notification createNotification(ControllerType type) {
        String channelId =
                createNotificationChannel("joycon_droid", "JoyCon Droid");

        Intent closeIntent = new Intent(getApplicationContext(), BluetoothControllerService.class);
        closeIntent.setAction(INTENT_DISCONNECT);

        String contentTitle = getString(R.string.left_joycon);
        int smallIcon = R.drawable.ic_left_joycon_icon_notification;

        switch (type) {
            case RIGHT_JOYCON:
                contentTitle = getString(R.string.right_joycon);
                smallIcon = R.drawable.ic_right_joycon_icon_notification;
                break;
            case PRO_CONTROLLER:
                contentTitle = getString(R.string.pro_controller);
                smallIcon = R.drawable.ic_procontroller_icon_notification;
                break;
            default:
                break;
        }

        Notification.Builder notificationBuilder = new Notification.Builder(this, channelId);
        return notificationBuilder.setOngoing(true)
                .setSmallIcon(smallIcon)
                .setContentTitle(contentTitle)
                .setContentText(getText(R.string.disconnect_controller))
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(PendingIntent.getService(getApplicationContext(), 0, closeIntent,
                        PendingIntent.FLAG_IMMUTABLE))
                .build();
    }

    private void setNotification(ControllerType type) {
        startForeground(NOTIFICATION_ID, createNotification(type));
    }

    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log(TAG, "onStartCommand");
        ControllerType type = controllerType;

        if (Objects.nonNull(intent)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                type = intent.getSerializableExtra(DEVICE_TYPE, ControllerType.class);
            } else {
                @SuppressWarnings("deprecation")
                ControllerType deprecatedType =
                        (ControllerType) intent.getSerializableExtra(DEVICE_TYPE);
                type = deprecatedType;
            }

            if (intent.getAction() == INTENT_DISCONNECT) {
                state = State.DESTROYING;
                if (Objects.nonNull(controllerActivity)) {
                    controllerActivity.finish();
                }
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        if (Objects.nonNull(switchController) && deviceConnected && type != null
                && controllerType == type) {
            return START_STICKY;
        }

        controllerType = type;

        if (Objects.isNull(controllerType)) {
            controllerType = PRO_CONTROLLER;
        }

        setNotification(controllerType);
        if (Objects.nonNull(switchController)) {
            switchController.stop();
        }

        Optional<BluetoothDevice> nintendoSwitch = getConnectedNintendoSwitch();
        nintendoSwitch.ifPresent(this::connect);

        synchronized (registerLock) {
            state = State.STARTING;
            Context context = getApplicationContext();
            switchController = JoyControllerBuilder.with(context)
                    .setLocalMacAddress(getBluetoothMacAddress())
                    .setType(controllerType)
                    .setListener(this)
                    .build();

            if (Objects.nonNull(bluetoothHidDevice)) {
                if (!appRegistered) {
                    registerApp();
                } else {
                    unregisterApp();
                }
            }
        }

        return START_STICKY;
    }

    private void registerApp() {
        BluetoothHidDeviceAppSdpSettings bluetoothHidDeviceAppSdpSettings;
        try {
            bluetoothHidDeviceAppSdpSettings = new BluetoothHidDeviceAppSdpSettings(
                    switchController.getHidName(),
                    switchController.getHidDescription(),
                    switchController.getHidProvider(),
                    switchController.getSubclass(),
                    hexStringToByteArray(switchController.getHidDescriptor())
            );
        } catch (IllegalArgumentException e) {
            log(TAG, "Cannot set SdpSettings", e);
            mainHandler.post(() -> sdpFailed(getApplicationContext()));
            return;
        }

        BluetoothHidDeviceAppQosSettings qos = new BluetoothHidDeviceAppQosSettings(
                SERVICE_GUARANTEED,
                QOS_TOKEN_RATE,
                QOS_TOKEN_BUCKET_SIZE,
                QOS_PEAK_BANDWIDTH,
                QOS_LATENCY,
                QOS_DELAY_VARIATION
        );

        try {
            boolean isRegisterCommandSent = bluetoothHidDevice.registerApp(
                    bluetoothHidDeviceAppSdpSettings,
                    qos,
                    qos,
                    bluetoothHidExecutor,
                    new Callback()
            );

            if (!isRegisterCommandSent) {
                mainHandler.post(() -> couldNotRegisterApp(getApplicationContext()));
            }
        } catch (SecurityException ex) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                missingPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT);
                log(TAG, "Missing permission", ex);
            }
        }
    }

    private void unregisterApp() {
        try {
            bluetoothHidDevice.unregisterApp();
        } catch (SecurityException ex) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                missingPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT);
                log(TAG, "Missing permission", ex);
            }
        }
    }

    @Override
    public void onDestroy() {
        state = State.DESTROYING;

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        if (Objects.nonNull(switchController)) {
            switchController.stop();
        }
        log(TAG, "onDestroy");
        if (Objects.nonNull(bluetoothHidDevice) && Objects.nonNull(bluetoothAdapter)) {
            revertBluetoothAdapterName();
            try {
                bluetoothHidDevice.unregisterApp();
            } catch (SecurityException ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    missingPermission(getApplicationContext(),
                            Manifest.permission.BLUETOOTH_CONNECT);
                    log(TAG, "Missing permission", ex);
                }
            }
            bluetoothAdapter.closeProfileProxy(HID_DEVICE, bluetoothHidDevice);
        }
    }

    private final IBinder binder = new BluetoothControllerServiceBinder();

    public JoyController getDevice() {
        return switchController;
    }

    public void connect(BluetoothDevice device) {
        if (Objects.nonNull(bluetoothHidDevice)) {
            try {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (Objects.nonNull(controllerActivity)) {
                        controllerActivity.stopHidDeviceDiscovery();
                    }
                    boolean connect = bluetoothHidDevice.connect(device);
                    log(TAG, "Connect result: " + connect);
                } else {
                    if (Objects.nonNull(controllerActivity)) {
                        controllerActivity.startHidDeviceDiscovery();
                    }
                    device.createBond();
                }
            } catch (SecurityException ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    missingPermission(getApplicationContext(),
                            Manifest.permission.BLUETOOTH_CONNECT);
                    log(TAG, "Missing permission", ex);
                }
            }
        }
    }


    public class BluetoothControllerServiceBinder extends Binder {
        public BluetoothControllerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothControllerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        log(TAG, "Connected Profile: " + profile);
        if (profile == HID_DEVICE) {
            synchronized (registerLock) {
                cancelHidCheck();
                serviceConnected = true;
                bluetoothHidDevice = (BluetoothHidDevice) proxy;
                if (Objects.nonNull(switchController)) {
                    registerApp();
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        log(TAG, "Disconnected Profile: " + profile);
        serviceConnected = false;
    }

    private class Callback extends BluetoothHidDevice.Callback {
        private void devicePlugged(BluetoothDevice pluggedDevice) {
            switchController.setProxy(bluetoothHidDevice);
            switchController.setRemoteDevice(pluggedDevice);
            if (Objects.nonNull(controllerActivity)) {
                controllerActivity.stopHidDeviceDiscovery();
            }
        }

        private void deviceUnplugged(BluetoothDevice unpluggedDevice) {
            switchController.setProxy(null);
            switchController.setRemoteDevice(null);
            if (Objects.nonNull(controllerActivity)) {
                controllerActivity.stopHidDeviceDiscovery();
            }
        }

        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            log(TAG, "onAppStatusChanged: device=" + pluggedDevice + " state=" + state);
            appRegistered = registered;
            if (registered) {
                setBluetoothAdapterName(switchController.getBtName());
                try {
                    if (pluggedDevice == null
                            || pluggedDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        state = State.DISCOVERY;
                        if (controllerActivity != null) {
                            controllerActivity.startHidDeviceDiscovery();
                        }
                    } else {
                        if (NINTENDO_SWITCH.equalsIgnoreCase(pluggedDevice.getName())) {
                            connect(pluggedDevice);
                        }
                    }
                } catch (SecurityException ex) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        missingPermission(getApplicationContext(),
                                Manifest.permission.BLUETOOTH_CONNECT);
                        log(TAG, "Missing permission", ex);
                    }
                }
            } else if (state == State.STARTING) {
                registerApp();
            } else if (state == State.DESTROYING) {
                bluetoothHidExecutor.shutdown();
            }
        }

        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            log(TAG, "onConnectionStateChanged: device=" + device + " state=" + state);
            try {
                if (!NINTENDO_SWITCH.equalsIgnoreCase(device.getName())) {
                    return;
                }
            } catch (SecurityException ex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    missingPermission(getApplicationContext(),
                            Manifest.permission.BLUETOOTH_CONNECT);
                    log(TAG, "Missing permission", ex);
                }
                return;
            }

            if (BluetoothAdapter.STATE_CONNECTED == state) {
                deviceConnected = true;
                BluetoothControllerService.this.state = State.CONNECTED;
                devicePlugged(device);
                mainHandler.post(() -> deviceConnected(getApplicationContext()));
            } else if (BluetoothAdapter.STATE_DISCONNECTED == state) {
                deviceConnected = false;
                BluetoothControllerService.this.state = State.DISCONNECTED;
                unpairDevice(device);
                deviceUnplugged(device);
                mainHandler.post(() -> deviceDisconnected(getApplicationContext()));
            }
        }

        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            log(
                    TAG,
                    "onGetReport: device="
                            + switchController
                            + " type="
                            + type
                            + " id="
                            + id
                            + " bufferSize="
                            + bufferSize);
            if (!switchController.isConnected()) {
                switchController.setRemoteDevice(device);
            }
            switchController.onGetReport(device, type, id, bufferSize);
        }

        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            log(TAG, "onSetReport: device=" + switchController + " type=" + type + " id=" + id);
            if (!switchController.isConnected()) {
                switchController.setRemoteDevice(device);
            }
            switchController.onSetReport(device, type, id, data);
        }

        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            log(TAG, "onSetProtocol: device=" + switchController + " protocol=" + protocol);
            if (!switchController.isConnected()) {
                switchController.setRemoteDevice(device);
            }
            switchController.onSetProtocol(device, protocol);
        }

        public void onInterruptData(BluetoothDevice device, byte reportId, byte[] data) {
            if (!switchController.isConnected()) {
                switchController.setRemoteDevice(device);
            }
            switchController.onInterruptData(device, reportId, data);
        }

        public void onVirtualCableUnplug(BluetoothDevice device) {
            log(TAG, "onVirtualCableUnplug: device=" + device);
            deviceConnected = false;
            BluetoothControllerService.this.state = State.DISCONNECTED;
            //unpairDevice(device);
            deviceUnplugged(device);
            mainHandler.post(() -> deviceDisconnected(getApplicationContext()));
        }
    }

    private enum State {
        INITIAL,
        CONNECTED,
        DISCONNECTED,
        STARTING,
        DESTROYING,
        DISCOVERY
    }

    public void showAmiiboPicker() {
        if (Objects.nonNull(controllerActivity)) {
            controllerActivity.showAmiiboPicker();
        }
    }

    @Override
    public void setPlayerLights(LedState led1, LedState led2, LedState led3, LedState led4) {
        controllerActivity.setPlayerLights(led1, led2, led3, led4);
    }
}
