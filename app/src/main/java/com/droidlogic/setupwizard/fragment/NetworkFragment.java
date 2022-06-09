package com.droidlogic.setupwizard.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionAdapter;
import androidx.leanback.widget.GuidedActionEditText;
import androidx.leanback.widget.GuidedActionsStylist;
import androidx.leanback.widget.VerticalGridView;
import androidx.lifecycle.Lifecycle;

import com.android.settingslib.wifi.AccessPoint;
import com.droidlogic.setupwizard.ConnectivityListener;
import com.droidlogic.setupwizard.MainActivity;
import com.droidlogic.setupwizard.R;
import com.droidlogic.setupwizard.leanback.timepicker.GuidedActionsStylistExtended;
import com.droidlogic.setupwizard.utils.WifiConfigHelper;

import java.util.ArrayList;
import java.util.List;

import static android.net.NetworkInfo.DetailedState.CONNECTING;
import static android.net.wifi.WifiConfiguration.NetworkSelectionStatus.DISABLED_AUTHENTICATION_FAILURE;
import static android.net.wifi.WifiConfiguration.NetworkSelectionStatus.DISABLED_BY_WRONG_PASSWORD;
import static android.net.wifi.WifiConfiguration.NetworkSelectionStatus.NETWORK_SELECTION_ENABLED;

public class NetworkFragment extends BaseGuideStepFragment {

    private final int ID_WIFI = 20;
    private final int ID_ETHERNET = 21;
    private final int EDITABLE_LABEL = 2000;
    private WifiManager wifiManager;
    private ConnectivityListener connectivityListener;
    private final List<GuidedAction> wifiGuideActionList = new ArrayList<>();
    private final ArrayList<AccessPoint> wifiList = new ArrayList<>();
    private WifiConfiguration currentConfiguration;

