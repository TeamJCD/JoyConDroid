package com.rdapps.gamepad.report;

import static androidx.core.math.MathUtils.clamp;
import static com.rdapps.gamepad.button.AxisEnum.LEFT_STICK_X;
import static com.rdapps.gamepad.button.AxisEnum.LEFT_STICK_Y;
import static com.rdapps.gamepad.button.AxisEnum.RIGHT_STICK_X;
import static com.rdapps.gamepad.button.AxisEnum.RIGHT_STICK_Y;
import static com.rdapps.gamepad.button.ButtonEnum.A;
import static com.rdapps.gamepad.button.ButtonEnum.B;
import static com.rdapps.gamepad.button.ButtonEnum.CAPTURE;
import static com.rdapps.gamepad.button.ButtonEnum.DOWN;
import static com.rdapps.gamepad.button.ButtonEnum.HOME;
import static com.rdapps.gamepad.button.ButtonEnum.L;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_SL;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_SR;
import static com.rdapps.gamepad.button.ButtonEnum.LEFT_STICK_BUTTON;
import static com.rdapps.gamepad.button.ButtonEnum.MINUS;
import static com.rdapps.gamepad.button.ButtonEnum.PLUS;
import static com.rdapps.gamepad.button.ButtonEnum.R;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT_SL;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT_SR;
import static com.rdapps.gamepad.button.ButtonEnum.RIGHT_STICK_BUTTON;
import static com.rdapps.gamepad.button.ButtonEnum.UP;
import static com.rdapps.gamepad.button.ButtonEnum.X;
import static com.rdapps.gamepad.button.ButtonEnum.Y;
import static com.rdapps.gamepad.button.ButtonEnum.ZL;
import static com.rdapps.gamepad.button.ButtonEnum.ZR;
import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.NON;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.READ_TAG;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.READ_TAG_2;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.READ_TAG_FINISHED;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.START_TAG_DISCOVERY;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.START_TAG_DISCOVERY_AUTO_MOVE;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.START_TAG_POLLING;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.WRITE_TAG_AWAITING;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.WRITE_TAG_REMOVE;
import static com.rdapps.gamepad.nx.constant.NxConstants.CAPTURE_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.DOWN_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_A_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_B_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_CAPTURE_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_DOWN_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_HOME_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_LEFT_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_LEFT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_L_R_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_MINUS_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_PLUS_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_RIGHT_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_RIGHT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_SL_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_SR_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_UP_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_X_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_Y_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.FULL_ZL_ZR_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.HOME_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.LEFT_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.LEFT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.L_R_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.MINUS_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.PLUS_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.RIGHT_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.RIGHT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.SL_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.SR_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.UP_BIT;
import static com.rdapps.gamepad.nx.constant.NxConstants.ZL_ZR_BIT;
import static com.rdapps.gamepad.protocol.ControllerType.LEFT_JOYCON;
import static com.rdapps.gamepad.protocol.ControllerType.PRO_CONTROLLER;
import static com.rdapps.gamepad.protocol.ControllerType.RIGHT_JOYCON;
import static java.lang.Math.round;
import static java.lang.Short.MAX_VALUE;
import static java.lang.Short.MIN_VALUE;

import android.os.SystemClock;
import com.google.android.gms.common.util.Hex;
import com.rdapps.gamepad.amiibo.AmiiboConfig;
import com.rdapps.gamepad.battery.BatteryData;
import com.rdapps.gamepad.button.ButtonState;
import com.rdapps.gamepad.nfcirmcu.NfcIrMcu;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.protocol.QuaternionPacker;
import com.rdapps.gamepad.sensor.AccelerometerEvent;
import com.rdapps.gamepad.sensor.GyroscopeEvent;
import com.rdapps.gamepad.util.ByteUtils;
import com.rdapps.gamepad.vibrator.VibratorData;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Getter;

@Data
public class InputReport {
    private static final String TAG = InputReport.class.getName();

    @Getter
    public enum Type {
        SIMPLE_HID_REPORT(0x3F, 11),
        STANDARD_FULL_REPORT(0x30, 48),
        SUBCOMMAND_REPLY_REPORT(0x21, 48),
        NFC_IR_REPORT(0x31, 48 + 313);

        private final byte reportId;
        private final int reportSize;

        Type(int reportId, int reportSize) {
            this.reportId = (byte) reportId;
            this.reportSize = reportSize;
        }
    }

    private byte[] buffer;
    private Type type;

