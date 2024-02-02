package com.rdapps.gamepad.util;


import android.graphics.Color;


import com.google.android.gms.common.util.Hex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ByteUtils {

    public static String encodeHexString(byte b) {
        return Hex.bytesToStringUppercase(new byte[]{b});
    }

    public static short toShort(byte[] bytes, int index) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(Arrays.copyOfRange(bytes, index, index + 2));
        return bb.getShort(0);
    }

    public static int byteArrayToColor(byte[] bytes) {
        return Color.rgb(
                (bytes[0] & 0xFF),
                (bytes[1] & 0xFF),
                (bytes[2] & 0xFF));
    }

    public static byte crc8(byte[] bytes) {
        byte polynomial = 0x07;
        byte accumulator = 0;

        for (int j = 0; j < bytes.length - 1; j++) {
            byte b = bytes[j];
            accumulator = (byte) (accumulator ^ b);
            for (int i = 0; i < 8; i++) {
                if ((accumulator & 0x80) != 0x00) {
                    accumulator = (byte) ((accumulator << 1) ^ polynomial);
                } else {
                    accumulator = (byte) (accumulator << 1);
                }
            }
        }
        return accumulator;
    }

    public static byte[] colorToByteArray(int color) {
        return new byte[]{
                (byte) Color.red(color),
                (byte) Color.green(color),
                (byte) Color.blue(color)};
    }

    public static String bytesToStringUppercase(byte[] data) {
        if (Objects.isNull(data)) {
            return "null";
        } else {
            return Hex.bytesToStringUppercase(data);
        }
    }

    public static byte[] hexStringToByteArray(CharSequence s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static List<Byte> asList(byte[] arr) {
        if (Objects.isNull(arr)) {
            return null;
        }
        List<Byte> arrayList = new ArrayList<>();
        for (byte b : arr) {
            arrayList.add(b);
        }
        return arrayList;
    }
}
