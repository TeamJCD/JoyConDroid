package com.rdapps.gamepad.nintendo_switch;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Process;
import android.util.Log;

import com.google.android.gms.common.util.Hex;
import com.rdapps.gamepad.device.AbstractDevice;
import com.rdapps.gamepad.log.JoyConLog;
import com.rdapps.gamepad.memory.FileSPIMemory;
import com.rdapps.gamepad.memory.RAFSPIMemory;
import com.rdapps.gamepad.memory.SPIMemory;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.service.BluetoothControllerService;
import com.rdapps.gamepad.util.ByteUtils;
import com.rdapps.gamepad.util.MacUtils;
import com.rdapps.gamepad.util.PreferenceUtils;
import com.rdapps.gamepad.util.PriorityThreadFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.Setter;

import static androidx.core.math.MathUtils.clamp;
import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.nintendo_switch.InputMode.NFC_IR_MODE;
import static com.rdapps.gamepad.nintendo_switch.InputMode.SIMPLE_HID_MODE;
import static com.rdapps.gamepad.nintendo_switch.InputMode.STANDARD_FULL_MODE;
import static com.rdapps.gamepad.nx.constant.NXConstants.ACK;
import static com.rdapps.gamepad.nx.constant.NXConstants.BLUETOOTH_MANUAL_PAIRING;
import static com.rdapps.gamepad.nx.constant.NXConstants.BUTTON_REPORT;
import static com.rdapps.gamepad.nx.constant.NXConstants.CAPTURE_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.CONTROLLER_STATE;
import static com.rdapps.gamepad.nx.constant.NXConstants.DOWN_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_A_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_BUTTON_REPORT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_B_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_CAPTURE_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_DOWN_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_HOME_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_LEFT_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_LEFT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_L_R_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_MINUS_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_PLUS_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_RIGHT_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_RIGHT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_SL_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_SR_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_UP_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_X_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_Y_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.FULL_ZL_ZR_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.HOME_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.LEFT_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.LEFT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.L_R_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.MINUS_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.NFC_IR_REPORT;
import static com.rdapps.gamepad.nx.constant.NXConstants.PLUS_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_AXIS_SENSOR;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_DEVICE_INFO;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_INPUT_REPORT_MODE;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_NFC_IR_MCU;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_RUMBLE_AND_SUBCOMMAND;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_RUMBLE_ONLY;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_SET_NFC_IR_CONFIGURATION;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_SET_NFC_IR_STATE;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_SET_PLAYER_LIGHTS;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_SET_SHIPMENT;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_SPI_FLASH_READ;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_SPI_FLASH_WRITE;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_TRIGGER_BUTTONS;
import static com.rdapps.gamepad.nx.constant.NXConstants.REQUEST_VIBRATION;
import static com.rdapps.gamepad.nx.constant.NXConstants.RIGHT_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.RIGHT_STICK_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.SET_IMU_SENSITIVITY;
import static com.rdapps.gamepad.nx.constant.NXConstants.SIMPLE_HID_REPORT;
import static com.rdapps.gamepad.nx.constant.NXConstants.SL_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.SR_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.SUBCOMMAND_REPLY_REPORT;
import static com.rdapps.gamepad.nx.constant.NXConstants.UP_BIT;
import static com.rdapps.gamepad.nx.constant.NXConstants.ZL_ZR_BIT;
import static com.rdapps.gamepad.protocol.ControllerType.LEFT_JOYCON;
import static com.rdapps.gamepad.protocol.ControllerType.PRO_CONTROLLER;
import static com.rdapps.gamepad.protocol.ControllerType.RIGHT_JOYCON;
import static com.rdapps.gamepad.toast.ToastHelper.missingPermission;
import static com.rdapps.gamepad.util.ByteUtils.toShort;
import static java.lang.Short.MAX_VALUE;
import static java.lang.Short.MIN_VALUE;
import static java.lang.Short.toUnsignedInt;

public class SwitchController extends AbstractDevice {
    private static final String TAG = SwitchController.class.getName();

    private static final byte SUBCLASS = (byte) 0x08;
    private static final String HID_NAME = "Wireless Gamepad";
    private static final String HID_DESCRIPTION = "Gamepad";
    private static final String HID_PROVIDER = "Nintendo";
    private static final String DESCRIPTOR = "05010905a1010601ff8521092175089530810285300930750895308102853109317508966901810285320932750896690181028533093375089669018102853f05091901291015002501750195108102050109391500250775049501814205097504950181010501093009310933093416000027ffff00007510950481020601ff85010901750895309102851009107508953091028511091175089530910285120912750895309102c0";


    private static final long WAIT_BEFORE_HANDSHAKE_MS = 1000;

    public static final int SAMPLING_INTERVAL = 5000; //5000 microseconds;
    public static final int SAMPLES_PER_INTERVAL = 3;

    private static final float G = 9.80665f;
    private float[] accCoeffs;
    private float[] gyrCoeffs;
    private short[] gyrOffset;

    private SPIMemory eeprom;
    private ButtonStates buttonStates;
    private BluetoothControllerService service;
    private boolean amiiboEnabled;
    private volatile InputMode inputMode;
    private volatile MCUMode mcuMode;

    @Setter
    private volatile byte[] amiiboBytes;

    private ScheduledExecutorService executorService;
    private ScheduledFuture fullModeSender;

    private Queue<SensorEvent> accelerometerEvents = new LinkedBlockingQueue<>();
    private Queue<SensorEvent> gyroscopeEvents = new LinkedBlockingQueue<>();

    private ControllerType type;

    private Callback notificationCallBack;

    public interface Callback {
        void notifyBeforePackage();
    }