    public InputReport(Type type) {
        this.type = type;
        this.buffer = new byte[type.getReportSize()];
        Arrays.fill(buffer, (byte) 0);
    }

    public void fillShortButtonReport(
            ControllerType controllerType,
            ButtonState buttonState
    ) {
        int[] buttonsArr = new int[16];
        //TODO: FIX THIS FOR PRO_CONTROLER TO INCLUDE R ZR
        if (controllerType == LEFT_JOYCON || controllerType == PRO_CONTROLLER) {
            buttonsArr[0] = buttonState.getButton(DOWN);
            buttonsArr[1] = buttonState.getButton(RIGHT);
            buttonsArr[2] = buttonState.getButton(LEFT);
            buttonsArr[3] = buttonState.getButton(UP);
            buttonsArr[4] = buttonState.getButton(LEFT_SL);
            buttonsArr[5] = buttonState.getButton(LEFT_SR);
            buttonsArr[6] = buttonState.getButton(MINUS);
            buttonsArr[7] = buttonState.getButton(PLUS);
            buttonsArr[8] = buttonState.getButton(LEFT_STICK_BUTTON);
            buttonsArr[9] = buttonState.getButton(RIGHT_STICK_BUTTON);
            buttonsArr[10] = buttonState.getButton(HOME);
            buttonsArr[11] = buttonState.getButton(CAPTURE);
            buttonsArr[12] = buttonState.getButton(L);
            buttonsArr[13] = buttonState.getButton(ZL);
            buttonsArr[14] = buttonState.getAxis(LEFT_STICK_X);
            buttonsArr[15] = buttonState.getAxis(LEFT_STICK_Y);
        } else {
            buttonsArr[0] = buttonState.getButton(B);
            buttonsArr[1] = buttonState.getButton(A);
            buttonsArr[2] = buttonState.getButton(Y);
            buttonsArr[3] = buttonState.getButton(X);
            buttonsArr[4] = buttonState.getButton(RIGHT_SL);
            buttonsArr[5] = buttonState.getButton(RIGHT_SR);
            buttonsArr[6] = buttonState.getButton(MINUS);
            buttonsArr[7] = buttonState.getButton(PLUS);
            buttonsArr[8] = buttonState.getButton(LEFT_STICK_BUTTON);
            buttonsArr[9] = buttonState.getButton(RIGHT_STICK_BUTTON);
            buttonsArr[10] = buttonState.getButton(HOME);
            buttonsArr[11] = buttonState.getButton(CAPTURE);
            buttonsArr[12] = buttonState.getButton(R);
            buttonsArr[13] = buttonState.getButton(ZR);
            buttonsArr[14] = buttonState.getAxis(RIGHT_STICK_X);
            buttonsArr[15] = buttonState.getAxis(RIGHT_STICK_Y);
        }

        int index = 0;
        buffer[index] = 0;
        buffer[index] |= buttonsArr[0] == 0 ? 0 : DOWN_BIT;
        buffer[index] |= buttonsArr[1] == 0 ? 0 : RIGHT_BIT;
        buffer[index] |= buttonsArr[2] == 0 ? 0 : LEFT_BIT;
        buffer[index] |= buttonsArr[3] == 0 ? 0 : UP_BIT;
        buffer[index] |= buttonsArr[4] == 0 ? 0 : SL_BIT;
        buffer[index] |= buttonsArr[5] == 0 ? 0 : SR_BIT;

        buffer[index + 1] = 0;
        buffer[index + 1] |= buttonsArr[6] == 0 ? 0 : MINUS_BIT;
        buffer[index + 1] |= buttonsArr[7] == 0 ? 0 : PLUS_BIT;
        buffer[index + 1] |= buttonsArr[8] == 0 ? 0 : LEFT_STICK_BIT;
        buffer[index + 1] |= buttonsArr[9] == 0 ? 0 : RIGHT_STICK_BIT;
        buffer[index + 1] |= buttonsArr[10] == 0 ? 0 : HOME_BIT;
        buffer[index + 1] |= buttonsArr[11] == 0 ? 0 : CAPTURE_BIT;
        buffer[index + 1] |= buttonsArr[12] == 0 ? 0 : L_R_BIT;
        buffer[index + 1] |= buttonsArr[13] == 0 ? 0 : ZL_ZR_BIT;


        if (buttonsArr[14] > 0) {
            if (buttonsArr[15] > 0) {
                buffer[index + 2] = 3;
            } else if (buttonsArr[15] < 0) {
                buffer[index + 2] = 1;
            } else {
                buffer[index + 2] = 2;
            }
        } else if (buttonsArr[14] < 0) {
            if (buttonsArr[15] > 0) {
                buffer[index + 2] = 5;
            } else if (buttonsArr[15] < 0) {
                buffer[index + 2] = 7;
            } else {
                buffer[index + 2] = 6;
            }
        } else {
            if (buttonsArr[15] > 0) {
                buffer[index + 2] = 4;
            } else if (buttonsArr[15] < 0) {
                buffer[index + 2] = 0;
            } else {
                buffer[index + 2] = 8;
            }
        }

        for (int i = 3; i < 11; i++) {
            byte filler = (byte) (i % 2 == 0 ? 0x80 : 0x00);
            buffer[index + i] = filler;
        }
    }

