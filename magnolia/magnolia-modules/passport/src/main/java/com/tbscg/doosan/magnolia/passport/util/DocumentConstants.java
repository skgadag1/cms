package com.tbscg.doosan.magnolia.passport.util;

import com.tbscg.doosan.magnolia.passport.data.model.Locale;

import java.util.Arrays;
import java.util.List;

public class DocumentConstants {

    public static List<Locale> allowedLocales() {
        return Arrays.asList(
                new Locale(1,"en", "English",true),
                new Locale(2,"de", "German", false),
                new Locale(3,"fr", "French", false),
                new Locale(4,"it", "Italian", false),
                new Locale(5,"nl", "Dutch", false),
                new Locale(6,"pt", "Portuguese", false),
                new Locale(7,"ko", "Korean", false),
                new Locale(8,"es", "Spanish", false)
        );
    }

    public static Locale getDefaultLocale(){
        Locale dLocale = new Locale(1, "en", "English", true);
        return allowedLocales().stream().filter(l -> l.isFallback()).findFirst().orElse(dLocale);
    }

    public static String getSystemOrDefaultLocaleCode() {
        return getDefaultLocale().getCode();
    }

    public enum Subject {
        General,
        IRW,
        Marketing,
        Parts,
        Sales,
        Service,
        Warranty
    }
}
