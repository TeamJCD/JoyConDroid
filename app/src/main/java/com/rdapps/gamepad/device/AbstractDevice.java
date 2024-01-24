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

    private boolean accelerometerEnabled;
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

    public abstract void onGetReport(BluetoothDevice rDevice, byte type, byte id, int bufferSize);

    public abstract void onSetReport(BluetoothDevice rDevice, byte type, byte id, byte[] data);

    public abstract void onSetProtocol(BluetoothDevice rDevice, byte protocol);

    public abstract void onInterruptData(BluetoothDevice rDevice, byte reportId, byte[] data);

    public abstract void connectingDevice(BluetoothDevice pluggingDevice);

    public abstract void stop();

    public void setAccelerometerEnabled(boolean enabled) {
        this.accelerometerEnabled = enabled;
    }

    public void setGyroscopeEnabled(boolean enabled) {
        this.gyroscopeEnabled = enabled;
    }

    public void setMotionControlsEnabled(boolean enabled) {
        setAccelerometerEnabled(enabled);
        setGyroscopeEnabled(enabled);
    }

    public boolean isAccelerometerEnabled() {
        return accelerometerEnabled;
    }

    public boolean isGyroscopeEnabled() {
        return gyroscopeEnabled;
    }

    public boolean isMotionControlsEnabled() {
        return isAccelerometerEnabled() && isGyroscopeEnabled();
    }
}
