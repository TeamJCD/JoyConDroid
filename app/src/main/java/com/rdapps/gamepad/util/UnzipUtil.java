package com.rdapps.gamepad.util;

import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.google.android.gms.common.util.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipUtil {
    private AssetFileDescriptor zipFile;
    private String location;

    public UnzipUtil(AssetFileDescriptor zipFile, String location) {
        this.zipFile = zipFile;
        this.location = location;

        dirChecker("");
    }

    public void unzip() {
        try {
            FileInputStream fin = zipFile.createInputStream();
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {
                    if (!Strings.isEmptyOrWhitespace(ze.getName())) {
                        FileOutputStream fout = new FileOutputStream(
                                location + File.separator + ze.getName());

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, len);
                        }
                        fout.close();
                    }
                    zin.closeEntry();
                }

            }
            zin.close();
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