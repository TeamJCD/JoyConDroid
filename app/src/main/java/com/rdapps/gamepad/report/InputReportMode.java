package com.rdapps.gamepad.report;

import java.util.Arrays;

public enum InputReportMode {
    /**
     * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md
     */
    ACTIVE_PULLING_NFC_IR_DATA(0x00),
    ACTIVE_PULLING_NFC_IR_MCU(0x01),
    ACTIVE_PULLING_NFC_IR_SPECIFIC(0x02),
    ACTIVE_PULLING_IR_DATA(0x03),
    MCU_UPDATE_STATE(0x23),
    STANDARD_FULL_MODE(0x30),
    NFC_IR_MODE(0x31),
    UNKNOWN_1(0x33),
    UNKNOWN_2(0x35),
    SIMPLE_HID(0x3F),
    UNKNOWN(0xFF);

    private byte arg;

    InputReportMode(int arg) {
        this.arg = (byte) arg;
    }

    public static InputReportMode getInputReportMode(byte b) {
        return Arrays.asList(values())
                .stream()
                .filter(inputReportMode -> inputReportMode.arg == b)
                .findAny()
                .orElse(UNKNOWN);
    }
}
