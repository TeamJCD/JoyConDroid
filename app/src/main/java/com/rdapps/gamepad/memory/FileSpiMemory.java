package com.rdapps.gamepad.memory;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;

public class FileSpiMemory implements SpiMemory {
    private static final String TAG = FileSpiMemory.class.getName();

    private byte[] bytes;

    public FileSpiMemory(Context context, int rawResource) throws IOException {
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
