package com.rdapps.gamepad.memory;

public class DummySPIMemory implements SPIMemory {
    @Override
    public byte[] read(int location, int length) {
        return new byte[length];
    }

    @Override
    public void write(int location, byte[] data) {
    }
}