    public void fillFullButtonReport(
            ControllerType controllerType,
            ButtonState buttonState) {
        int index = 2;
        Arrays.fill(buffer, index, index + 9, (byte) 0);

        //Right joycon bits
        if (controllerType == RIGHT_JOYCON || controllerType == PRO_CONTROLLER) {
            buffer[index] |= buttonState.getButton(Y) == 0 ? 0 : FULL_Y_BIT;
            buffer[index] |= buttonState.getButton(X) == 0 ? 0 : FULL_X_BIT;
            buffer[index] |= buttonState.getButton(B) == 0 ? 0 : FULL_B_BIT;
            buffer[index] |= buttonState.getButton(A) == 0 ? 0 : FULL_A_BIT;
            buffer[index] |= buttonState.getButton(RIGHT_SR) == 0 ? 0 : FULL_SR_BIT;
            buffer[index] |= buttonState.getButton(RIGHT_SL) == 0 ? 0 : FULL_SL_BIT;
            buffer[index] |= buttonState.getButton(R) == 0 ? 0 : FULL_L_R_BIT;
            buffer[index] |= buttonState.getButton(ZR) == 0 ? 0 : FULL_ZL_ZR_BIT;

            //RIGHT Stick bytes for analog stick
            int dataX = round(((buttonState.getAxis(RIGHT_STICK_X) + 100) / 200f) * 4095);
            int dataY = round(((buttonState.getAxis(RIGHT_STICK_Y) + 100) / 200f) * 4095);
            buffer[index + 6] = (byte) (dataX & 0xFF);
            buffer[index + 7] = (byte) ((dataX >> 8) & 0xF);
            buffer[index + 7] |= (byte) ((dataY & 0xF) << 4);
            buffer[index + 8] = (byte) ((dataY >> 4) & 0xFF);
        }

        //Button status shared
        buffer[index + 1] |= buttonState.getButton(MINUS) == 0 ? 0 : FULL_MINUS_BIT;
        buffer[index + 1] |= buttonState.getButton(PLUS) == 0 ? 0 : FULL_PLUS_BIT;
        buffer[index + 1] |= buttonState.getButton(RIGHT_STICK_BUTTON) == 0
                ? 0 : FULL_RIGHT_STICK_BIT;
        buffer[index + 1] |= buttonState.getButton(LEFT_STICK_BUTTON) == 0
                ? 0 : FULL_LEFT_STICK_BIT;
        buffer[index + 1] |= buttonState.getButton(HOME) == 0 ? 0 : FULL_HOME_BIT;
        buffer[index + 1] |= buttonState.getButton(CAPTURE) == 0 ? 0 : FULL_CAPTURE_BIT;

        //Left joycon bits
        if (controllerType == LEFT_JOYCON || controllerType == PRO_CONTROLLER) {
            buffer[index + 2] |= buttonState.getButton(DOWN) == 0 ? 0 : FULL_DOWN_BIT;
            buffer[index + 2] |= buttonState.getButton(RIGHT) == 0 ? 0 : FULL_RIGHT_BIT;
            buffer[index + 2] |= buttonState.getButton(LEFT) == 0 ? 0 : FULL_LEFT_BIT;
            buffer[index + 2] |= buttonState.getButton(UP) == 0 ? 0 : FULL_UP_BIT;
            buffer[index + 2] |= buttonState.getButton(LEFT_SR) == 0 ? 0 : FULL_SR_BIT;
            buffer[index + 2] |= buttonState.getButton(LEFT_SL) == 0 ? 0 : FULL_SL_BIT;
            buffer[index + 2] |= buttonState.getButton(L) == 0 ? 0 : FULL_L_R_BIT;
            buffer[index + 2] |= buttonState.getButton(ZL) == 0 ? 0 : FULL_ZL_ZR_BIT;

            //LEFT Stick bytes for analog stick
            int dataX = round(((buttonState.getAxis(LEFT_STICK_X) + 100) / 200f) * 4095);
            int dataY = round(((buttonState.getAxis(LEFT_STICK_Y) + 100) / 200f) * 4095);
            buffer[index + 3] = (byte) (dataX & 0xFF);
            buffer[index + 4] = (byte) ((dataX >> 8) & 0xF);
            buffer[index + 4] |= (byte) ((dataY & 0xF) << 4);
            buffer[index + 5] = (byte) ((dataY >> 4) & 0xFF);
        }
    }

