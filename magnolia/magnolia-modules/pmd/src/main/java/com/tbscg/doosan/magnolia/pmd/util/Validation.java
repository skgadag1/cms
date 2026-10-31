package com.tbscg.doosan.magnolia.pmd.util;

import java.util.regex.Pattern;

public class Validation {

    private static final Pattern numberPattern = Pattern.compile("\\d+(\\.\\d+)?");

    public static boolean isNumberFormat(String s) {
        return numberPattern.matcher(s).matches();
    }
}
