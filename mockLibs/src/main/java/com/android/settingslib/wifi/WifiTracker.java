package com.android.settingslib.wifi;

import android.content.Context;

import androidx.lifecycle.Lifecycle;

import java.util.List;

public class WifiTracker {
    public interface WifiListener {
        void onWifiStateChanged(int state);

        void onConnectedChanged();

        void onAccessPointsChanged();
    }

    public WifiTracker(Context context, WifiListener wifiListener,
                       boolean includeSaved, boolean includeScans) {
    }

    public WifiTracker(Context context, WifiListener wifiListener,
                       Lifecycle lifecycle, boolean includeSaved, boolean includeScans) {
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public List<AccessPoint> getAccessPoints() {
        return null;
    }
}
