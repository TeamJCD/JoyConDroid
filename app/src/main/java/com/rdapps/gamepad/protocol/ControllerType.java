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
    private static final String DESCRIPTOR = "05010905a1010601ff8521092175089530810285300930750895308102853109317508966901810285320932750896690181028533093375089669018102853f05091901291015002501750195108102050109391500250775049501814205097504950181010501093009310933093416000027ffff00007510950481020601ff85010901750895309102851009107508953091028511091175089530910285120912750895309102c0";

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

    public String getBTName() {
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