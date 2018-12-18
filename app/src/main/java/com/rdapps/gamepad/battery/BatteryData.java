package com.rdapps.gamepad.battery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatteryData {
    private volatile boolean isCharging;
    private volatile float batteryLevel;
}
