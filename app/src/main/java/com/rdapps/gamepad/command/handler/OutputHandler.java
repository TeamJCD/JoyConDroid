package com.rdapps.gamepad.command.handler;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.NON;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.READ_TAG;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.READ_TAG_2;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.READ_TAG_FINISHED;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.REQUEST_STATUS;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.START_TAG_DISCOVERY;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.START_TAG_POLLING;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.WRITE_TAG_ACK;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.WRITE_TAG_AWAITING;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.WRITE_TAG_SETUP;
import static java.util.Arrays.asList;

import com.google.android.gms.common.util.Hex;
import com.rdapps.gamepad.amiibo.AmiiboConfig;
import com.rdapps.gamepad.button.ButtonState;
import com.rdapps.gamepad.command.handler.subcommand.SubCommand;
import com.rdapps.gamepad.nfcirmcu.NfcIrMcu;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.InputReportMode;
import com.rdapps.gamepad.report.OutputReport;
import com.rdapps.gamepad.report.OutputReportMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputHandler {
    private static final String TAG = OutputHandler.class.getName();
    private final JoyController joyController;

    public OutputHandler(JoyController joyController) {
        this.joyController = joyController;
    }

    public void handleOutputReport(OutputReport outputReport) {
        OutputReportMode reportMode = outputReport.getReportMode();
        switch (reportMode) {
            case RUMBLE_ONLY:
                joyController.rumble(outputReport.getRumbleData());
                break;
            case RUMBLE_AND_SUBCOMMAND:
                joyController.rumble(outputReport.getRumbleData());
                handleRumbleAndSubCommand(outputReport);
                break;
            case REQUEST_NFC_IR_MCU_DATA:
                handleRequestNfcIrMcuData(outputReport);
                break;
            case NFC_IR_MCU_FW_UPDATE_PACKET:
                handleNfcIrMcuFwUpdatePacket(outputReport);
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

    private void handleNfcIrMcuFwUpdatePacket(OutputReport outputReport) {
    }

    private void handleRequestNfcIrMcuData(OutputReport outputReport) {
        log(TAG, "NFC/IR Data: " + outputReport.toString());
        JoyControllerState state = joyController.getState();
        NfcIrMcu nfcIrMcu = state.getNfcIrMcu();
        final AmiiboConfig amiiboConfig = joyController.getAmiiboConfig();

        byte[] data = outputReport.getData();

        byte globalPacketNumber = data[0];
        byte[] rumbleData = Arrays.copyOfRange(data, 1, 9);

        byte subCommand = data[9];
        byte nfcCommand = data[10];

        log(TAG, "subCommand: " + subCommand + " nfcCommand: " + nfcCommand);

        if (asList(READ_TAG, READ_TAG_2).contains(nfcIrMcu.getAction())) {
            return;
        }

        // During write setup/awaiting/ack, only process incoming 0x08 write packets
        boolean inWriteFlow = asList(WRITE_TAG_SETUP, WRITE_TAG_AWAITING, WRITE_TAG_ACK)
                .contains(nfcIrMcu.getAction());
        if (inWriteFlow && !(subCommand == 0x02 && nfcCommand == 0x08)) {
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
        } else if (subCommand == 0x02) { // Request NFC data report
            switch (nfcCommand) {
                //Start Tag Discovery
                case 0x04:
                    if (nfcIrMcu.getAction() == NON || nfcIrMcu.getAction() == REQUEST_STATUS) {
                        nfcIrMcu.setAction(START_TAG_DISCOVERY);
                    }
                    break;
                case 0x01:
                    nfcIrMcu.setFirstPollSent(false);
                    nfcIrMcu.setAction(START_TAG_POLLING);
                    break;
                case 0x02:
                    nfcIrMcu.setFirstPollSent(false);
                    nfcIrMcu.setAction(NON);
                    break;
                case 0x06:
                    // Non-zero UID at data[17..23] means write setup, zeros mean read
                    boolean isWrite = false;
                    for (int i = 17; i < 24 && i < data.length; i++) {
                        if (data[i] != 0) {
                            isWrite = true;
                            break;
                        }
                    }
                    String uid = data.length >= 24
                            ? Hex.bytesToStringUppercase(Arrays.copyOfRange(data, 17, 24))
                            : "?";
                    log(TAG, "NFC 0x06: UID=" + uid
                            + " → " + (isWrite ? "WRITE_TAG_SETUP" : "READ_TAG"));
                    if (isWrite) {
                        nfcIrMcu.setWriteBuffer(new ArrayList<>());
                        nfcIrMcu.setAckSeqNo(0);
                        nfcIrMcu.setLastWritePacketReceived(false);
                        nfcIrMcu.setRemoveFramesRemaining(0);
                        nfcIrMcu.setAction(WRITE_TAG_SETUP);
                    } else {
                        nfcIrMcu.setAction(READ_TAG);
                    }
                    break;
                case 0x08:
                    handleWritePacket(data, nfcIrMcu, amiiboConfig);
                    break;
                default:
                    log(TAG, "Unknown Action : " + nfcCommand, true);
            }
        } else {
            log(TAG, "Unknown MCU SubCommand : " + subCommand, true);
        }
    }

    private void handleWritePacket(byte[] data, NfcIrMcu nfcIrMcu, AmiiboConfig amiiboConfig) {
        // data[11] = seqNo, data[13] = endFlag, data[14] = payloadLen, data[15..] = payload
        if (data.length < 15) {
            return;
        }
        int seqNo = data[11] & 0xFF;
        byte endFlag = data[13];
        int payloadLen = data[14] & 0xFF;

        if (seqNo == 0 && endFlag == 0x08) {
            // Single-packet write (edge case, never seen in practice)
            if (data.length >= 15 + payloadLen) {
                byte[] payload = Arrays.copyOfRange(data, 15, 15 + payloadLen);
                log(TAG, "NFC write: single-packet write, payload="
                        + payload.length + " bytes, processing");
                processNfcWrite(payload, amiiboConfig);
            }
            nfcIrMcu.setAckSeqNo(0);
            nfcIrMcu.setLastWritePacketReceived(true);
            nfcIrMcu.setRemoveFramesRemaining(4);
        } else if (seqNo == nfcIrMcu.getAckSeqNo() + 1) {
            // Next packet in sequence – append payload
            List<Byte> buf = nfcIrMcu.getWriteBuffer();
            int limit = Math.min(15 + payloadLen, data.length);
            for (int i = 15; i < limit; i++) {
                buf.add(data[i]);
            }
            nfcIrMcu.setAckSeqNo(seqNo);

            if (endFlag == 0x08) {
                // Last packet – process write
                byte[] payload = new byte[buf.size()];
                for (int i = 0; i < payload.length; i++) {
                    payload[i] = buf.get(i);
                }
                log(TAG, "NFC write: last packet (seqNo=" + seqNo + "), total payload="
                        + payload.length + " bytes, processing");
                processNfcWrite(payload, amiiboConfig);
                nfcIrMcu.setWriteBuffer(new ArrayList<>());
                nfcIrMcu.setLastWritePacketReceived(true);
                nfcIrMcu.setRemoveFramesRemaining(4);
            }
        } else {
            log(TAG, "NFC write: unexpected seqNo " + seqNo
                    + ", expected " + (nfcIrMcu.getAckSeqNo() + 1), true);
        }
        nfcIrMcu.setAction(WRITE_TAG_ACK);
    }

    private void processNfcWrite(byte[] payload, AmiiboConfig amiiboConfig) {
        byte[] amiiboBytes = amiiboConfig.getAmiiboBytes();
        if (amiiboBytes == null) {
            log(TAG, "NFC write: no amiibo loaded", true);
            return;
        }
        if (payload.length < 22) {
            log(TAG, "NFC write: payload too short (" + payload.length + " bytes)", true);
            return;
        }

        // Apply write lock: payload[13:17] → amiiboBytes[16:20]
        System.arraycopy(payload, 13, amiiboBytes, 16, 4);

        // Apply page writes starting at payload[22]
        int i = 22;
        while (i + 1 < payload.length) {
            int pageAddr = payload[i] & 0xFF;
            int len = payload[i + 1] & 0xFF;
            if (pageAddr == 0 || len == 0) {
                break;
            }
            int byteOffset = pageAddr * 4;
            if (byteOffset + len <= amiiboBytes.length && i + 2 + len <= payload.length) {
                System.arraycopy(payload, i + 2, amiiboBytes, byteOffset, len);
            } else {
                log(TAG, "NFC page write: addr=" + pageAddr + " len=" + len
                        + " out of bounds, skipped", true);
            }
            i += 2 + len;
        }

        // Remove write lock: payload[17:21] → amiiboBytes[16:20]
        System.arraycopy(payload, 17, amiiboBytes, 16, 4);

        amiiboConfig.setAmiiboBytes(amiiboBytes);
        amiiboConfig.saveAmiiboFileToDisk(amiiboBytes);
        log(TAG, "NFC write completed and persisted");
    }
}
