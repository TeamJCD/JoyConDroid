package com.rdapps.gamepad.command.handler.subcommand;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

class SetIMU6AxisSensitivityHandler implements SubCommandHandler {
    private final static String TAG = SetIMU6AxisSensitivityHandler.class.getName();
    private final static byte ACK = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        byte sensitivity = outputReport.getData()[10];
        log(TAG, "6AxisSensor Sensitivity: " + sensitivity);
        //TODO set sensitivity
        return subCommandReply;
    }
}
