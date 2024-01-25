package com.rdapps.gamepad.protocol;

import com.google.android.gms.common.util.Hex;
import com.rdapps.gamepad.battery.BatteryData;
import com.rdapps.gamepad.memory.ControllerMemory;
import com.rdapps.gamepad.memory.SPIMemory;
import com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu;
import com.rdapps.gamepad.report.InputReportMode;
import com.rdapps.gamepad.vibrator.VibratorData;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.report.InputReportMode.NFC_IR_MODE;
import static com.rdapps.gamepad.report.InputReportMode.SIMPLE_HID;
import static com.rdapps.gamepad.util.ByteUtils.toShort;
import static java.lang.Math.PI;

public class JoyControllerState {
    private static final String TAG = JoyControllerState.class.getName();

    private static final double G = 9.8f;

    private final AtomicInteger timeByte = new AtomicInteger(0);

    @Getter
    private InputReportMode inputReportMode = SIMPLE_HID;

    @Getter
    private final BatteryData batteryData = new BatteryData(true, (float) 1.0);

    @Setter
    @Getter
    private byte playerLights = 0;

    @Setter
    @Getter
    private boolean axisSensorEnabled = false;

    @Setter
    @Getter
    private boolean vibrationEnabled = false;

    /**
     * TODO implement this
     */
    @Setter
    @Getter
    private VibratorData vibratorData = new VibratorData();

    @Setter
    @Getter
    private byte[] macBytes;

    @Setter
    @Getter
    private NfcIrMcu nfcIrMcu = new NfcIrMcu();

    @Getter
    private double[] accCoeffs;
    @Getter
    private int[] accOffset;
    @Getter
    private double[] gyrCoeffs;
    @Getter
    private short[] gyrOffset;

    public JoyControllerState(byte[] macBytes) {
        this.macBytes = macBytes;
    }

    public void calculateCoeffs(SPIMemory memory) {
        accCoeffs = new double[3];
        gyrCoeffs = new double[3];
        accOffset = new int[3];
        gyrOffset = new short[3];

        byte[] motionCalibration = memory.read(0x8026, 26);
        if (toShort(motionCalibration, 0) != (short) 0xB2A1) {
            motionCalibration = memory.read(0x601E, 26);
        }

        log(TAG, "Motion Calibration: " + Hex.bytesToStringUppercase(motionCalibration));

        for (int i = 0; i < 3; i++) {
            accOffset[i] = toShort(motionCalibration, i * 2 + 2);
            int calAccCoef = toShort(motionCalibration, i * 2 + 8);
            accCoeffs[i] = ((calAccCoef - accOffset[i]) / (4.0 * G));
            log(TAG, "ACC Calibration: " + i + ": " + accCoeffs[i]);

            gyrOffset[i] = toShort(motionCalibration, i * 2 + 14);
            int calGyrCoef = toShort(motionCalibration, i * 2 + 20);
            //https://github.com/CTCaer/jc_toolkit/blob/master/jctool/jctool.cpp#L975
            // however it is scale by 10 weeeeird
            gyrCoeffs[i] = (((calGyrCoef - gyrOffset[i]) * 4 * PI) / (0.01745329251994 * 936.0));
            log(TAG, "Gyro Calibration: " + i + ": " + gyrCoeffs[i]);
        }

    }

    /**
     * System time in milliseconds
     *
     * @return millis
     */
    public long getTime() {
        return timeByte.incrementAndGet();
    }

    public void setCharging(boolean charging) {
        this.batteryData.setCharging(charging);
    }

    public void setBatteryLevel(float level) {
        this.batteryData.setBatteryLevel(level);
    }

    public void setInputReportMode(InputReportMode inputReportMode) {
        this.inputReportMode = inputReportMode;
        if (inputReportMode != NFC_IR_MODE) {
            nfcIrMcu.setMcuState(NfcIrMcu.MCUState.NOT_INITIALIZED);
            nfcIrMcu.setAction(NfcIrMcu.Action.NON);
        }
    }

}