    public byte[] build() {
        return buffer;
    }

    public byte getReportId() {
        return type.getReportId();
    }

    public void fillTime(JoyControllerState state) {
        long timeMillis = state.getTime();
        buffer[0] = (byte) timeMillis;
    }


    public void fillBattery(JoyControllerState state) {
        final BatteryData batteryData = state.getBatteryData();
        byte batteryByte = 0;
        // https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_notes.md#standard-input-report-format
        batteryByte |= (byte) ((round(batteryData.getBatteryLevel() * 4) * 2) << 4);
        batteryByte |= (byte) (batteryData.isCharging() ? 0x10 : 0x00);
        buffer[1] |= batteryByte;
    }

    //TODO Learn whats connection info
    public void fillConnectionInfo(JoyControllerState state) {
        buffer[1] |= 0x0E;
    }

    //TODO Fill Real Data
    public void fillVibratorData(JoyControllerState state) {
        VibratorData vibratorData = state.getVibratorData();
        buffer[11] = (byte) 0xB0;
    }

    public void fillSensorData(JoyController controller) {
        float[] accs = new float[3 * 3];
        Arrays.fill(accs, 0);
        float[] gyrs = new float[3 * 3];
        Arrays.fill(gyrs, 0);

        ControllerType controllerType = controller.getControllerType();
        final JoyControllerState state = controller.getState();
        Queue<AccelerometerEvent> accelerometerEvents = controller.getAccelerometerEvents();
        Queue<GyroscopeEvent> gyroscopeEvents = controller.getGyroscopeEvents();
        int multiplier = 1;
        final boolean isPro = controllerType == PRO_CONTROLLER;
        if (controllerType == RIGHT_JOYCON) {
            multiplier = -1;
        }

        byte[] sensorData = new byte[36];
        Arrays.fill(sensorData, (byte) 0);

        AccelerometerEvent[] accEvents = accelerometerEvents.toArray(new AccelerometerEvent[]{});
        final GyroscopeEvent[] gyrEvents = gyroscopeEvents.toArray(new GyroscopeEvent[]{});
        accelerometerEvents.clear();
        gyroscopeEvents.clear();

        //log(TAG, "accSize: " + accEvents.length);
        if (accEvents.length > 0) {
            int start = 0;
            //Start

            accs[0] = accEvents[start].values[0];
            accs[1] = accEvents[start].values[1];
            accs[2] = accEvents[start].values[2];

            int end = accEvents.length - 1;

            accs[6] = accEvents[end].values[0];
            accs[7] = accEvents[end].values[1];
            accs[8] = accEvents[end].values[2];
            while (start < end) {
                start++;
                end--;
            }
            accs[3] = (accEvents[end].values[0] + accEvents[start].values[0]) / 2;
            accs[4] = (accEvents[end].values[1] + accEvents[start].values[1]) / 2;
            accs[5] = (accEvents[end].values[2] + accEvents[start].values[2]) / 2;
        }
        //log(TAG, "Accs: " + Arrays.toString(accs));

        //log(TAG, "gyrSize: " + gyrEvents.length);
        if (gyrEvents.length > 0) {
            int start = 0;
            //Start
            gyrs[0] = gyrEvents[start].values[0];
            gyrs[1] = gyrEvents[start].values[1];
            gyrs[2] = gyrEvents[start].values[2];

            int end = gyrEvents.length - 1;

            gyrs[6] = gyrEvents[end].values[0];
            gyrs[7] = gyrEvents[end].values[1];
            gyrs[8] = gyrEvents[end].values[2];
            while (start < end) {
                start++;
                end--;
            }
            gyrs[3] = (gyrEvents[end].values[0] + gyrEvents[start].values[0]) / 2;
            gyrs[4] = (gyrEvents[end].values[1] + gyrEvents[start].values[1]) / 2;
            gyrs[5] = (gyrEvents[end].values[2] + gyrEvents[start].values[2]) / 2;
        }

        //log(TAG, "Gyrs: " + Arrays.toString(gyrs));

        double[] accCoeffs = state.getAccCoeffs();

        if (state.getSensorMode() == JoyControllerState.SensorMode.QUATERNION) {
            QuaternionPacker packer = state.getQuaternionPacker();
            for (GyroscopeEvent ev : gyrEvents) {
                double gyrInX = multiplier * ev.values[0];
                double gyrInY = ev.values[1];
                double gyrInZ = multiplier * ev.values[2];
                packer.integrateGyro(
                        isPro ? gyrInX :  gyrInY,
                        isPro ? gyrInY : -gyrInX,
                        gyrInZ,
                        ev.timestamp);
            }

            short[][] accelSamples = new short[3][];
            for (int i = 0; i < 3; i++) {
                accelSamples[i] = computeRawAccel(accs, i, multiplier, isPro, accCoeffs);
            }
            packer.pack(sensorData, 0, accelSamples, SystemClock.elapsedRealtime());
        } else {
            double[] gyrCoeffs = state.getGyrCoeffs();
            short[] gyrOffset = state.getGyrOffset();

            for (int i = 0; i < 3; i++) {
                short[] rawAcc = computeRawAccel(accs, i, multiplier, isPro, accCoeffs);
                sensorData[i * 12] = (byte) (rawAcc[0] & 0xFF);
                sensorData[1 + i * 12] = (byte) (rawAcc[0] >> 8 & 0xFF);
                sensorData[2 + i * 12] = (byte) (rawAcc[1] & 0xFF);
                sensorData[3 + i * 12] = (byte) (rawAcc[1] >> 8 & 0xFF);
                sensorData[4 + i * 12] = (byte) (rawAcc[2] & 0xFF);
                sensorData[5 + i * 12] = (byte) (rawAcc[2] >> 8 & 0xFF);

                float gyrX = multiplier * gyrs[i * 3];
                float gyrY = gyrs[i * 3 + 1];
                float gyrZ = multiplier * gyrs[i * 3 + 2];
                short rawGyrX = (short) round(clamp((isPro ? gyrX : gyrY)
                        * gyrCoeffs[0] + gyrOffset[0], MIN_VALUE, MAX_VALUE));
                short rawGyrY = (short) round(clamp((isPro ? gyrY : -gyrX)
                        * gyrCoeffs[1] + gyrOffset[1], MIN_VALUE, MAX_VALUE));
                final short rawGyrZ = (short) round(clamp(gyrZ
                        * gyrCoeffs[2] + gyrOffset[2], MIN_VALUE, MAX_VALUE));

                sensorData[6 + i * 12] = (byte) (rawGyrX & 0xFF);
                sensorData[7 + i * 12] = (byte) (rawGyrX >> 8 & 0xFF);
                sensorData[8 + i * 12] = (byte) (rawGyrY & 0xFF);
                sensorData[9 + i * 12] = (byte) (rawGyrY >> 8 & 0xFF);
                sensorData[10 + i * 12] = (byte) (rawGyrZ & 0xFF);
                sensorData[11 + i * 12] = (byte) (rawGyrZ >> 8 & 0xFF);
            }
        }

        System.arraycopy(sensorData, 0, buffer, 12, 36);
    }

