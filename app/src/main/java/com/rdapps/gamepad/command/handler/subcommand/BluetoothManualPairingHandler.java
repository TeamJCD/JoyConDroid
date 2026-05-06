package com.rdapps.gamepad.command.handler.subcommand;

import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;
import java.util.Arrays;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x01-bluetooth-manual-pairing
 */
class BluetoothManualPairingHandler implements SubCommandHandler {

    private static final byte ACK_WITH_DATA = (byte) 0x81;
    private static final byte ACK_SIMPLE = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(
            JoyController joyController, OutputReport outputReport) {
        InputReport subcommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subcommandReply.fillSubCommand(outputReport.getSubCommandId());

        byte pairRequestType = outputReport.getData()[10];
        switch (pairRequestType) {
            case 0x01:
                // Send our BT MAC in Little-Endian
                subcommandReply.fillAckByte(ACK_WITH_DATA);
                byte[] replyData = new byte[32];
                replyData[0] = 0x01;
                byte[] macBytes = joyController.getState().getMacBytes();
                for (int i = 0; i < 6; i++) {
                    replyData[1 + i] = macBytes[5 - i];
                }
                // bytes 7-31: descriptor, zeroed
                subcommandReply.fillData(14, replyData);
                break;
            case 0x02:
                // Send LTK XOR 0xAA (all-zero LTK → all 0xAA)
                subcommandReply.fillAckByte(ACK_WITH_DATA);
                byte[] ltk = new byte[16];
                Arrays.fill(ltk, (byte) 0xAA);
                subcommandReply.fillData(14, ltk);
                break;
            default:
                // type 0x03 (save pairing info) and unknown types: simple ACK
                subcommandReply.fillAckByte(ACK_SIMPLE);
                break;
        }

        return subcommandReply;
    }
}
