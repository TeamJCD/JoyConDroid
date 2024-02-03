package com.rdapps.gamepad.command.handler.subcommand;

import static com.rdapps.gamepad.report.InputReport.Type.SUBCOMMAND_REPLY_REPORT;

import com.rdapps.gamepad.memory.ControllerMemory;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.OutputReport;

class RequestDeviceInfoHandler implements SubCommandHandler {
    private static final byte ACK = (byte) 0x82;

    @Override
    public InputReport handleRumbleAndSubCommand(
            JoyController joyController, OutputReport outputReport) {
        InputReport subCommandReply = new InputReport(SUBCOMMAND_REPLY_REPORT);
        subCommandReply.fillAckByte(ACK);
        subCommandReply.fillSubCommand(outputReport.getSubCommandId());
        fillDeviceInformation(joyController, subCommandReply);
        return subCommandReply;
    }

    private void fillDeviceInformation(JoyController joyController, InputReport subcommandReply) {
        ControllerType controllerType = joyController.getControllerType();
        byte[] deviceInfo = new byte[12];
        //Firmware Version
        if (true/*controllerType == PRO_CONTROLLER*/) {
            //TODO: firmware version 0x04 0x06 has new gyro setup.
            deviceInfo[0] = 0x03;
            deviceInfo[1] = 0x48;
        } else {
            deviceInfo[0] = 0x04;
            deviceInfo[1] = 0x06;
        }
        //JoyCon Type 1=Left Joy-Con, 2=Right Joy-Con, 3=Pro Controller.
        deviceInfo[2] = controllerType.getTypeByte();
        //deviceInfo[2] = controllerMemory.read(0x6012, 1)[0];
        //Unknown
        deviceInfo[3] = 0x02;

        // Mac Address
        JoyControllerState state = joyController.getState();
        byte[] macBytes = state.getMacBytes();
        for (int i = 0; i < 6; i++) {
            deviceInfo[4 + i] = macBytes[6 - i - 1];
        }
        //Unknown
        deviceInfo[10] = 0x01;
        //If 01, colors in SPI are used. Otherwise, default ones.
        //  buffer[index + 11] = 0x01;
        ControllerMemory controllerMemory = joyController.getControllerMemory();
        deviceInfo[11] = controllerMemory.read(0x601B, 1)[0];
        subcommandReply.fillData(14, deviceInfo);
    }
}
