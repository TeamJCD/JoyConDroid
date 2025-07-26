package com.rdapps.gamepad.util;

import android.content.res.AssetFileDescriptor;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipUtil {
    private final AssetFileDescriptor zipFile;
    private final String location;

    public UnzipUtil(AssetFileDescriptor zipFile, String location) {
        this.zipFile = zipFile;
        this.location = location;

        dirChecker("");
    }

    public void unzip() {
        try (FileInputStream fin = zipFile.createInputStream();
                ZipInputStream zin = new ZipInputStream(fin)) {
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else if (ze.getName() != null && !ze.getName().trim().isEmpty()) {
                    File newFile = new File(location, ze.getName());
                    // Validate that the file path is within the intended extraction directory
                    if (!newFile.toPath().normalize()
                            .startsWith(new File(location).toPath().normalize())) {
                        throw new RuntimeException("Bad zip entry: " + ze.getName());
                    }

                    // Ensure parent directory exists
                    File parent = new File(newFile.getParent());
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream fout = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, len);
                        }
                    }
                }
                zin.closeEntry();
            }
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        }
    }

    private void dirChecker(String dir) {
        File f = new File(location + File.separator + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