    private static short[] computeRawAccel(
            float[] accs, int frame, int multiplier, boolean isPro, double[] accCoeffs) {
        float accX = multiplier * accs[frame * 3];
        float accY = accs[frame * 3 + 1];
        float accZ = multiplier * accs[frame * 3 + 2];
        short rawX = (short) round(clamp(
                (isPro ? accX : accY) * accCoeffs[0], MIN_VALUE, MAX_VALUE));
        short rawY = (short) round(clamp(
                (isPro ? accY : -accX) * accCoeffs[1], MIN_VALUE, MAX_VALUE));
        short rawZ = (short) round(clamp(
                accZ * accCoeffs[2], MIN_VALUE, MAX_VALUE));
        return new short[]{rawX, rawY, rawZ};
    }

    public void fillNfcIrData(JoyController controller) {
        JoyControllerState state = controller.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        NfcIrMcu.Action action = nfcIrMcu.getAction();
        switch (action) {
            case NON:
                // NFC NONE: no tag present
                buffer[48] = (byte) 0x2a;
                buffer[49] = (byte) 0x00;
                buffer[50] = (byte) 0x05;
                buffer[51] = (byte) 0x00;
                buffer[52] = (byte) 0x00;
                buffer[53] = (byte) 0x09;
                buffer[54] = (byte) 0x31;
                buffer[55] = (byte) 0x00;
                Arrays.fill(buffer, 56, buffer.length, (byte) 0x00);
                break;
            case REQUEST_STATUS:
                fillNfcIrStatus(state);
                break;
            case START_TAG_DISCOVERY:
            case START_TAG_DISCOVERY_AUTO_MOVE:
                fillTagDiscovery(action, controller);
                break;
            case START_TAG_POLLING:
                fillTagPolling(controller);
                break;
            case READ_TAG:
            case READ_TAG_2:
                fillReadTag(controller);
                break;
            case READ_TAG_FINISHED:
                fillReadFinished(controller);
                break;
            case WRITE_TAG_SETUP:
                fillWriteSetup(controller);
                break;
            case WRITE_TAG_AWAITING:
                fillWriteAwaiting(controller);
                break;
            case WRITE_TAG_ACK:
                fillWriteAck(controller);
                break;
            case WRITE_TAG_REMOVE:
                fillWriteRemove(controller);
                break;
            default:
        }

        buffer[buffer.length - 1] = ByteUtils.crc8(Arrays.copyOfRange(buffer, 48, buffer.length));
    }

