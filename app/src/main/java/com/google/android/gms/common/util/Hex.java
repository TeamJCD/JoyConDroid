package com.google.android.gms.common.util;

public class Hex {
    private static final char[] zzgw = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    private static final char[] zzgx = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String bytesToStringUppercase(byte[] bytes) {
        return bytesToStringUppercase(bytes, false);
    }

    public static String bytesToStringUppercase(byte[] bytes, boolean z) {
        int length = bytes.length;
        StringBuilder stringBuilder = new StringBuilder(length << 1);
        int i = 0;
        while (i < length && (!z || i != length - 1 || (bytes[i] & 255) != 0)) {
            stringBuilder.append(zzgw[(bytes[i] & 240) >>> 4]);
            stringBuilder.append(zzgw[bytes[i] & 15]);
            i++;
        }
        return stringBuilder.toString();
    }

    public static byte[] stringToBytes(String str) throws IllegalArgumentException {
        int length = str.length();
        if (length % 2 == 0) {
            byte[] bytes = new byte[(length / 2)];
            int i = 0;
            while (i < length) {
                int i2 = i + 2;
                bytes[i / 2] = (byte) Integer.parseInt(str.substring(i, i2), 16);
                i = i2;
            }
            return bytes;
        }
        throw new IllegalArgumentException("Hex string has odd number of characters");
    }

    public static String zza(byte[] bytes) {
        int i = 0;
        char[] chars = new char[(bytes.length << 1)];
        int i2 = 0;
        while (true) {
            int i3 = i;
            if (i3 >= bytes.length) {
                return new String(chars);
            }
            i = bytes[i3] & 255;
            int i4 = i2 + 1;
            chars[i2] = zzgx[i >>> 4];
            i2 = i4 + 1;
            chars[i4] = zzgx[i & 15];
            i = i3 + 1;
        }
    }
}
