package com.rdapps.gamepad.memory;

import com.rdapps.gamepad.util.ByteUtils;

public class ControllerMemory implements SPIMemory {
    private SPIMemory delegate;

    private static int BODY_LOCATION = 0x6050;
    private static int BUTTON_LOCATION = 0x6053;

    public ControllerMemory(SPIMemory delegate) {
        this.delegate = delegate;
    }

    public int getBodyColor() {
        byte[] read = read(BODY_LOCATION, 3);
        return ByteUtils.byteArrayToColor(read);
    }

    public int getButtonColor() {
        byte[] read = read(BUTTON_LOCATION, 3);
        return ByteUtils.byteArrayToColor(read);
    }

    public void setBodyColor(int color) {
        byte[] bytes = ByteUtils.colorToByteArray(color);
        write(BODY_LOCATION, bytes);
    }

    public void setButtonColor(int color) {
        byte[] bytes = ByteUtils.colorToByteArray(color);
        write(BUTTON_LOCATION, bytes);
    }

    @Override
    public byte[] read(int location, int length) {
        return delegate.read(location, length);
    }

    @Override
    public void write(int location, byte[] data) {
        delegate.write(location, data);
    }
}
