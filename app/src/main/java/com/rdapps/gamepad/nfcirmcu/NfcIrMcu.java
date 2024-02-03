package com.rdapps.gamepad.nfcirmcu;

import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.Action.NON;
import static com.rdapps.gamepad.nfcirmcu.NfcIrMcu.McuState.NOT_INITIALIZED;

import lombok.Data;

@Data
public class NfcIrMcu {
    public enum Action {
        NON,
        REQUEST_STATUS,
        START_TAG_POLLING,
        START_TAG_DISCOVERY,
        START_TAG_DISCOVERY_AUTO_MOVE,
        READ_TAG,
        READ_TAG_2,
        READ_TAG_FINISHED,
        WRITE_TAG;
    }

    public enum McuState {
        NOT_INITIALIZED,
        IRC,
        NFC,
        STAND_BY,
        BUSY;

        public byte getByte() {
            return switch (this) {
                case NFC -> 0x04;
                case BUSY -> 0x06;
                case NOT_INITIALIZED, STAND_BY -> 0x01;
                default -> 0x00;
            };
        }
    }

    private byte[] firmwareMajor = new byte[]{0x00, 0x08};
    private byte[] firmwareMinor = new byte[]{0x00, 0x1B};
    private Action action = NON;
    private McuState mcuState = NOT_INITIALIZED;
}
