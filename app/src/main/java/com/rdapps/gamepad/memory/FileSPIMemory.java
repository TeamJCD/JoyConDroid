package com.rdapps.gamepad.memory;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FileSPIMemory implements SPIMemory {
    private static final String TAG = FileSPIMemory.class.getName();

    private byte[] bytes;

    public FileSPIMemory(Context context, int rawResource) throws IOException {
        try (InputStream resourceAsStream = context.getResources().openRawResource(rawResource)) {
            bytes = IOUtils.toByteArray(resourceAsStream);
        }
    }

    @Override
    public byte[] read(int location, int length) {
        return Arrays.copyOfRange(bytes, location, location + length);
    }

    @Override
    public void write(int location, byte[] data) {
        System.arraycopy(data, 0, bytes, location, data.length);
    }
}
