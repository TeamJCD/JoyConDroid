package com.rdapps.gamepad.nx.constant;

public class NxConstants {

    public static final byte BUTTON_REPORT = 0x3F;
    public static final byte SUBCOMMAND_REPLY_REPORT = 0x21;
    public static final byte FULL_BUTTON_REPORT = 0x30;
    public static final byte NFC_IR_REPORT = 0x31;
    public static final byte SIMPLE_HID_REPORT = 0x3F;

    //COMMANDS
    public static final byte REQUEST_RUMBLE_AND_SUBCOMMAND = 0x01;
    public static final byte REQUEST_RUMBLE_ONLY = 0x10;
    public static final byte REQUEST_NFC_IR_MCU = 0x11;

    //SUBCOMMANDS
    public static final byte CONTROLLER_STATE = 0x00;
    public static final byte BLUETOOTH_MANUAL_PAIRING = 0x01;
    public static final byte REQUEST_DEVICE_INFO = 0x02;
    public static final byte REQUEST_SET_SHIPMENT = 0x08;
    public static final byte REQUEST_SPI_FLASH_READ = 0x10;
    public static final byte REQUEST_SPI_FLASH_WRITE = 0x11;
    public static final byte REQUEST_INPUT_REPORT_MODE = 0x03;
    public static final byte REQUEST_TRIGGER_BUTTONS = 0x04;
    public static final byte REQUEST_AXIS_SENSOR = 0x40;
    public static final byte SET_IMU_SENSITIVITY = 0x41;
    public static final byte REQUEST_VIBRATION = 0x48;
    public static final byte REQUEST_SET_PLAYER_LIGHTS = 0x30;
    public static final byte REQUEST_SET_NFC_IR_CONFIGURATION = 0x21;
    public static final byte REQUEST_SET_NFC_IR_STATE = 0x22;

    //ACK
    public static final byte ACK = (byte) 0x80;


    //Button Report Bits
    public static final byte DOWN_BIT = 0x01;
    public static final byte RIGHT_BIT = 0x02;
    public static final byte LEFT_BIT = 0x04;
    public static final byte UP_BIT = 0x08;
    public static final byte SL_BIT = 0x10;
    public static final byte SR_BIT = 0x20;

    public static final byte MINUS_BIT = 0x01;
    public static final byte PLUS_BIT = 0x02;
    public static final byte LEFT_STICK_BIT = 0x04;
    public static final byte RIGHT_STICK_BIT = 0x08;
    public static final byte HOME_BIT = 0x10;
    public static final byte CAPTURE_BIT = 0x20;
    public static final byte L_R_BIT = 0x40;
    public static final byte ZL_ZR_BIT = (byte) 0x80;

    //Full report bits
    public static final byte FULL_MINUS_BIT = 0x01;
    public static final byte FULL_PLUS_BIT = 0x02;
    public static final byte FULL_RIGHT_STICK_BIT = 0x04;
    public static final byte FULL_LEFT_STICK_BIT = 0x08;
    public static final byte FULL_HOME_BIT = 0x10;
    public static final byte FULL_CAPTURE_BIT = 0x20;

    public static final byte FULL_DOWN_BIT = 0x01;
    public static final byte FULL_RIGHT_BIT = 0x04;
    public static final byte FULL_LEFT_BIT = 0x08;
    public static final byte FULL_UP_BIT = 0x02;
    public static final byte FULL_SL_BIT = 0x20;
    public static final byte FULL_SR_BIT = 0x10;
    public static final byte FULL_L_R_BIT = 0x40;
    public static final byte FULL_ZL_ZR_BIT = (byte) 0x80;

    public static final byte FULL_Y_BIT = 0x01;
    public static final byte FULL_X_BIT = 0x02;
    public static final byte FULL_B_BIT = 0x04;
    public static final byte FULL_A_BIT = 0x08;
    //
}
