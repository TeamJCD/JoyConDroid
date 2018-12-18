package com.rdapps.gamepad.command.handler.subcommand;

import com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.NON;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.MCUState.STAND_BY;
import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

class SetNFCIRMCUStateHandler implements SubCommandHandler {
    private final static String TAG = SetNFCIRMCUStateHandler.class.getName();
    private final static byte ACK = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        JoyControllerState state = joyController.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        byte[] data = outputReport.getData();
        byte arg = data[10];
        if (arg == 0x00) {
            nfcIrMcu.setMcuState(STAND_BY);
        } else if (arg == 0x01) {
            nfcIrMcu.setAction(NON);
            nfcIrMcu.setMcuState(STAND_BY);
        } else {
            log(TAG, "Unknown mcu state : " + arg, true);
        }
        return subCommandReply;
    }
}
