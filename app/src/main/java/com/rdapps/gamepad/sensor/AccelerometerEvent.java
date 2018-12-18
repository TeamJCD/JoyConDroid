package com.rdapps.gamepad.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import lombok.Data;

@Data
public class AccelerometerEvent {
    public int accuracy;
    public Sensor sensor;
    public long timestamp;
    public float[] values;

    public static AccelerometerEvent createFromSensorEvent(SensorEvent event) {
        AccelerometerEvent e = new AccelerometerEvent();
        e.accuracy = event.accuracy;
        e.sensor = event.sensor;
        e.timestamp = event.timestamp;
        e.values = event.values;
        return e;
    }

}
