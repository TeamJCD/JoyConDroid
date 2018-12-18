package com.rdapps.gamepad.command.handler;

import com.rdapps.gamepad.amiibo.AmiiboConfig;
import com.rdapps.gamepad.button.ButtonState;
import com.rdapps.gamepad.command.handler.subcommand.SubCommand;
import com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.InputReportMode;
import com.rdapps.gamepad.report.OutputReport;
import com.rdapps.gamepad.report.OutputReportMode;

import java.util.Arrays;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.NON;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.READ_TAG;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.READ_TAG_2;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.READ_TAG_FINISHED;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.REQUEST_STATUS;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.START_TAG_DISCOVERY;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.START_TAG_DISCOVERY_AUTO_MOVE;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.START_TAG_POLLING;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.WRITE_TAG;
import static java.util.Arrays.asList;

public class OutputHandler {
    private static final String TAG = OutputHandler.class.getName();
    private JoyController joyController;

    public OutputHandler(JoyController joyController) {
        this.joyController = joyController;
    }

    public void handleOutputReport(OutputReport outputReport) {
        OutputReportMode reportMode = outputReport.getReportMode();
        switch (reportMode) {
            case RUMBLE_ONLY:
                //TODO implement rumble
                break;
            case RUMBLE_AND_SUBCOMMAND:
                handleRumbleAndSubCommand(outputReport);
                break;
            case REQUEST_NFC_IR_MCU_DATA:
                handleRequestNFCIRMCUData(outputReport);
                break;
            case NFC_IR_MCU_FW_UPDATE_PACKET:
                handleNFCIRMCUFWUpdatePacket(outputReport);
                break;
            case UNKNOWN:
            default:
                log(TAG, "Could Not Handle Output:" + outputReport.toString(), true);
                break;
        }
    }

    private void handleRumbleAndSubCommand(OutputReport outputReport) {
        SubCommand subCommand = SubCommand.getSubCommand(outputReport.getSubCommandId());
        InputReport subCommandReply = subCommand.getHandler()
                .handleRumbleAndSubCommand(joyController, outputReport);

        JoyControllerState state = joyController.getState();
        ControllerType controllerType = joyController.getControllerType();
        ButtonState buttonState = joyController.getButtonState();
        subCommandReply.fillTime(state);
        subCommandReply.fillBattery(state);
        subCommandReply.fillConnectionInfo(state);
        subCommandReply.fillFullButtonReport(controllerType, buttonState);
        subCommandReply.fillVibratorData(state);

        joyController.sendReport(subCommandReply);
    }

    private void handleNFCIRMCUFWUpdatePacket(OutputReport outputReport) {
    }

    private void handleRequestNFCIRMCUData(OutputReport outputReport) {
        log(TAG, "NFC/IR Data: " + outputReport.toString());
        JoyControllerState state = joyController.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        AmiiboConfig amiiboConfig = joyController.getAmiiboConfig();

        byte[] data = outputReport.getData();

        byte globalPacketNumber = data[0];
        byte[] rumbleData = Arrays.copyOfRange(data, 1, 9);

        byte subCommand = data[9];
        byte nfcCommand = data[10];

        log(TAG, "subCommand: " + subCommand + " nfcCommand: " + nfcCommand);

        if (asList(READ_TAG, READ_TAG_2, READ_TAG_FINISHED).contains(nfcIrMcu.getAction())) {
            return;
        }

        //Request MCU status
        if (subCommand == 0x01) {
            if (!asList(READ_TAG_FINISHED, NON).contains(nfcIrMcu.getAction())) {
                return;
            } else {
                if (state.getInputReportMode() == InputReportMode.STANDARD_FULL_MODE) {
                    amiiboConfig.removeAmiiboBytes();
                }
            }
            nfcIrMcu.setAction(REQUEST_STATUS);
        }
        // Request NFC data report
        else if (subCommand == 0x02) {
            switch (nfcCommand) {
                //Start Tag Discovery
                case 0x04:
                    if (nfcIrMcu.getAction() != START_TAG_DISCOVERY_AUTO_MOVE) {
                        nfcIrMcu.setAction(START_TAG_DISCOVERY);
                    }
                    break;
                case 0x01:
                    nfcIrMcu.setAction(START_TAG_POLLING);
                    break;
                case 0x02:
                    nfcIrMcu.setAction(NON);
                    break;
                case 0x06:
                    nfcIrMcu.setAction(READ_TAG);
                    break;
                case 0x08:
                    nfcIrMcu.setAction(WRITE_TAG);
                    break;
                default:
                    log(TAG, "Unknown Action : " + nfcCommand, true);
            }
        } else {
            log(TAG, "Unknown MCU SubCommand : " + subCommand, true);
        }
    }
}