    private void fillReadFinished(JoyController controller) {
        //NFC State/Tag Info
        buffer[48] = (byte) 0x2a;
        //Error Code
        buffer[49] = (byte) 0x00;
        //Input type state info
        buffer[50] = (byte) 0x05;
        buffer[51] = (byte) 0x00;
        buffer[52] = (byte) 0x00;
        byte[] bytes = Hex.stringToBytes("0931040000000101020007");
        AmiiboConfig amiiboConfig = controller.getAmiiboConfig();
        byte[] amiiboBytes = amiiboConfig.getAmiiboBytes();
        System.arraycopy(bytes, 0, buffer, 53, bytes.length);
        System.arraycopy(amiiboBytes, 0, buffer, 53 + bytes.length, 3);
        System.arraycopy(amiiboBytes, 4, buffer, 53 + 3 + bytes.length, 4);
        // Return to polling so the Switch can initiate a write with 0x06 + non-zero UID
        JoyControllerState state = controller.getState();
        state.getNfcIrMcu().setAction(START_TAG_POLLING);
    }

    private void fillNfcIrStatus(JoyControllerState state) {
        buffer[48] = 0x01;
        buffer[49] = 0x00;
        buffer[50] = 0x00;
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        System.arraycopy(nfcIrMcu.getFirmwareMajor(), 0, buffer, 51, 2);
        System.arraycopy(nfcIrMcu.getFirmwareMinor(), 0, buffer, 53, 2);
        buffer[55] = nfcIrMcu.getMcuState().getByte();
    }

    private void fillTagDiscovery(NfcIrMcu.Action action, JoyController controller) {
        //NFC State/Tag Info
        buffer[48] = (byte) 0x2a;
        //Error Code
        buffer[49] = (byte) 0x00;
        //Input type state info
        buffer[50] = (byte) 0x05;
        //
        buffer[51] = 0x00;
        buffer[52] = 0x00;
        buffer[53] = 0x09;
        buffer[54] = 0x31;
        buffer[55] = 0x00;


        if (action == START_TAG_DISCOVERY) {
            JoyControllerState state = controller.getState();
            NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
            nfcIrMcu.setAction(START_TAG_DISCOVERY_AUTO_MOVE);
            ScheduledExecutorService executorService = controller.getExecutorService();
            executorService.schedule(() -> {
                NfcIrMcu.Action a = nfcIrMcu.getAction();
                if (a == START_TAG_DISCOVERY_AUTO_MOVE) {
                    log(TAG, "Auto Move To Polling");
                    nfcIrMcu.setAction(START_TAG_POLLING);
                }
            }, 2000, TimeUnit.MILLISECONDS);
        }
    }

