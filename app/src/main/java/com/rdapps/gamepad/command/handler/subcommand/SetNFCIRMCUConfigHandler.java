package com.rdapps.gamepad.command.handler.subcommand;

import com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;
import com.rdapps.gamepad.util.ByteUtils;

import java.util.Arrays;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.MCUState.NFC;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.MCUState.STAND_BY;
import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x21-set-nfcir-mcu-configuration
 */
class SetNFCIRMCUConfigHandler implements SubCommandHandler {
    private final static String TAG = SetNFCIRMCUConfigHandler.class.getName();
    private final static byte ACK = (byte) 0xA0;

    @Override
    public InputReport handleRumbleAndSubCommand(JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        fillMCUStatus(joyController, subCommandReply);
        JoyControllerState state = joyController.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        byte[] buffer = subCommandReply.getBuffer();
        byte[] data = outputReport.getData();
        byte mcucmd = data[10];
        byte mcusubcmd = data[11];
        byte mode = data[12];

        if (mcucmd == 0x21) {
            if (mcusubcmd == 0) {
                if (mode == 0) {
                    nfcIrMcu.setMcuState(STAND_BY);
                } else if (mode == 4) {
                    nfcIrMcu.setMcuState(NFC);
                } else {
                    log(TAG, "Unknown NFC Mode " + mode, true);
                }
            } else {
                log(TAG, "Unknown MCU Config SubCommand" + mcusubcmd, true);
            }
        } else {
            log(TAG, "Unknown MCU Config Command" + mcucmd, true);
        }

        buffer[47] = ByteUtils.crc8(Arrays.copyOfRange(buffer, 14, buffer.length));
        return subCommandReply;
    }

    private void fillMCUStatus(JoyController joyController, InputReport subCommandReply) {
        JoyControllerState state = joyController.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        byte[] buffer = subCommandReply.getBuffer();
        buffer[14] = 0x01;
        buffer[15] = 0x00;
        buffer[16] = 0x00;
        System.arraycopy(nfcIrMcu.getFirmwareMajor(), 0, buffer, 17, 2);
        System.arraycopy(nfcIrMcu.getFirmwareMinor(), 0, buffer, 19, 2);
        buffer[21] = nfcIrMcu.getMcuState().getByte();
    }
}
