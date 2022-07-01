package com.droidlogic.setupwizard;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.droidlogic.setupwizard.fragment.BaseGuideStepFragment;
import com.droidlogic.setupwizard.fragment.LocalFragment;
import com.droidlogic.setupwizard.utils.Backdoor;

import java.util.List;


public class MainActivity extends FragmentActivity {

    private View viNextAction;
    private View viWifiFloat;
    private TextView tvWifiName;
    private final int[] forbiddenKey = new int[]{206, 243, 244, 245, 165, 246, 247, 248, 168, 85, 86, 130, 169, 88, 87, 89, 90, 183, 184, 185, 186};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0) == 1) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finishSetup();
            return;
        }
        if (null == savedInstanceState) {
            GuidedStepSupportFragment.addAsRoot(this, new LocalFragment(), android.R.id.content);
        }
        setHdmiCecComponentEnabled(PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        final FrameLayout contentGroup = findViewById(android.R.id.content);
        viWifiFloat = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_wifi_float, contentGroup, false);
        tvWifiName = viWifiFloat.findViewById(R.id.tv_wifi_name);
        viNextAction = LayoutInflater.from(MainActivity.this).inflate(R.layout.view_next_action, contentGroup, false);
        viNextAction.setOnClickListener(view -> {
            BaseGuideStepFragment topFragment = getTopBaseGuideStepFragment();
            if (topFragment != null) {
                topFragment.onNextAction();
            }
        });
        contentGroup.post(() -> {
            contentGroup.addView(viNextAction);
            contentGroup.addView(viWifiFloat);
        });
    }

    private BaseGuideStepFragment getTopBaseGuideStepFragment() {
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList.size() > 0) {
            Fragment fragment = fragmentList.get(fragmentList.size() - 1);
            if (fragment instanceof BaseGuideStepFragment) {
                return (BaseGuideStepFragment) fragment;
            }
        }
        return null;
    }

    private final ViewTreeObserver.OnGlobalFocusChangeListener globalFocusChangeListener = (oldFocus, newFocus) -> {
        BaseGuideStepFragment topFragment = getTopBaseGuideStepFragment();
        if (topFragment != null) {
            topFragment.onFocusChange(oldFocus, newFocus);
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(globalFocusChangeListener);
        } else {
            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalFocusChangeListener(globalFocusChangeListener);
        }
    }

    public void setWifiName(String wifiName) {
        tvWifiName.setText(wifiName);
    }

    public void showWifiView(View anchorView) {
        if (anchorView == null) return;
        viWifiFloat.postDelayed(() -> {
            int[] location = new int[2];
            anchorView.getLocationInWindow(location);
            viWifiFloat.setVisibility(View.VISIBLE);
            viWifiFloat.bringToFront();
            FrameLayout.LayoutParams pms = (FrameLayout.LayoutParams) viWifiFloat.getLayoutParams();
            pms.leftMargin = location[0];
            pms.topMargin = (int) (location[1] - viWifiFloat.getHeight() * 0.5);
            viWifiFloat.setLayoutParams(pms);
        }, 60);
    }

    public void hideWifiView() {
        if (viWifiFloat.getVisibility() == View.VISIBLE) {
            viWifiFloat.setVisibility(View.INVISIBLE);
        }
    }

    public void actionNextVisible() {
        viNextAction.setVisibility(View.VISIBLE);
    }

    public void actionNextInvisible() {
        viNextAction.setVisibility(View.INVISIBLE);
    }

    public void nextActionBringToFront() {
        viNextAction.bringToFront();
    }

    public void setNextActionText(String text) {
        TextView tvNext = viNextAction.findViewById(R.id.tv_next_action);
        tvNext.setText(text);
    }

    private final Backdoor backdoor = new Backdoor();

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (backdoor.input(event)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_skip_title)
                    .setMessage(R.string.dialog_skip_notice)
                    .setPositiveButton(R.string.dialog_btn_confirm, (dialog, which) -> {
                        dialog.dismiss();
                        setHdmiCecComponentEnabled(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
                        finishSetup();
                    })
                    .setNegativeButton(R.string.dialog_btn_cancel, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
            return true;
        }
        for (int keyCode : forbiddenKey) {
            if (keyCode == event.getKeyCode()) {
                return true;
            }
        }
        if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            viNextAction.postDelayed(this::actionNextVisible, 50);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void finish() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            setHdmiCecComponentEnabled(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
            super.finish();
        }
    }

    private void setHdmiCecComponentEnabled(int state) {
        try {
            PackageManager pm = getPackageManager();
            ComponentName name = new ComponentName("com.android.tv.settings", "com.android.tv.settings.tvoption.HdmiCecActivity");
            pm.setComponentEnabledSetting(name, state, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finishSetup() {
        try {
            ContentResolver contentResolver = getContentResolver();
            Settings.Global.putInt(contentResolver, Settings.Global.DEVICE_PROVISIONED, 1);
            Settings.Secure.putInt(contentResolver, Settings.Secure.USER_SETUP_COMPLETE, 1);
            PackageManager pm = getPackageManager();
            ComponentName name = new ComponentName(MainActivity.this, MainActivity.class);
            pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.finish();
    }

}