package com.rdapps.gamepad.command.handler;

import static com.rdapps.gamepad.report.InputReport.Type.NFC_IR_REPORT;
import static com.rdapps.gamepad.report.InputReport.Type.SIMPLE_HID_REPORT;
import static com.rdapps.gamepad.report.InputReport.Type.STANDARD_FULL_REPORT;
import static com.rdapps.gamepad.report.InputReportMode.NFC_IR_MODE;
import static com.rdapps.gamepad.report.InputReportMode.SIMPLE_HID;
import static com.rdapps.gamepad.report.InputReportMode.STANDARD_FULL_MODE;
import static com.rdapps.gamepad.util.ThreadUtil.safeSleep;

import com.rdapps.gamepad.button.ButtonState;
import com.rdapps.gamepad.protocol.Callback;
import com.rdapps.gamepad.protocol.ControllerType;
import com.rdapps.gamepad.protocol.JoyController;
import com.rdapps.gamepad.protocol.JoyControllerState;
import com.rdapps.gamepad.report.InputReport;
import com.rdapps.gamepad.report.InputReportMode;
import java.util.Objects;

public class InputHandler {
    private static final String TAG = InputHandler.class.getName();

    private static final int HANDSHAKE_COUNT = 3;

    private final JoyController joyController;

    public InputHandler(JoyController joyController) {
        this.joyController = joyController;
    }

    public void sendHandShake() {
        boolean connected = joyController.isConnected();
        InputReportMode inputReportMode = joyController.getState().getInputReportMode();
        int tryCount = 0;
        while (inputReportMode == SIMPLE_HID && connected && tryCount < HANDSHAKE_COUNT) {
            sendSimpleHidReport();
            safeSleep(100);
            tryCount++;
        }
    }

    public void sendSimpleHidReport() {
        InputReport inputReport = new InputReport(SIMPLE_HID_REPORT);
        ControllerType controllerType = joyController.getControllerType();
        inputReport.fillShortButtonReport(
                controllerType,
                joyController.getButtonState());
        joyController.sendReport(inputReport);
    }

    public boolean sendStandardFullReport() {
        JoyControllerState state = joyController.getState();
        ButtonState buttonState = joyController.getButtonState();
        ControllerType controllerType = joyController.getControllerType();
        InputReportMode inputReportMode = state.getInputReportMode();
        InputReport inputReport = new InputReport(
                inputReportMode == NFC_IR_MODE ? NFC_IR_REPORT : STANDARD_FULL_REPORT);
        inputReport.fillTime(state);
        inputReport.fillBattery(state);
        inputReport.fillConnectionInfo(state);
        inputReport.fillFullButtonReport(controllerType, buttonState);
        inputReport.fillVibratorData(state);
        inputReport.fillSensorData(joyController);

        if (inputReportMode == NFC_IR_MODE) {
            inputReport.fillNfcIrData(joyController);
        }
        return joyController.sendReport(inputReport);
    }

    public boolean sendFullReport() {
        Callback callbackFunction = joyController.getCallbackFunction();
        JoyControllerState state = joyController.getState();
        InputReportMode inputReportMode = state.getInputReportMode();
        if (Objects.nonNull(callbackFunction)) {
            callbackFunction.notifyBeforePackage();
        }
        if (inputReportMode == STANDARD_FULL_MODE || inputReportMode == NFC_IR_MODE) {
            return sendStandardFullReport();
        }
        return false;
    }
}