    public SwitchController(BluetoothControllerService service, ControllerType type) {
        super(
                service.getApplicationContext(),
                type.getBTName(),
                SUBCLASS,
                HID_NAME,
                HID_DESCRIPTION,
                HID_PROVIDER,
                DESCRIPTOR
        );

        this.type = type;
        this.notificationCallBack = null;
        this.service = service;

        Context context = service.getApplicationContext();
        try {
            String btName = type.getBTName();
            this.eeprom = new RAFSPIMemory(context, btName, type.getMemoryResource());
        } catch (Exception e) {
            Log.e(TAG, "RAFEEPROM Failed.", e);
            try {
                this.eeprom = new FileSPIMemory(context, type.getMemoryResource());
            } catch (IOException ex) {
                JoyConLog.log(TAG, "File PROM exception", e);
            }
        }
        this.amiiboEnabled = PreferenceUtils.getAmiiboEnabled(context);
        this.buttonStates = new ButtonStates(type);
        this.inputMode = SIMPLE_HID_MODE;
        this.executorService = Executors.newSingleThreadScheduledExecutor(
                new PriorityThreadFactory(Process.THREAD_PRIORITY_URGENT_AUDIO,
                        false,
                        "BT Thread",
                        false)
        );
        calculateCoeffs();
        this.amiiboBytes = null;
        this.mcuMode = new MCUMode();
        this.amiiboBytes = PreferenceUtils.getAmiiboBytes(context);
    }

    private void calculateCoeffs() {
        accCoeffs = new float[3];
        gyrCoeffs = new float[3];

        byte[] motionCalibration = eeprom.read(0x8026, 26);
        short[] accOffset = new short[3];
        int[] calAccCoef = new int[3];
        gyrOffset = new short[3];
        int[] calGyrCoef = new int[3];
        if (toShort(motionCalibration, 0) != (short) 0xB2A1) {
            motionCalibration = eeprom.read(0x6020, 26);
        }
        for (int i = 0; i < 3; i++) {
            accOffset[i] = toShort(motionCalibration, i * 2 + 2);
            calAccCoef[i] = toUnsignedInt(toShort(motionCalibration, i * 2 + 8));
            accCoeffs[i] = (float) ((calAccCoef[i] - accOffset[i]) / (4.0 * G));
            gyrOffset[i] = toShort(motionCalibration, i * 2 + 14);
            calGyrCoef[i] = toUnsignedInt(toShort(motionCalibration, i * 2 + 20));
            gyrCoeffs[i] = (float) ((calGyrCoef[i] - gyrOffset[i]) / (0.01745329251994 * 936.0));
        }
    }

