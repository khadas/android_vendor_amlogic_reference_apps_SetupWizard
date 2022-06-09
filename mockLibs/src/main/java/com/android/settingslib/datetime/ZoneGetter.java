package com.android.settingslib.datetime;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ZoneGetter {

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_GMT = "gmt";

    public static List<Map<String, Object>> getZonesList(Context context) {
        List<Map<String, Object>> zonesList = new ArrayList<>();
        Map<String, Object> zone1 = new HashMap<>();

        zone1.put(KEY_ID, "Asia/Shanghai");
        zone1.put(KEY_NAME, "Shanghai");
        zone1.put(KEY_GMT, "GMT+08:00");

        Map<String, Object> zone2 = new HashMap<>();
        zone2.put(KEY_ID, "Atlantic/Azores");
        zone2.put(KEY_NAME, "Azores");
        zone2.put(KEY_GMT, "GMT+00:00");

        Map<String, Object> zone3 = new HashMap<>();
        zone3.put(KEY_ID, "America/Manaus");
        zone3.put(KEY_NAME, "Manaus");
        zone3.put(KEY_GMT, "GMT-04:00");

        zonesList.add(zone1);
        zonesList.add(zone2);
        zonesList.add(zone3);

        return zonesList;
    }

    public static CharSequence getTimeZoneOffsetAndName(Context context, TimeZone tz, Date now) {
        return "ddd ddd";
    }

}