    private GuidedAction wifiGuidedAction;
    private GuidedAction ethernetGuidedAction;

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    updateConnectState(true);
                } else {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == CONNECTING ||
                            state == NetworkInfo.DetailedState.AUTHENTICATING ||
                            state == NetworkInfo.DetailedState.OBTAINING_IPADDR ||
                            state == NetworkInfo.DetailedState.DISCONNECTED ||
                            state == NetworkInfo.DetailedState.FAILED) {
                        updateWifiState(state);
                    }
                }
            }
        }
    };

    private NetworkInfo getNetworkInfo(int networkType) {
        if (getContext() == null) return null;
        ConnectivityManager connManager = (ConnectivityManager) getContext().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getNetworkInfo(networkType);
    }

    private void updateWifiList() {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            final List<AccessPoint> accessPoints = connectivityListener.getAvailableNetworks();
            updateWifiList(accessPoints);
            updateConnectState(false);
        }
    }

    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.network_setup);
        String breadcrumb = "";
        String description = getString(R.string.network_description);
        Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.network);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }

    @Override
    public void onFocusChange(View oldFocus, View newFocus) {
        super.onFocusChange(oldFocus, newFocus);
        MainActivity mainActivity = getMainActivity();
        if (mainActivity == null) return;
        if (newFocus instanceof GuidedActionEditText) {
            mainActivity.showWifiView(newFocus);
        } else {
            mainActivity.hideWifiView();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getContext() == null) return;
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getContext().registerReceiver(wifiScanReceiver, intentFilter);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        connectivityListener.setWifiListener(() -> {
            List<AccessPoint> accessPointList = connectivityListener.getAvailableNetworks();
            if (accessPointList != null) {
                for (AccessPoint accessPoint : accessPointList) {
                    try {
                        WifiConfiguration configuration = accessPoint.getConfig();
                        if (currentConfiguration != null && configuration != null && TextUtils.equals(configuration.SSID, currentConfiguration.SSID)) {
                            int state = (configuration.getNetworkSelectionStatus().getNetworkSelectionStatus());
                            if (state == DISABLED_AUTHENTICATION_FAILURE || state == DISABLED_BY_WRONG_PASSWORD) {
                                toast(currentConfiguration.SSID + getString(R.string.password_error));
                                currentConfiguration = null;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            updateWifiList();
        });
        updateWifiList();
        connectivityListener.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectivityListener.stop();
        connectivityListener.destroy();
        if (getContext() != null) {
            getContext().unregisterReceiver(wifiScanReceiver);
        }
    }

    @Override
    String getNextActionLabel() {
        return getString(R.string.action_next);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectivityListener = new ConnectivityListener(getContext(), this::updateWifiList);
    }

    @SuppressLint("RestrictedApi")
    private void updateWifiList(List<AccessPoint> accessPoints) {
        if (accessPoints == null || wifiManager == null) return;
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED) && isHideShowSoftKeyboard()) {
            wifiGuideActionList.clear();
            wifiList.clear();
            wifiList.addAll(accessPoints);
            int index = 0;
            for (final AccessPoint accessPoint : accessPoints) {
                WifiConfiguration config = accessPoint.getConfig();
                if (config != null) {
                    if (config.getNetworkSelectionStatus().getNetworkSelectionStatus() != NETWORK_SELECTION_ENABLED) {
                        addEditablePasswordAction(getActivity(), wifiGuideActionList, EDITABLE_LABEL + index, accessPoint.getTitle(), accessPoint.getSummary());
                    } else {
                        addAction(getActivity(), wifiGuideActionList, index, accessPoint.getTitle(), accessPoint.getSummary());
                    }
                } else {
                    addEditablePasswordAction(getActivity(), wifiGuideActionList, EDITABLE_LABEL + index, accessPoint.getTitle(), accessPoint.getSummary());
                }
                index++;
            }
            VerticalGridView verticalGridView = getGuidedActionsStylist().getSubActionsGridView();
            if (verticalGridView != null) {
                GuidedActionAdapter guidedActionAdapter = (GuidedActionAdapter) verticalGridView.getAdapter();
                if (guidedActionAdapter != null) {
                    guidedActionAdapter.setActions(wifiGuideActionList);
                }
            }
        }
    }

    private void updateConnectState(boolean toastMsg) {
        if (wifiGuidedAction != null && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
            //wifi state
            if (wifiManager != null) {
                NetworkInfo networkInfo = getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo != null && networkInfo.isConnected()) {
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo == null) {
                        wifiGuidedAction.setDescription(getString(R.string.not_connected));
                    } else {
                        String ssid = wifiInfo.getSSID();
                        if (!TextUtils.isEmpty(ssid) && ssid.length() > 2) {
                            if (ssid.startsWith("\"")) {
                                ssid = ssid.substring(1);
                            }
                            if (ssid.endsWith("\"")) {
                                ssid = ssid.substring(0, ssid.length() - 1);
                            }
                        }
                        wifiGuidedAction.setDescription(ssid);
                        notifyActionChanged(0);
                        if (toastMsg) {
                            toast(ssid + getString(R.string.connected));
                        }
                    }
                }
            }
            //ethernet
            if (connectivityListener.isEthernetConnected()) {
                ethernetGuidedAction.setDescription(getString(R.string.connected));
            } else {
                ethernetGuidedAction.setDescription(getString(R.string.not_connected));
            }
            notifyActionChanged(1);
        }
    }

    private void updateWifiState(NetworkInfo.DetailedState state) {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED) && state != null) {
            int strId = -1;
            switch (state) {
                case CONNECTING:
                    strId = R.string.wifi_state_connecting;
                    break;
                case AUTHENTICATING:
                    strId = R.string.wifi_state_authenticating;
                    break;
                case OBTAINING_IPADDR:
                    strId = R.string.wifi_state_obtaining_ipaddr;
                    break;
                case DISCONNECTED:
                    strId = R.string.not_connected;
                    break;
                case FAILED:
                    strId = R.string.wifi_state_failed;
                    break;
            }
            if (strId != -1) {
                wifiGuidedAction.setDescription(getString(strId));
                notifyActionChanged(0);
            }
        }
    }

    @Override
    public GuidedActionsStylist onCreateActionsStylist() {
        GuidedActionsStylistExtended guidedActionsStylistExtended = new GuidedActionsStylistExtended();
        guidedActionsStylistExtended.setEditingModeChangeListener(new GuidedActionsStylistExtended.EditingModeChangeListener() {
            @Override
            public void onEditingModeChange(GuidedActionsStylist.ViewHolder vh, boolean editing, boolean withTransition) {
                if (editing) {
                    GuidedAction action = vh.getAction();
                    CharSequence editTitle = action.getTitle();
                    MainActivity mainActivity = getMainActivity();
                    if (mainActivity == null) return;
                    mainActivity.setWifiName(String.valueOf(editTitle));
                }
            }
        });
        return guidedActionsStylistExtended;
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        wifiGuidedAction = new GuidedAction.Builder(getActivity())
                .title(getString(R.string.network_type_wifi))
                .subActions(wifiGuideActionList)
                .id(ID_WIFI)
                .description(getString(R.string.not_connected))
                .build();

        ethernetGuidedAction = new GuidedAction.Builder(getActivity())
                .title(getString(R.string.network_type_ethernet))
                .id(ID_ETHERNET)
                .description(getString(R.string.not_connected))
                .build();

        actions.add(wifiGuidedAction);
        actions.add(ethernetGuidedAction);

        //addAction(getContext(), actions, CONTINUE, getString(R.string.action_next), null);
    }

    @Override
    public void onNextAction() {
        FragmentManager fm = getParentFragmentManager();
        DateTimeFragment next = DateTimeFragment.newInstance(getSelectedActionPosition() - 1);
        GuidedStepSupportFragment.add(fm, next);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == CONTINUE) {
            onNextAction();
        } else if (action.getId() == ID_WIFI) {
            actionNextInvisible();
        } else if (action.getId() == ID_ETHERNET) {
            if (ethernetGuidedAction != null) {
                toast(getString(R.string.network_type_ethernet) + ":" + ethernetGuidedAction.getDescription());
            }
        }
    }

    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {
        if (action.getId() >= EDITABLE_LABEL) return super.onSubGuidedActionClicked(action);
        AccessPoint accessPoint = wifiList.get((int) action.getId());
        WifiConfiguration config = accessPoint.getConfig();
        if (config != null) {
            if (!accessPoint.isActive()) {
                connect(config);
            }
        }
        return super.onSubGuidedActionClicked(action);
    }

    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction action) {
        int position = (int) (action.getId() - EDITABLE_LABEL);
        CharSequence wifiPassword = action.getEditTitle();
        AccessPoint accessPoint = wifiList.get(position);
        WifiConfiguration wifiConfiguration = WifiConfigHelper.getConfiguration(getContext(), accessPoint.getSsidStr(), accessPoint.getSecurity(), String.valueOf(wifiPassword));
        connect(wifiConfiguration);
        return super.onGuidedActionEditedAndProceed(action);
    }

    private void connect(WifiConfiguration configuration) {
        if (wifiManager == null || configuration == null) return;
        currentConfiguration = configuration;
        wifiManager.disconnect();
        wifiManager.connect(configuration, null);
    }

    private boolean isHideShowSoftKeyboard() {
        if (getContext() == null) return true;
        InputMethodManager im = getContext().getSystemService(InputMethodManager.class);
        return im.getInputMethodWindowVisibleHeight() == 0;
    }

}