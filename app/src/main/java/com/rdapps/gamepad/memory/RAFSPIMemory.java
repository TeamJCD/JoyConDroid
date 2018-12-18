package com.rdapps.gamepad.memory;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import static com.rdapps.gamepad.log.JoyConLog.log;
import static com.rdapps.gamepad.util.PreferenceUtils.hasFile;
import static com.rdapps.gamepad.util.PreferenceUtils.setFile;

public class RAFSPIMemory implements SPIMemory {
    private static final String TAG = RAFSPIMemory.class.getName();

    private RandomAccessFile randomAccessFile;

    public RAFSPIMemory(Context context, String name, int rawResource) throws IOException {
        createFile(context, name, rawResource);
    }

    @Override
    public byte[] read(int location, int length) {
        byte[] result = new byte[length];
        try {
            randomAccessFile.seek(location);
            randomAccessFile.read(result);
        } catch (IOException e) {
            log(TAG, "Read Failed.", e);
        }
        return result;
    }

    @Override
    public void write(int location, byte[] data) {
        try {
            randomAccessFile.seek(location);
            randomAccessFile.write(data);
        } catch (IOException e) {
            log(TAG, "Write Failed.", e);
        }
    }

    private void createFile(Context context, String name, int rawResource) throws IOException {
        File file = new File(context.getFilesDir(), name);
        if (!hasFile(context, name) || !file.exists()) {
            if (!file.exists()) {
                file.createNewFile();
            }
            try (InputStream resourceAsStream = context.getResources().openRawResource(rawResource)) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(resourceAsStream, fileOutputStream);
                setFile(context, name, true);
            }
        }
        randomAccessFile = new RandomAccessFile(file, "rw");
    }
}
