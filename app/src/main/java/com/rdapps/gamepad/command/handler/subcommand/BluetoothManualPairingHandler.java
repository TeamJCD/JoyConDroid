package com.rdapps.gamepad.command.handler.subcommand;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x01-bluetooth-manual-pairing
 */
class BluetoothManualPairingHandler implements SubCommandHandler {
    private static final String TAG = BluetoothManualPairingHandler.class.getName();
    private static final byte ACK = (byte) 0x81;

    @Override
    public InputReport handleRumbleAndSubCommand(
            JoyController joyController, OutputReport outputReport) {
        // TODO FIX THIS
        InputReport subcommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subcommandReply.fillAckByte(ACK);
        subcommandReply.fillSubCommand(outputReport.getSubCommandId());
        log(TAG, "Got Bluetooth Pairing SubCommand: " + outputReport.toString(), true);
        return subcommandReply;
    }
}
