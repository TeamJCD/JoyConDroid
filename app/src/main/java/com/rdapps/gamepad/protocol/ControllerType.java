package com.rdapps.gamepad.protocol;

import com.rdapps.gamepad.R;
import lombok.Getter;

public enum ControllerType {
    LEFT_JOYCON("Joy-Con (L)", (byte) 0x01, R.raw.left_joycon_eeprom),
    RIGHT_JOYCON("Joy-Con (R)", (byte) 0x02, R.raw.right_joycon_eeprom),
    PRO_CONTROLLER("Pro Controller", (byte) 0x03, R.raw.pro_controller_eeprom);

    private static final byte SUBCLASS = (byte) 0x08;
    private static final String HID_NAME = "Wireless Gamepad";
    private static final String HID_DESCRIPTION = "Gamepad";
    private static final String HID_PROVIDER = "Nintendo";
    private static final String DESCRIPTOR
            = "05010905a1010601ff852109217508953081028530093075089530810285310931750896690181028532"
            + "0932750896690181028533093375089669018102853f0509190129101500250175019510810205010939"
            + "1500250775049501814205097504950181010501093009310933093416000027ffff0000751095048102"
            + "0601ff850109017508953091028510091075089530910285110911750895309102851209127508953091"
            + "02c0";

    private final String btName;
    @Getter
    private final byte typeByte;
    @Getter
    private final int memoryResource;

    ControllerType(String btName, byte typeByte, int memoryResource) {
        this.btName = btName;
        this.typeByte = typeByte;
        this.memoryResource = memoryResource;
    }

    public String getBtName() {
        return btName;
    }

    public byte getSubClass() {
        return SUBCLASS;
    }

    public String getHidName() {
        return HID_NAME;
    }

    public String getHidDescription() {
        return HID_DESCRIPTION;
    }

    public String getHidProvider() {
        return HID_PROVIDER;
    }

    public String getDescriptor() {
        return DESCRIPTOR;
    }
}