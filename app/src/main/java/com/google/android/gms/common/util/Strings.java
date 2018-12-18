package com.google.android.gms.common.util;

import android.text.TextUtils;

import java.util.regex.Pattern;


public class Strings {
    private static final Pattern zzhf = Pattern.compile("\\$\\{(.*?)\\}");

    private Strings() {
    }


    public static String emptyToNull(String str) {
        return TextUtils.isEmpty(str) ? null : str;
    }

    public static boolean isEmptyOrWhitespace(String str) {
        return str == null || str.trim().isEmpty();
    }
}