    private void fillTagPolling(JoyController controller) {
        //NFC State/Tag Info
        buffer[48] = (byte) 0x2a;
        //Error Code
        buffer[49] = (byte) 0x00;
        //Input type state info
        buffer[50] = (byte) 0x05;
        buffer[51] = 0x00;
        buffer[52] = 0x00;
        JoyControllerState state = controller.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        AmiiboConfig amiiboConfig = controller.getAmiiboConfig();
        byte[] amiiboBytes = amiiboConfig.getAmiiboBytes();
        if (Objects.isNull(amiiboBytes)) {
            buffer[53] = 0x09;
            buffer[54] = 0x31;
            buffer[55] = 0x01;
            controller.showAmiiboPicker();
        } else {
            // First response: POLL (0x01); subsequent: POLL_AGAIN (0x09)
            byte nfcState = nfcIrMcu.isFirstPollSent() ? (byte) 0x09 : (byte) 0x01;
            nfcIrMcu.setFirstPollSent(true);
            byte[] bytes = new byte[]{
                0x09, 0x31, nfcState, 0x00, 0x00, 0x00, 0x01, 0x01, 0x02, 0x00, 0x07};
            System.arraycopy(bytes, 0, buffer, 53, bytes.length);
            int removing = nfcIrMcu.getRemoveFramesRemaining();
            if (removing > 0) {
                // Send zero UID for several frames after write to signal tag removal
                nfcIrMcu.setRemoveFramesRemaining(removing - 1);
                // buffer[64..70] are already zero (fresh buffer) — zero UID
            } else {
                System.arraycopy(amiiboBytes, 0, buffer, 53 + bytes.length, 3);
                System.arraycopy(amiiboBytes, 4, buffer, 53 + 3 + bytes.length, 4);
            }
        }
    }

    private void fillReadTag(JoyController controller) {
        JoyControllerState state = controller.getState();
        final NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        AmiiboConfig amiiboConfig = controller.getAmiiboConfig();
        final byte[] amiiboBytes = amiiboConfig.getAmiiboBytes();
        //NFC TAG read
        buffer[48] = (byte) 0x3a;
        //Error Code
        buffer[49] = (byte) 0x00;
        //Input type state info
        buffer[50] = (byte) 0x07;

        //3A0007
        if (nfcIrMcu.getAction() == READ_TAG) {
            //010001310200000001020007047C7CD24D5D80000000007DFDF0793651ABD7466E39C191BABEB856CEEDF1
            //CE44CC75EAFB27094D087AE803003B3C7778860000047C7C8CD24D5D8042480FE0F110FFEEA5CA9C005EB5
            //4F12E8C19C8F3162CE257F60A48A17CC628C2FB68C88991647679421AF5D36EE8CBA7C68AB0EAFDBEBBD90
            //7BA6B16FE63225F2B6A3833D36A95EE1FDC35000030000003701020512C4130EBD1509F563F799C8513F15
            //AFA4DD5D41AA85ECF77329E218EBD0D9243F51EB1D8B1E647A420999AD0C56A71F192947F5230FDFCAC06D
            //103D5316A4D9089372C0815B96E1DCF2A7F3CE6330543F5123E34EB844DCC8C6963A114E92AB5F17F076E8
            //8ACFB30D8716839F059DFE04D9DBE04FD38FE6CD052ED7D9A0981372C19E9982189A12F028E5C8AEDDF9C7
            //9900B0202183EC8673,
            byte[] bytes = Hex.stringToBytes("010001310200000001020007");
            System.arraycopy(bytes, 0, buffer, 51, bytes.length);
            System.arraycopy(amiiboBytes, 0, buffer, 51 + bytes.length, 3);
            System.arraycopy(amiiboBytes, 4, buffer, 51 + 3 + bytes.length, 4);
            byte[] bytes2 = Hex.stringToBytes("000000007DFDF0793651ABD7466E39C191BABEB856CEEDF1CE44"
                    + "CC75EAFB27094D087AE803003B3C7778860000");
            System.arraycopy(bytes2, 0, buffer, 58 + bytes.length, bytes2.length);
            System.arraycopy(amiiboBytes, 0, buffer, 58 + bytes.length + bytes2.length, 245);
            nfcIrMcu.setAction(READ_TAG_2);
        } else {
            byte[] bytes = Hex.stringToBytes("02000927");
            System.arraycopy(bytes, 0, buffer, 51, bytes.length);
            System.arraycopy(
                    amiiboBytes, 0xF5, buffer, 51 + bytes.length, amiiboBytes.length - 0xF5);
            nfcIrMcu.setAction(READ_TAG_FINISHED);
        }
    }

