package com.rdapps.gamepad.memory;

public interface SPIMemory {
    byte[] read(int location, int length);
    void write(int location, byte[] data);
}
