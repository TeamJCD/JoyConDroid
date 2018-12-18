package com.rdapps.gamepad.nfc_ir_mcu;

import lombok.Data;

import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.Action.NON;
import static com.rdapps.gamepad.nfc_ir_mcu.NfcIrMcu.MCUState.NOT_INITIALIZED;

@Data
public class NfcIrMcu {
    public enum Action {
        NON(0),
        REQUEST_STATUS(1),
        START_TAG_POLLING(2),
        START_TAG_DISCOVERY(3),
        START_TAG_DISCOVERY_AUTO_MOVE(3),
        READ_TAG(4),
        READ_TAG_2(5),
        READ_TAG_FINISHED(6),
        WRITE_TAG(8);
        private byte value;

        Action(int value) {
            this.value = (byte) value;
        }
    }

    public enum MCUState {
        NOT_INITIALIZED(0),
        IRC(1),
        NFC(2),
        STAND_BY(3),
        BUSY(4);
        private byte value;

        MCUState(int value) {
            this.value = (byte) value;
        }

        public byte getByte() {
            switch (this) {
                case NFC:
                    return 0x04;
                case BUSY:
                    return 0x06;
                case NOT_INITIALIZED:
                case STAND_BY:
                    return 0x01;
                default:
                    return 0x00;
            }
        }
    }

    private byte[] firmwareMajor = new byte[]{0x00, 0x08};
    private byte[] firmwareMinor = new byte[]{0x00, 0x1B};
    private Action action = NON;
    private MCUState mcuState = NOT_INITIALIZED;
}
