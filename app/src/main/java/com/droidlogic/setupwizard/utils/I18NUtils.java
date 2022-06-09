package com.droidlogic.setupwizard.utils;

import android.content.Context;

import java.util.Locale;
import java.util.TimeZone;

public class I18NUtils {

    public static String getTimeZoneOffset() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName(false, TimeZone.SHORT);
    }

    public static String getTimeZoneName() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName(false, TimeZone.LONG);
    }

    public static String getCurrentLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        return language + "_" + country;
    }

}
