package com.rdapps.gamepad.command.handler.subcommand;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.InputReportMode;
import com.rdapps.gamepad.report.OutputReport;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;
import static com.rdapps.gamepad.report.InputReportMode.NFC_IR_MODE;
import static com.rdapps.gamepad.report.InputReportMode.SIMPLE_HID;
import static com.rdapps.gamepad.report.InputReportMode.STANDARD_FULL_MODE;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x03-set-input-report-mode
 * //TODO x31 input report has all zeroes for IR/NFC data if a 11 ouput report with subcmd 03 00/01/02/03 was not sent before.
 */
class RequestInputReportModeHandler implements SubCommandHandler {
    private final static String TAG = RequestInputReportModeHandler.class.getName();
    private final static byte ACK = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(JoyController joyController, OutputReport outputReport) {
        InputReport inputReport = new InputReport(SUBCOMMAND_REPLY_REPORT);
        inputReport.fillAckByte(ACK);
        inputReport.fillSubCommand(outputReport.getSubCommandId());
        byte inputReportModeByte = outputReport.getData()[10];
        InputReportMode inputReportMode = InputReportMode.getInputReportMode(inputReportModeByte);

        JoyControllerState state = joyController.getState();

        if ((inputReportMode == STANDARD_FULL_MODE) || (inputReportMode == NFC_IR_MODE)) {
            state.setInputReportMode(inputReportMode);
            if (!joyController.isInFullMode()) {
                joyController.startFullReportMode();
            }
        } else if (inputReportMode == SIMPLE_HID) {
            joyController.stopFullReportMode();
            log(TAG, "SIMPLE HID Report Mode: " + outputReport.toString(), true);
        } else {
            log(TAG, "Unknown Report Mode: " + outputReport.toString(), true);
        }
        return inputReport;
    }
}