    // Write setup response: sent once after Switch sends 0x06 with non-zero UID.
    // Header + UID + fixed 48-byte blob.
    private void fillWriteSetup(JoyController controller) {
        JoyControllerState state = controller.getState();
        final NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        AmiiboConfig amiiboConfig = controller.getAmiiboConfig();
        byte[] amiiboBytes = amiiboConfig.getAmiiboBytes();

        byte[] header = Hex.stringToBytes("3a0007010008400200000001020007");
        System.arraycopy(header, 0, buffer, 48, header.length);
        int pos = 48 + header.length;
        System.arraycopy(amiiboBytes, 0, buffer, pos, 3);
        System.arraycopy(amiiboBytes, 4, buffer, pos + 3, 4);
        pos += 7;
        byte[] blob = Hex.stringToBytes(
                "00000000fdb0c0a434c9bf31690030aaef56444b0f602627366d5a281adc697f"
                + "de0d6cbc010303000000000000f110ffee");
        System.arraycopy(blob, 0, buffer, pos, blob.length);

        log(TAG, "NFC write setup response sent → WRITE_TAG_AWAITING");
        nfcIrMcu.setAction(WRITE_TAG_AWAITING);
    }

    // NFC status with AWAITING_WRITE state (0x04): sent between write setup and first 0x08 packet.
    private void fillWriteAwaiting(JoyController controller) {
        JoyControllerState state = controller.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        AmiiboConfig amiiboConfig = controller.getAmiiboConfig();
        byte[] amiiboBytes = amiiboConfig.getAmiiboBytes();

        fillNfcWriteStatus(buffer, (byte) 0x04, nfcIrMcu.getAckSeqNo(), amiiboBytes);
    }

    // NFC status ACK for each 0x08 write packet: WRITING (0x03) or PROCESSING_WRITE (0x05) state.
    private void fillWriteAck(JoyController controller) {
        JoyControllerState state = controller.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        AmiiboConfig amiiboConfig = controller.getAmiiboConfig();
        byte[] amiiboBytes = amiiboConfig.getAmiiboBytes();

        boolean lastPacket = nfcIrMcu.isLastWritePacketReceived();
        if (lastPacket) {
            fillNfcWriteStatus(buffer, (byte) 0x05, nfcIrMcu.getAckSeqNo(), amiiboBytes);
            nfcIrMcu.setAction(WRITE_TAG_REMOVE);
        } else {
            fillNfcWriteStatus(buffer, (byte) 0x03, nfcIrMcu.getAckSeqNo(), amiiboBytes);
            nfcIrMcu.setAction(WRITE_TAG_AWAITING);
        }
    }

    // Tag-removed response: sent ~4 times after write so Switch exits the amiibo screen.
    private void fillWriteRemove(JoyController controller) {
        JoyControllerState state = controller.getState();
        final NfcIrMcu nfcIrMcu = state.getNfcIrMcu();

        // NFC NONE state: no tag present
        buffer[48] = 0x2a;
        buffer[49] = 0x00;
        buffer[50] = 0x05;
        buffer[51] = 0x00;
        buffer[52] = 0x00;
        buffer[53] = 0x09;
        buffer[54] = 0x31;
        buffer[55] = 0x00;
        Arrays.fill(buffer, 56, buffer.length, (byte) 0x00);

        int remaining = nfcIrMcu.getRemoveFramesRemaining() - 1;
        nfcIrMcu.setRemoveFramesRemaining(remaining);
        if (remaining <= 0) {
            nfcIrMcu.setLastWritePacketReceived(false);
            nfcIrMcu.setAckSeqNo(0);
            nfcIrMcu.setAction(NON);
        }
    }

    private static void fillNfcWriteStatus(byte[] buffer, byte nfcStateByte, int ackSeqNo,
            byte[] amiiboBytes) {
        buffer[48] = 0x2a;
        buffer[49] = 0x00;
        buffer[50] = 0x05;
        buffer[51] = 0x00;              // seq_no (always 0)
        buffer[52] = (byte) ackSeqNo;
        buffer[53] = 0x09;
        buffer[54] = 0x31;
        buffer[55] = nfcStateByte;
        buffer[56] = 0x00;
        buffer[57] = 0x00;
        buffer[58] = 0x00;
        buffer[59] = 0x01;
        buffer[60] = 0x02;
        buffer[61] = 0x00;
        buffer[62] = 0x07;
        System.arraycopy(amiiboBytes, 0, buffer, 63, 3);
        System.arraycopy(amiiboBytes, 4, buffer, 66, 4);
    }

    public void fillAckByte(byte ack) {
        buffer[12] = ack;
    }

    public void fillSubCommand(byte subCommandId) {
        buffer[13] = subCommandId;
    }

    public void fillData(int index, byte[] data) {
        System.arraycopy(data, 0, buffer, index, data.length);
    }

    public String toString() {
        return "InputReport: " + ByteUtils.encodeHexString(getReportId())
                + " Data: " + Hex.bytesToStringUppercase(build());
    }
}
