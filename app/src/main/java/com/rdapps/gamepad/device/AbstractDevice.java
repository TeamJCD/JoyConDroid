package com.rdapps.gamepad.device;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.content.Context;
import android.hardware.SensorEventListener;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractDevice implements SensorEventListener {
    protected Context context;

    private String btName;
    private byte subclass;
    private String hidName;
    private String hidDescription;
    private String hidProvider;
    private String hidDescriptor;

    private String localMacAddress;
    private BluetoothDevice remoteDevice;

    private BluetoothHidDevice proxy;

    @Setter
    @Getter
    private boolean accelerometerEnabled;
    @Setter
    @Getter
    private boolean gyroscopeEnabled;

    public AbstractDevice(
            Context context,
            String btName,
            byte subclass,
            String hidName,
            String hidDescription,
            String hidProvider,
            String hidDescriptor) {
        this.context = context;

        this.btName = btName;
        this.subclass = subclass;
        this.hidName = hidName;
        this.hidDescription = hidDescription;
        this.hidProvider = hidProvider;
        this.hidDescriptor = hidDescriptor;

        this.localMacAddress = null;
        this.remoteDevice = null;

        this.proxy = null;

        this.accelerometerEnabled = true;
        this.gyroscopeEnabled = true;
    }

    public boolean isConnected() {
        return remoteDevice != null;
    }

    public abstract void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize);

    public abstract void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data);

    public abstract void onSetProtocol(BluetoothDevice device, byte protocol);

    public abstract void onInterruptData(BluetoothDevice device, byte reportId, byte[] data);

    public abstract void connectingDevice(BluetoothDevice pluggingDevice);

    public abstract void stop();

    public void setMotionControlsEnabled(boolean enabled) {
        setAccelerometerEnabled(enabled);
        setGyroscopeEnabled(enabled);
    }

    public boolean isMotionControlsEnabled() {
        return isAccelerometerEnabled() && isGyroscopeEnabled();
    }
}
