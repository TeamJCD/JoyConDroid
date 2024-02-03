package com.rdapps.gamepad.memory;

public interface SpiMemory {
    byte[] read(int location, int length);

    void write(int location, byte[] data);
}
