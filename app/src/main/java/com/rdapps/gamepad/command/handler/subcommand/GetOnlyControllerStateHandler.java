package com.rdapps.gamepad.command.handler.subcommand;

import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x00-get-only-controller-state
 */
public class GetOnlyControllerStateHandler implements SubCommandHandler {
    private static final byte ACK = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(
            JoyController joyController, OutputReport outputReport) {
        InputReport inputReport = new InputReport(SUBCOMMAND_REPLY_REPORT);
        inputReport.fillAckByte(ACK);
        inputReport.fillSubCommand(outputReport.getSubCommandId());
        inputReport.fillData(14, new byte[]{0x03});
        return inputReport;
    }
}
