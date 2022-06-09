package com.android.settingslib.wifi;

import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;

public class AccessPoint {

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;
    public static final int SECURITY_OWE = 4;
    public static final int SECURITY_SAE = 5;
    public static final int SECURITY_EAP_SUITE_B = 6;
    public static final int SECURITY_MAX_VAL = 7; // Has to be the last

    public int getSecurity() {
        return 0;
    }

    public String getSsidStr() {
        return "";
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    public WifiConfiguration getConfig() {
        return null;
    }

    public DetailedState getDetailedState() {
        return null;
    }

    public boolean isActive() {
        return false;
    }

    public String getTitle() {
        return null;
    }

    public String getSummary() {
        return null;
    }

}
