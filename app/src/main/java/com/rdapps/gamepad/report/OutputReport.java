package com.rdapps.gamepad.report;

import com.rdapps.gamepad.util.ByteUtils;
import com.rdapps.gamepad.vibrator.RumbleData;

import java.util.Arrays;
import java.util.Optional;

import lombok.Data;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.report.OutputReportMode.UNKNOWN;

@Data
public class OutputReport {
    private static final String TAG = OutputReport.class.getName();

    private final byte[] data;
    private byte reportId;
    private OutputReportMode reportMode;

    public OutputReport(byte reportId, byte[] data) {
        Optional<OutputReportMode> reportMode = OutputReportMode.getReportMode(reportId);
        if (reportMode.isPresent()) {
            this.reportMode = reportMode.get();
        } else {
            this.reportMode = UNKNOWN;
        }

        this.reportId = reportId;
        this.data = data;

        if (this.reportMode == UNKNOWN) {
            log(TAG, toString(), true);
        }
    }

    public byte getGlobalPacketNumber() {
        return data[0];
    }

    public RumbleData getRumbleData() {
        return new RumbleData(Arrays.copyOfRange(data, 1, 9));
    }

    public byte getSubCommandId() {
        return data[9];
    }

    public String toString() {
        return "Output Report Id: " + ByteUtils.encodeHexString(reportId) +
                " data: " + ByteUtils.bytesToStringUppercase(data);
    }
}
