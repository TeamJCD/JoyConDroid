package com.rdapps.gamepad.report;

import java.util.Arrays;
import java.util.Optional;

public enum OutputReportMode {
    /**
     * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_notes.md#standard-input-report-format
     */
    RUMBLE_AND_SUBCOMMAND(0x01),
    NFC_IR_MCU_FW_UPDATE_PACKET(0x03),
    RUMBLE_ONLY(0x10),
    REQUEST_NFC_IR_MCU_DATA(0x11),
    UNKNOWN(0x12);

    private final byte arg;

    OutputReportMode(int arg) {
        this.arg = (byte) arg;
    }

    public static Optional<OutputReportMode> getReportMode(byte reportId) {
        return Arrays.asList(values())
                .stream()
                .filter(mode -> mode.arg == reportId)
                .findAny();
    }
}
