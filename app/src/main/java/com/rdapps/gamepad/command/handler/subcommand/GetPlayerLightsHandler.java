package com.rdapps.gamepad.command.handler.subcommand;

import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x31-get-player-lights
 */
class GetPlayerLightsHandler implements SubCommandHandler {
    private static final String TAG = GetPlayerLightsHandler.class.getName();
    private static final byte ACK = (byte) 0xB0;

    @Override
    public InputReport handleRumbleAndSubCommand(
            JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        JoyControllerState state = joyController.getState();
        byte playerLights = state.getPlayerLights();
        subCommandReply.fillData(14, new byte[]{playerLights});
        return subCommandReply;
    }
}
