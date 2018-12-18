package com.rdapps.gamepad.nintendo_switch;

import lombok.Data;

@Data
public class MCUMode {
    public enum State {
        NOT_INITIALIZED,
        IRC,
        NFC,
        STAND_BY,
        BUSY
    }

    public enum Action {
        NON,
        REQUEST_STATUS,
        START_TAG_POLLING,
        START_TAG_DISCOVERY,
        READ_TAG_2, READ_FINISHED, READ_TAG
    }

    private State state = State.NOT_INITIALIZED;
    private Action action = Action.NON;
    private int sentCount = 0;
    private byte[] fwMajor = new byte[]{0x00, 0x03};
    private byte[] fwMinor = new byte[]{0x00, 0x05};
}
