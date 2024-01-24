package com.rdapps.gamepad.command.handler.subcommand;

import java.util.Arrays;

import lombok.Getter;

public enum SubCommand {
    /**
     * https://github.com/dekuNukem/Nintendo_Switch_Reverse_Engineering/blob/master/bluetooth_hid_subcommands_notes.md
     */
    UNKNOWN(0xFF, new UnknownSubCommandHandler()),
    GET_ONLY_CONTROLLER_STATE(0x00, new GetOnlyControllerStateHandler()),
    BLUETOOTH_MANUAL_PAIRING(0x01, new BluetoothManualPairingHandler()),
    REQUEST_DEVICE_INFO(0x02, new RequestDeviceInfoHandler()),
    REQUEST_INPUT_REPORT_MODE(0x03, new RequestInputReportModeHandler()),
    TRIGGER_BUTTONS_ELAPSED_TIME(0x04, new TriggerButtonsElapsedTimeHandler()),
    //GET_PAGE_LIST_STATE(0x05),
    //SET_HCI_STATE(0x06),
    //RESET_PAIRING_INFO(0x07),
    SET_SHIPMENT_LOW_POWER_STATE(0x08, new SetShipmentLowPowerStateHandler()),
    SPI_FLASH_READ(0x10, new SPIFlashReadHandler()),
    SPI_FLASH_WRITE(0x11, new SPIFLashWriteHandler()),
    //SPI_SECTOR_ERASE(0x12),
    //RESET_NFC_IR_MCU(0x20),
    SET_NFC_IR_MCU_CONFIG(0x21, new SetNFCIRMCUConfigHandler()),
    SET_NFC_IR_STATE(0x22, new SetNFCIRMCUStateHandler()),
    //SET_UNKNOWN(0x24),
    //RESET_UNKNOWN(0x25),
    //TODO .... BUNCH OF THINGS
    SET_PLAYER_LIGHTS(0x30, new SetPlayerLightsHandler()),
    GET_PLAYER_LIGHTS(0x31, new GetPlayerLightsHandler()),
    //SET_HOME_LIGHT(0x38),
    ENABLE_IMU_6_AXIS_SENSOR(0x40, new EnableIMU6AxisSensorHandler()),
    SET_IMU_6_AXIS_SENSITIVITY(0x41, new SetIMU6AxisSensitivityHandler()),
    //WRITE_TO_IMU_REGISTERS(0x42),
    //READ_FROM_IMU_REGISTERS(0x43),
    ENABLE_VIBRATION(0x48, new EnableVibrationHandler()),
    //TODO .... BUNCH OF THINGS
    ;

    @Getter
    private final byte subCommandId;
    @Getter
    private final SubCommandHandler handler;

    SubCommand(int subCommandId, SubCommandHandler handler) {
        this.subCommandId = (byte) subCommandId;
        this.handler = handler;
    }

    public static SubCommand getSubCommand(byte subCommandId) {
        return Arrays.stream(values())
                .filter(subCommand -> subCommand.subCommandId == subCommandId)
                .findAny()
                .orElse(UNKNOWN);
    }
}
