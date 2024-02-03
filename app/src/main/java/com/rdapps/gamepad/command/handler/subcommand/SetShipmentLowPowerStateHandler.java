package com.rdapps.gamepad.command.handler.subcommand;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

import com.rdapps.gamepad.memory.ControllerMemory;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;
import com.rdapps.gamepad.util.ByteUtils;

class SetShipmentLowPowerStateHandler implements SubCommandHandler {
    private static final String TAG = SetShipmentLowPowerStateHandler.class.getName();
    private static final byte ACK = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(
            JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        ControllerMemory controllerMemory = joyController.getControllerMemory();
        byte[] data = outputReport.getData();
        byte arg = data[10];
        if (arg == 0x00) {
            controllerMemory.write(0x5000, new byte[]{(byte) 0xFF});
        } else {
            controllerMemory.write(0x5000, new byte[]{(byte) 0x01});
        }
        log(TAG, "Set shipment input: " + ByteUtils.encodeHexString(arg));
        return subCommandReply;
    }
}
