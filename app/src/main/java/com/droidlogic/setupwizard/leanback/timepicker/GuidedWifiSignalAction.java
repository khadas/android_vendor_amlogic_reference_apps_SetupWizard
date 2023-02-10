package com.droidlogic.setupwizard.leanback.timepicker;

import android.content.Context;

import androidx.leanback.widget.GuidedAction;

public class GuidedWifiSignalAction extends GuidedAction {

    private int signalLevel;

    public int getSignalLevel() {
        return signalLevel;
    }

    public final static class Builder extends BuilderBase<Builder> {

        int signalLevel;

        public Builder(Context context) {
            super(context);
        }

        public Builder signalLevel(int wifiStatueIcon) {
            this.signalLevel = wifiStatueIcon;
            return this;
        }

        public GuidedWifiSignalAction build() {
            GuidedWifiSignalAction action = new GuidedWifiSignalAction();
            applyValues(action);
            action.signalLevel = signalLevel;
            return action;
        }
    }

}