    public void setLeftSLState(int buttonState) {
        this.buttonStates.setLeft_sl(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setLeftSRState(int buttonState) {
        this.buttonStates.setLeft_sr(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setRightSLState(int buttonState) {
        this.buttonStates.setRight_sl(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setRightSRState(int buttonState) {
        this.buttonStates.setRight_sr(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setLeftState(int buttonState) {
        this.buttonStates.setLeft(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setRightState(int buttonState) {
        this.buttonStates.setRight(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setUpState(int buttonState) {
        this.buttonStates.setUp(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setDownState(int buttonState) {
        this.buttonStates.setDown(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setYState(int buttonState) {
        this.buttonStates.setY(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setAState(int buttonState) {
        this.buttonStates.setA(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setXState(int buttonState) {
        this.buttonStates.setX(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setBState(int buttonState) {
        this.buttonStates.setB(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setL(int buttonState) {
        this.buttonStates.setL(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setZL(int buttonState) {
        this.buttonStates.setZl(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setR(int buttonState) {
        this.buttonStates.setR(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setZR(int buttonState) {
        this.buttonStates.setZr(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setMinus(int buttonState) {
        this.buttonStates.setMinus(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setPlus(int buttonState) {
        this.buttonStates.setPlus(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setCapture(int buttonState) {
        this.buttonStates.setCapture(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setHome(int buttonState) {
        this.buttonStates.setHome(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setLeftStickX(int stickValue) {
        this.buttonStates.setLeft_stick_x(stickValue);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setLeftStickY(int stickValue) {
        this.buttonStates.setLeft_stick_y(stickValue);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setLeftStick(int buttonState) {
        this.buttonStates.setLeft_stick_button(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setRightStickX(int stickValue) {
        this.buttonStates.setRight_stick_x(stickValue);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setRightStickY(int stickValue) {
        this.buttonStates.setRight_stick_y(stickValue);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    public void setRightStick(int buttonState) {
        this.buttonStates.setRight_stick_button(buttonState);
        if (inputMode == SIMPLE_HID_MODE) {
            sendShortButton();
        }
    }

    @Override
    public void setRemoteDevice(BluetoothDevice pluggedDevice) {
        super.setRemoteDevice(pluggedDevice);
        if (Objects.nonNull(pluggedDevice)) {
            startHandShake();
        } else {
            inputMode = SIMPLE_HID_MODE;
        }
    }

    public void setCallbackFunction(Callback notificationCallBack) {
        this.notificationCallBack = notificationCallBack;
    }

    @Override
    public void connectingDevice(BluetoothDevice pluggingDevice) {
        //setRemoteDevice(pluggingDevice);
    }

    @Override
    public void stop() {
        inputMode = SIMPLE_HID_MODE;
        executorService.shutdownNow();
    }

    @Override
    public void onGetReport(BluetoothDevice rDevice, byte type, byte id, int bufferSize) {
        Log.w(TAG, "Get Report Type: " + type + " Id: " + id + " bufferSize: " + bufferSize);
    }

    @Override
    public void onSetReport(BluetoothDevice rDevice, byte type, byte id, byte[] data) {
        Log.w(TAG, "Set Report Type: " + type + " Id: " + id + " data: " + Hex.bytesToStringUppercase(data));
    }

    @Override
    public void onSetProtocol(BluetoothDevice rDevice, byte protocol) {
        Log.w(TAG, "Set Protocol Protocol: " + ByteUtils.encodeHexString(protocol));
    }

    @Override
    public void onInterruptData(BluetoothDevice rDevice, byte reportId, byte[] data) {
        Log.v(TAG, "Interrupt Data Report Id: " + ByteUtils.encodeHexString(reportId) + " data: " + Hex.bytesToStringUppercase(data));

        if (reportId == REQUEST_RUMBLE_AND_SUBCOMMAND) {
            handleRumbleAndSubcommand(data);
        } else if (reportId == REQUEST_RUMBLE_ONLY) {
            handleRumbleOnly(data);
        } else if (reportId == REQUEST_NFC_IR_MCU) {
            handleNFC_IR(data);
        } else {
            Log.w(TAG, "Unknown Command : " + ByteUtils.encodeHexString(reportId));
        }
    }

    private void handleRumbleAndSubcommand(byte[] data) {
        byte globalPacketNumber = data[0];
        byte[] rumbleData = Arrays.copyOfRange(data, 1, 9);

        byte subcommand = data[9];

        byte[] output = new byte[48];
        //Clean Data
        Arrays.fill(output, (byte) 0);

        output[0] = getTimeByte();
        output[1] = getBatteryReport();
        //Fills 9 bytes of data
        buttonStates.fillFullButtonReport(output, 2);
        output[11] = getVibratorData();

        if (subcommand == CONTROLLER_STATE) {
            output[12] = ACK;
            output[13] = subcommand;
            output[14] = 0x03;
            output[14] = 0x03;
            Log.w(TAG, "Controller State ");

        } else if (subcommand == BLUETOOTH_MANUAL_PAIRING) {
            output[12] = ACK | BLUETOOTH_MANUAL_PAIRING;
            output[13] = subcommand;
            Log.w(TAG, "BT Pairing ");
            //TODO Implement this
        }
        //Request Device Information
        else if (subcommand == REQUEST_DEVICE_INFO) {
            //ACK
            output[12] = ACK | REQUEST_DEVICE_INFO;
            output[13] = subcommand;
            log(TAG, "Device Info ");
            fillDeviceInformation(output, 14);
        }
        //Set shipment
        else if (subcommand == REQUEST_SET_SHIPMENT) {
            //ACK
            output[12] = ACK;
            output[13] = subcommand;

            byte arg = data[10];
            log(TAG, "Set shipment input: " + ByteUtils.encodeHexString(arg));
        }
        //Set input report mode
        else if (subcommand == REQUEST_INPUT_REPORT_MODE) {
            byte mode = data[10];
            log(TAG, "Input Report Mode: " + ByteUtils.encodeHexString(mode));
            //ACK
            output[12] = ACK;
            output[13] = subcommand;

            if (mode == FULL_BUTTON_REPORT) {
                startFullReport(STANDARD_FULL_MODE);
            } else if (mode == NFC_IR_REPORT && amiiboEnabled) {
                startFullReport(NFC_IR_MODE);
            } else if (mode == SIMPLE_HID_REPORT) {
                startFullReport(SIMPLE_HID_MODE);
            } else {
                Log.w(TAG, "Unknown mode " + mode);
            }
        }
        //Trigger buttons elapsed time
        else if (subcommand == REQUEST_TRIGGER_BUTTONS) {
            //ACK
            output[12] = (byte) 0x83;
            output[13] = subcommand;

//            //PROCONTROLLER
//            if (type.getTypeByte() == 0x03) {
//                output[14] = 0x2C;
//                output[15] = 0x01;
//                output[16] = 0x2C;
//                output[17] = 0x01;
//            } else {
//                output[22] = 0x2C;
//                output[23] = 0x01;
//                output[24] = 0x2C;
//                output[25] = 0x01;
//            }
            //TODO start sending full report 60hz
            //startFullReport();
        }
        //SPI flash read
        else if (subcommand == REQUEST_SPI_FLASH_READ) {
            int eepromLocation = 0;
            for (int i = 0; i < 4; i++) {
                int a = data[10 + i] & 0xFF;
                eepromLocation |= a << (i * 8);
            }
            log(TAG, "EEPROM Location: " + Integer.toHexString(eepromLocation));
            int len = data[14] & 0xFF;
            log(TAG, "READ Length: " + len);

            //ACK
            output[12] = ACK | REQUEST_SPI_FLASH_READ;
            output[13] = subcommand;

            byte[] readBytes = eeprom.read(eepromLocation, len);

            //SPI read reply
            //Copy parameters back to reply
            System.arraycopy(data, 10, output, 14, 5);
            System.arraycopy(readBytes, 0, output, 19, readBytes.length);
        }
        //SPI flash Write
        else if (subcommand == REQUEST_SPI_FLASH_WRITE) {
            Log.w(TAG, "Unknown Subcommand : " + ByteUtils.encodeHexString(subcommand));
            Log.w(TAG, "Unknown Subcommand  Data: " + Hex.bytesToStringUppercase(data));
            int eepromLocation = 0;
            for (int i = 0; i < 4; i++) {
                int a = data[10 + i] & 0xFF;
                eepromLocation |= a << (i * 8);
            }
            log(TAG, "EEPROM Location: " + Integer.toHexString(eepromLocation));
            int len = data[14] & 0xFF;
            log(TAG, "Write Length: " + len);

            //ACK
            output[12] = ACK;
            output[13] = subcommand;

            if (len > 0) {
                eeprom.write(eepromLocation, Arrays.copyOfRange(data, 15, len));
            }
        }
        //Set player lights
        else if (subcommand == REQUEST_SET_PLAYER_LIGHTS) {
            output[12] = ACK;
            output[13] = subcommand;

            //TODO: set player lights on UI
            byte arg = data[10];
            log(TAG, "player lights: " + ByteUtils.encodeHexString(arg));
        }
        //Enable 6-Axis sensor
        else if (subcommand == REQUEST_AXIS_SENSOR) {
            output[12] = ACK;
            output[13] = subcommand;

            //TODO: Enable 6-Axis sensor
            byte arg = data[10];
            Log.w(TAG, "Enable 6-Axis sensor: " + ByteUtils.encodeHexString(arg));
        }
        //Set 6-Axis sensor sensitivity
        else if (subcommand == SET_IMU_SENSITIVITY) {
            output[12] = ACK;
            output[13] = subcommand;

            //TODO: Enable 6-Axis sensor
            byte arg = data[10];
            Log.w(TAG, "Enable 6-Axis sensor: " + ByteUtils.encodeHexString(arg));
        }
        //Enable vibration
        else if (subcommand == REQUEST_VIBRATION) {
            output[12] = ACK;
            output[13] = subcommand;

            //TODO: Enable vibration
            byte arg = data[10];
            log(TAG, "Enable vibration: " + ByteUtils.encodeHexString(arg));
        }
        //Set NFC/IR MCU state
        else if (subcommand == REQUEST_SET_NFC_IR_CONFIGURATION) {
            output[12] = (byte) 0xA0;
            output[13] = subcommand;

            byte mcucmd = data[10];
            byte mcusubcmd = data[11];

            if (mcucmd == 0x21) {
                if (mcusubcmd == 0x00) {
                    byte mode = data[12];

                    output[14] = 0x01;
                    output[15] = 0x00;
                    output[16] = 0x00;
                    System.arraycopy(mcuMode.getFwMajor(), 0, output, 17, 2);
                    System.arraycopy(mcuMode.getFwMinor(), 0, output, 19, 2);
                    output[21] = 0x01; //Stand By

                    //NFC Mode
                    if (mode == 0x04) {
                        mcuMode.setState(MCUMode.State.NFC);
                    } else {
                        output[16] = (byte) 0xFF;
                        mcuMode.setState(MCUMode.State.NOT_INITIALIZED);
                    }
                }
            }

            output[47] = ByteUtils.crc8(Arrays.copyOfRange(output, 14, output.length));
        }
        //Set NFC/IR MCU state
        else if (subcommand == REQUEST_SET_NFC_IR_STATE) {
            output[12] = ACK;
            output[13] = subcommand;

            byte arg = data[10];
            String argument = "Unknown";
            if (arg == 0x00) {
                argument = "Suspend";
                mcuMode.setState(MCUMode.State.STAND_BY);
            } else if (arg == 0x01) {
                argument = "Resume";
            } else if (arg == 0x02) {
                argument = "Resume For Update";
            }
            log(TAG, "NFC argument: " + ByteUtils.encodeHexString(arg) + " : " + argument);
        } else {
            // Everything else
            output[12] = ACK;
            output[13] = subcommand;
            //output[14] = 0x03;

            Log.w(TAG, "Unknown Subcommand : " + ByteUtils.encodeHexString(subcommand));
            Log.w(TAG, "Unknown Subcommand  Data: " + Hex.bytesToStringUppercase(data));
        }

        sendReport(SUBCOMMAND_REPLY_REPORT, output);
    }

    private void handleRumbleOnly(byte[] data) {
        log(TAG, "Rumble Data: " + Hex.bytesToStringUppercase(data));
    }

    private void handleNFC_IR(byte[] data) {
        log(TAG, "NFC/IR Data: " + Hex.bytesToStringUppercase(data));
        byte globalPacketNumber = data[0];
        byte[] rumbleData = Arrays.copyOfRange(data, 1, 9);

        byte subcommand = data[9];

        if (mcuMode.getAction() == MCUMode.Action.READ_TAG ||
                mcuMode.getAction() == MCUMode.Action.READ_TAG_2 ||
                mcuMode.getAction() == MCUMode.Action.READ_FINISHED) {
            return;
        }

        //Request MCU status
        if (subcommand == 0x01) {
            mcuMode.setAction(MCUMode.Action.REQUEST_STATUS);
        }
        // Request NFC data report
        else if (subcommand == 0x02) {
            byte nfcCommand = data[10];
            switch (nfcCommand) {
                //Start Tag Discovery
                case 0x04:
                case 0x05:
                    mcuMode.setAction(MCUMode.Action.START_TAG_DISCOVERY);
                    break;
                case 0x01:
                    mcuMode.setAction(MCUMode.Action.START_TAG_POLLING);
                    break;
                case 0x02:
                    mcuMode.setAction(MCUMode.Action.NON);
                    break;
                case 0x06:
                    mcuMode.setAction(MCUMode.Action.READ_TAG);
            }
        }
    }

    public synchronized void startFullReport(InputMode mode) {
        if (mode == SIMPLE_HID_MODE) {
            if (fullModeSender != null) {
                fullModeSender.cancel(false);
                fullModeSender = null;
            }
        }
        if (inputMode == SIMPLE_HID_MODE) {
            if (fullModeSender == null || fullModeSender.isCancelled()) {
                fullModeSender = executorService.scheduleWithFixedDelay(
                        new FullReportSender(),
                        0,
                        30,
                        //type.getTypeByte() == 0x03 ? 8333 : 16667,
                        TimeUnit.MILLISECONDS);
            }
        }
        inputMode = mode;
    }


    private static byte timeByte = 0;
    private byte getTimeByte() {
        long nanoTime = System.nanoTime();
        return timeByte++;
    }

    private byte getBatteryReport() {
        /**
         * TODO: Currently returning battery is full all the time
         * Might be improved to return correct battery level
         */
        byte batteryReport = (byte) 0x90; //Battery level. 8=full, 6=medium, 4=low, 2=critical, 0=empty. LSB=Charging.
        if (type != PRO_CONTROLLER) {
            batteryReport = (byte) (batteryReport | 0x0E);
        }

        batteryReport = (byte) (batteryReport | 0x01); //Switch/USB powered
        return batteryReport;
        //return (byte) 0x8E;
    }

    private byte getVibratorData() {
        /**
         * TODO: Currently returning fake vibrator setting
         * might return real values depending on the device
         */
        return (byte) 0xB0;
    }

    float[] accs = new float[3 * 3];
    float[] gyrs = new float[3 * 3];

    private synchronized byte[] getSensorData() {
        int multiplier = 1;
        boolean isPro = type == PRO_CONTROLLER;
        if (type == RIGHT_JOYCON) {
            multiplier = -1;
        }
        byte[] sensorData = new byte[36];
        Arrays.fill(sensorData, (byte) 0);
        SensorEvent[] accEvents = accelerometerEvents.toArray(new SensorEvent[]{});
        accelerometerEvents.clear();
        SensorEvent[] gyrEvents = gyroscopeEvents.toArray(new SensorEvent[]{});
        gyroscopeEvents.clear();

        //log(TAG, "accSize: " + accEvents.length);
        if (accEvents.length > 0) {
            int start = 0;
            int end = accEvents.length - 1;
            //Start

            accs[0] = accEvents[start].values[0];
            accs[1] = accEvents[start].values[1];
            accs[2] = accEvents[start].values[2];
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
            int end = gyrEvents.length - 1;
            //Start
            gyrs[0] = gyrEvents[start].values[0];
            gyrs[1] = gyrEvents[start].values[1];
            gyrs[2] = gyrEvents[start].values[2];
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

        for (int i = 0; i < SAMPLES_PER_INTERVAL; i++) {
            float accX = multiplier * accs[i * 3], accY = accs[i * 3 + 1], accZ = multiplier * accs[i * 3 + 2];
            short rawAccX = (short) clamp(((isPro ? -accZ : accY))
                    * accCoeffs[0], MIN_VALUE, MAX_VALUE);
            short rawAccY = (short) clamp((-accX)
                    * accCoeffs[1], MIN_VALUE, MAX_VALUE);
            short rawAccZ = (short) clamp(((isPro ? accY : accZ))
                    * accCoeffs[2], MIN_VALUE, MAX_VALUE);

            sensorData[0 + i * 12] = (byte) (rawAccX & 0xFF);
            sensorData[1 + i * 12] = (byte) (rawAccX >> 8 & 0xFF);
            sensorData[2 + i * 12] = (byte) (rawAccY & 0xFF);
            sensorData[3 + i * 12] = (byte) (rawAccY >> 8 & 0xFF);
            sensorData[4 + i * 12] = (byte) (rawAccZ & 0xFF);
            sensorData[5 + i * 12] = (byte) (rawAccZ >> 8 & 0xFF);

//            log(TAG, "accRaw: " +
//                    ByteUtils.encodeHexString(sensorData[1 + i * 12]) +
//                    ByteUtils.encodeHexString(sensorData[0 + i * 12]) +
//                    " " +
//                    ByteUtils.encodeHexString(sensorData[3 + i * 12]) +
//                    ByteUtils.encodeHexString(sensorData[2 + i * 12]) +
//                    " " +
//                    ByteUtils.encodeHexString(sensorData[5 + i * 12]) +
//                    ByteUtils.encodeHexString(sensorData[4 + i * 12]) +
//                    " "
//            );

            float gyrX = multiplier * gyrs[i * 3], gyrY = gyrs[i * 3 + 1], gyrZ = multiplier * gyrs[i * 3 + 2];
            short rawGyrX = (short) clamp((isPro ? -gyrZ : gyrY)
                    * gyrCoeffs[0] + gyrOffset[0], MIN_VALUE, MAX_VALUE);
            short rawGyrY = (short) clamp(-gyrX
                    * gyrCoeffs[1] + gyrOffset[1], MIN_VALUE, MAX_VALUE);
            short rawGyrZ = (short) clamp((isPro ? gyrY : gyrZ)
                    * gyrCoeffs[2] + gyrOffset[2], MIN_VALUE, MAX_VALUE);


            sensorData[6 + i * 12] = (byte) (rawGyrX & 0xFF);
            sensorData[7 + i * 12] = (byte) (rawGyrX >> 8 & 0xFF);
            sensorData[8 + i * 12] = (byte) (rawGyrY & 0xFF);
            sensorData[9 + i * 12] = (byte) (rawGyrY >> 8 & 0xFF);
            sensorData[10 + i * 12] = (byte) (rawGyrZ & 0xFF);
            sensorData[11 + i * 12] = (byte) (rawGyrZ >> 8 & 0xFF);

//            log(TAG, "gyrRaw: " +
//                    ByteUtils.encodeHexString(sensorData[7 + i * 12]) +
//                    ByteUtils.encodeHexString(sensorData[6 + i * 12]) +
//                    " " +
//                    ByteUtils.encodeHexString(sensorData[9 + i * 12]) +
//                    ByteUtils.encodeHexString(sensorData[8 + i * 12]) +
//                    " " +
//                    ByteUtils.encodeHexString(sensorData[11 + i * 12]) +
//                    ByteUtils.encodeHexString(sensorData[10 + i * 12]) +
//                    " "
//            );
        }
        //log(TAG, (sensorData));
        //log(TAG, "sensorData: " + Hex.bytesToStringUppercase(sensorData));
        return sensorData;
    }

    private void fillDeviceInformation(byte[] buffer, int index) {
        // Firmware Version
        buffer[index] = 0x04;
        buffer[index + 1] = (byte) 0x06;//0x91;
        //JoyCon Type 1=Left Joy-Con, 2=Right Joy-Con, 3=Pro Controller.
        //buffer[index + 2] = type.getTypeByte();
        buffer[index + 2] = eeprom.read(0x6012, 1)[0];
        //Unknown
        buffer[index + 3] = 0x02;

        // Mac Address
        String localMacAddress = getLocalMacAddress();
        byte[] bytes = MacUtils.parseMacAddress(localMacAddress);
        for (int i = 0; i < 6; i++) {
            buffer[index + 4 + i] = bytes[6 - i - 1];
        }
        //Unknown
        buffer[index + 10] = 0x01;
        //If 01, colors in SPI are used. Otherwise, default ones.
        //  buffer[index + 11] = 0x01;
        buffer[index + 11] = eeprom.read(0x601B, 1)[0];
        ;
    }

    private byte[] fillNFCReport() {
        byte[] buffer = new byte[313];
        if (mcuMode.getAction() == MCUMode.Action.REQUEST_STATUS) {
            buffer[0] = 0x01;
            System.arraycopy(mcuMode.getFwMajor(), 0, buffer, 3, 2);
            System.arraycopy(mcuMode.getFwMinor(), 0, buffer, 5, 2);

            MCUMode.State state = mcuMode.getState();
            switch (state) {
                case NFC:
                    buffer[7] = 0x04;
                    break;
                case BUSY:
                    buffer[7] = 0x06;
                    break;
                case NOT_INITIALIZED:
                case STAND_BY:
                    buffer[7] = 0x01;
                    break;
                default:
                    buffer[7] = (byte) 0xFF;
            }
            int sentCount = mcuMode.getSentCount();
            if (sentCount >= 3) {
                mcuMode.setSentCount(0);
                //mcuMode.setAction(MCUMode.Action.NON);
            } else {
                mcuMode.setSentCount(sentCount + 1);
            }
        } else if (mcuMode.getAction() == MCUMode.Action.NON) {
            buffer[0] = (byte) 0xFF;
        } else if (mcuMode.getAction() == MCUMode.Action.START_TAG_DISCOVERY) {
            //NFC State/Tag Info
            buffer[0] = (byte) 0x2a;
            //Error Code
            buffer[1] = (byte) 0x00;
            //Input type state info
            buffer[2] = (byte) 0x05;
            byte[] bytes = Hex.stringToBytes("093100");
            System.arraycopy(bytes, 0, buffer, 5, bytes.length);
        } else if (mcuMode.getAction() == MCUMode.Action.START_TAG_POLLING) {
            //NFC State/Tag Info
            buffer[0] = (byte) 0x2a;
            //Error Code
            buffer[1] = (byte) 0x00;
            //Input type state info
            buffer[2] = (byte) 0x05;
            if (amiiboBytes != null) {
                byte[] bytes = Hex.stringToBytes("0931090000000101020007");
                System.arraycopy(bytes, 0, buffer, 5, bytes.length);
                System.arraycopy(amiiboBytes, 0, buffer, 5 + bytes.length, 3);
                System.arraycopy(amiiboBytes, 4, buffer, 8 + bytes.length, 4);
            } else {
                byte[] bytes = Hex.stringToBytes("093100");
                System.arraycopy(bytes, 0, buffer, 5, bytes.length);
                service.showAmiiboPicker();
            }
        } else if (mcuMode.getAction() == MCUMode.Action.READ_TAG ||
                mcuMode.getAction() == MCUMode.Action.READ_TAG_2) {
            //NFC TAG read
            buffer[0] = (byte) 0x3a;
            //Error Code
            buffer[1] = (byte) 0x00;
            //Input type state info
            buffer[2] = (byte) 0x07;

            if (mcuMode.getAction() == MCUMode.Action.READ_TAG) {
                byte[] bytes = Hex.stringToBytes("010001310200000001020007");
                System.arraycopy(bytes, 0, buffer, 3, bytes.length);
                System.arraycopy(amiiboBytes, 0, buffer, 3 + bytes.length, 3);
                System.arraycopy(amiiboBytes, 4, buffer, 6 + bytes.length, 4);
                byte[] bytes2 = Hex.stringToBytes("000000007DFDF0793651ABD7466E39C191BABEB856CEEDF1CE44CC75EAFB27094D087AE803003B3C7778860000");
                System.arraycopy(bytes2, 0, buffer, 10 + bytes.length, bytes2.length);
                System.arraycopy(amiiboBytes, 0, buffer, 10 + bytes.length + bytes2.length, 245);
                mcuMode.setAction(MCUMode.Action.READ_TAG_2);
            } else {
                byte[] bytes = Hex.stringToBytes("02000927");
                System.arraycopy(bytes, 0, buffer, 3, bytes.length);
                System.arraycopy(amiiboBytes, 0xF5, buffer, 3 + bytes.length, amiiboBytes.length - 0xF5);
                mcuMode.setAction(MCUMode.Action.READ_FINISHED);
            }
        } else if (mcuMode.getAction() == MCUMode.Action.READ_FINISHED) {
            //NFC State/Tag Info
            buffer[0] = (byte) 0x2a;
            //Error Code
            buffer[1] = (byte) 0x00;
            //Input type state info
            buffer[2] = (byte) 0x05;
            byte[] bytes = Hex.stringToBytes("0931040000000101020007");
            System.arraycopy(bytes, 0, buffer, 5, bytes.length);
            System.arraycopy(amiiboBytes, 0, buffer, 5 + bytes.length, 3);
            System.arraycopy(amiiboBytes, 4, buffer, 8 + bytes.length, 4);
        }

        buffer[312] = ByteUtils.crc8(buffer);
        return buffer;
    }

    private void startHandShake() {
//        try {
//            if (!executorService.isShutdown()) {
//                executorService.schedule(
//                        this::sendHandShake,
//                        WAIT_BEFORE_HANDSHAKE_MS,
//                        TimeUnit.MILLISECONDS);
//            }
//        } catch (RejectedExecutionException e) {
//            log(TAG, "View Closed", e);
//        }
    }

    public void sendHandShake() {
        if (inputMode != STANDARD_FULL_MODE && isConnected()) {
            buttonStates.setLeft_stick_x(-1);
            buttonStates.setLeft_stick_y(1);
            sendShortButton();

            buttonStates.setLeft_stick_x(0);
            buttonStates.setLeft_stick_y(0);
            sendShortButton();

            startHandShake();
        }
    }

    public void sendShortButton() {
        byte[] buttonReportData = new byte[11];
        buttonStates.fillButtonReport(buttonReportData, 0);
        sendReport(BUTTON_REPORT, buttonReportData);
    }

    private void sendFullReport() {
        if (notificationCallBack != null) {
            notificationCallBack.notifyBeforePackage();
        }

        byte[] output;
        if (inputMode == STANDARD_FULL_MODE) {
            output = new byte[48];
        } else if (inputMode == NFC_IR_MODE) {
            output = new byte[48 + 313];
        } else {
            return;
        }
        //Clean Data
        //Arrays.fill(output, (byte) 0);
        output[0] = getTimeByte();
        output[1] = getBatteryReport();
        //Fills 9 bytes of data
        buttonStates.fillFullButtonReport(output, 2);
        //Fill sensor data
        output[11] = getVibratorData();
        byte[] sensorData = getSensorData();
        System.arraycopy(sensorData, 0, output, 12, 36);

        if (inputMode == STANDARD_FULL_MODE) {
            sendReport(FULL_BUTTON_REPORT, output);
        } else if (inputMode == NFC_IR_MODE) {
            System.arraycopy(fillNFCReport(), 0, output, 48, 313);
            sendReport(NFC_IR_REPORT, output);
        }
    }

    public boolean sendReport(int reportId, byte[] data) {
        Log.v(TAG, "Sent Data Report Id: " + ByteUtils.encodeHexString((byte) reportId) + " data: " + Hex.bytesToStringUppercase(data));
        BluetoothHidDevice proxy = getProxy();
        BluetoothDevice remoteDevice = getRemoteDevice();
        if (Objects.nonNull(proxy) && Objects.nonNull(remoteDevice)) {
            try {
                return proxy.sendReport(remoteDevice, reportId, data);
            } catch (SecurityException ex) {
                missingPermission(context, Manifest.permission.BLUETOOTH_CONNECT);
                log(TAG, "Missing permission", ex);
            }
        }
        return false;
    }

    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        if (Objects.nonNull(event) && Objects.nonNull(event.sensor)) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && isAccelerometerEnabled()) {
                accelerometerEvents.add(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && isGyroscopeEnabled()) {
                gyroscopeEvents.add(event);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Data
    public static class ButtonStates {
        public static final int DOWN = 100;
        public static final int UP = 0;
        public static final int STICK_CENTER = 0;
        public static final int STICK_NEGATIVE = -100;
        public static final int STICK_POSITIVE = 100;

        private int up;
        private int down;
        private int left;
        private int right;
        private int y;
        private int a;
        private int b;
        private int x;
        private int minus;
        private int plus;
        private int left_sl;
        private int left_sr;
        private int right_sl;
        private int right_sr;
        private int l;
        private int r;
        private int zl;
        private int zr;
        private int left_stick_button;
        private int left_stick_x;
        private int left_stick_y;
        private int right_stick_button;
        private int right_stick_x;
        private int right_stick_y;
        private int capture;
        private int home;

        private ControllerType type;

        public ButtonStates(ControllerType type) {
            this.type = type;
        }

        public void fillButtonReport(byte[] buffer, int index) {

            int[] buttons = new int[16];
            if (type == LEFT_JOYCON || type == PRO_CONTROLLER) {
                buttons[0] = down;
                buttons[1] = right;
                buttons[2] = left;
                buttons[3] = up;
                buttons[4] = left_sl;
                buttons[5] = left_sr;
                buttons[6] = minus;
                buttons[7] = plus;
                buttons[8] = left_stick_button;
                buttons[9] = right_stick_button;
                buttons[10] = home;
                buttons[11] = capture;
                buttons[12] = l;
                buttons[13] = zl;
                buttons[14] = left_stick_x;
                buttons[15] = left_stick_y;
            } else {
                buttons[0] = b;
                buttons[1] = a;
                buttons[2] = y;
                buttons[3] = x;
                buttons[4] = right_sl;
                buttons[5] = right_sr;
                buttons[6] = minus;
                buttons[7] = plus;
                buttons[8] = left_stick_button;
                buttons[9] = right_stick_button;
                buttons[10] = home;
                buttons[11] = capture;
                buttons[12] = r;
                buttons[13] = zr;
                buttons[14] = right_stick_x;
                buttons[15] = right_stick_y;
            }

            buffer[index] = 0;
            buffer[index] |= buttons[0] == 0 ? 0 : DOWN_BIT;
            buffer[index] |= buttons[1] == 0 ? 0 : RIGHT_BIT;
            buffer[index] |= buttons[2] == 0 ? 0 : LEFT_BIT;
            buffer[index] |= buttons[3] == 0 ? 0 : UP_BIT;
            buffer[index] |= buttons[4] == 0 ? 0 : SL_BIT;
            buffer[index] |= buttons[5] == 0 ? 0 : SR_BIT;

            buffer[index + 1] = 0;
            buffer[index + 1] |= buttons[6] == 0 ? 0 : MINUS_BIT;
            buffer[index + 1] |= buttons[7] == 0 ? 0 : PLUS_BIT;
            buffer[index + 1] |= buttons[8] == 0 ? 0 : LEFT_STICK_BIT;
            buffer[index + 1] |= buttons[9] == 0 ? 0 : RIGHT_STICK_BIT;
            buffer[index + 1] |= buttons[10] == 0 ? 0 : HOME_BIT;
            buffer[index + 1] |= buttons[11] == 0 ? 0 : CAPTURE_BIT;
            buffer[index + 1] |= buttons[12] == 0 ? 0 : L_R_BIT;
            buffer[index + 1] |= buttons[13] == 0 ? 0 : ZL_ZR_BIT;


            if (buttons[14] > 0) {
                if (buttons[15] > 0) {
                    buffer[index + 2] = 3;
                } else if (buttons[15] < 0) {
                    buffer[index + 2] = 1;
                } else {
                    buffer[index + 2] = 2;
                }
            } else if (buttons[14] < 0) {
                if (buttons[15] > 0) {
                    buffer[index + 2] = 5;
                } else if (buttons[15] < 0) {
                    buffer[index + 2] = 7;
                } else {
                    buffer[index + 2] = 6;
                }
            } else {
                if (buttons[15] > 0) {
                    buffer[index + 2] = 4;
                } else if (buttons[15] < 0) {
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

        public void fillFullButtonReport(byte[] buffer, int index) {
            Arrays.fill(buffer, index, index + 9, (byte) 0);

            //Right joycon bits
            if (type == RIGHT_JOYCON || type == PRO_CONTROLLER) {
                buffer[index] |= y == 0 ? 0 : FULL_Y_BIT;
                buffer[index] |= x == 0 ? 0 : FULL_X_BIT;
                buffer[index] |= b == 0 ? 0 : FULL_B_BIT;
                buffer[index] |= a == 0 ? 0 : FULL_A_BIT;
                buffer[index] |= right_sl == 0 ? 0 : FULL_SL_BIT;
                buffer[index] |= right_sr == 0 ? 0 : FULL_SR_BIT;
                buffer[index] |= r == 0 ? 0 : FULL_L_R_BIT;
                buffer[index] |= zr == 0 ? 0 : FULL_ZL_ZR_BIT;

                //RIGHT Stick bytes for analog stick
                int dataX = Math.round(((right_stick_x + 100) / 200f) * 4095);
                int dataY = Math.round(((right_stick_y + 100) / 200f) * 4095);
                buffer[index + 6] = (byte) (dataX & 0xFF);
                buffer[index + 7] = (byte) ((dataX >> 8) & 0xF);
                buffer[index + 7] |= (byte) ((dataY & 0xF) << 4);
                buffer[index + 8] = (byte) ((dataY >> 4) & 0xFF);
            }

            //Button status shared
            buffer[index + 1] |= minus == 0 ? 0 : FULL_MINUS_BIT;
            buffer[index + 1] |= plus == 0 ? 0 : FULL_PLUS_BIT;
            buffer[index + 1] |= right_stick_button == 0 ? 0 : FULL_RIGHT_STICK_BIT;
            buffer[index + 1] |= left_stick_button == 0 ? 0 : FULL_LEFT_STICK_BIT;
            buffer[index + 1] |= home == 0 ? 0 : FULL_HOME_BIT;
            buffer[index + 1] |= capture == 0 ? 0 : FULL_CAPTURE_BIT;

            //Left joycon bits
            if (type == LEFT_JOYCON || type == PRO_CONTROLLER) {
                buffer[index + 2] |= down == 0 ? 0 : FULL_DOWN_BIT;
                buffer[index + 2] |= right == 0 ? 0 : FULL_RIGHT_BIT;
                buffer[index + 2] |= left == 0 ? 0 : FULL_LEFT_BIT;
                buffer[index + 2] |= up == 0 ? 0 : FULL_UP_BIT;
                buffer[index + 2] |= left_sl == 0 ? 0 : FULL_SL_BIT;
                buffer[index + 2] |= left_sr == 0 ? 0 : FULL_SR_BIT;
                buffer[index + 2] |= l == 0 ? 0 : FULL_L_R_BIT;
                buffer[index + 2] |= zl == 0 ? 0 : FULL_ZL_ZR_BIT;

                //LEFT Stick bytes for analog stick
                int dataX = Math.round(((left_stick_x + 100) / 200f) * 4095);
                int dataY = Math.round(((left_stick_y + 100) / 200f) * 4095);
                buffer[index + 3] = (byte) (dataX & 0xFF);
                buffer[index + 4] = (byte) ((dataX >> 8) & 0xF);
                buffer[index + 4] |= (byte) ((dataY & 0xF) << 4);
                buffer[index + 5] = (byte) ((dataY >> 4) & 0xFF);
            }
        }
    }

    private class FullReportSender implements Runnable {
        @Override
        public void run() {
            sendFullReport();
        }
    }
}
