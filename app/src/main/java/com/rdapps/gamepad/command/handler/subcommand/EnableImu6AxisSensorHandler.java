package com.rdapps.gamepad.command.handler.subcommand;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

/**
 * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md#subcommand-0x40-enable-imu-6-axis-sensor
 */
class EnableImu6AxisSensorHandler implements SubCommandHandler {
    private static final String TAG = EnableImu6AxisSensorHandler.class.getName();
    private static final byte ACK = (byte) 0x80;

    @Override
    public InputReport handleRumbleAndSubCommand(
            JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        byte mode = outputReport.getData()[10];
        JoyControllerState.SensorMode sensorMode;
        if (mode == 0x01) {
            sensorMode = JoyControllerState.SensorMode.STANDARD;
        } else if (mode >= 0x02 && mode <= 0x05) {
            sensorMode = JoyControllerState.SensorMode.QUATERNION;
        } else {
            sensorMode = JoyControllerState.SensorMode.INACTIVE;
        }
        log(TAG, "6AxisSensor Mode: " + sensorMode);
        joyController.setSensorMode(sensorMode);

        return subCommandReply;
    }
}
