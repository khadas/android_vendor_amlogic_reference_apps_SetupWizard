package com.droidlogic.setupwizard.leanback.timepicker;

import androidx.annotation.NonNull;
import androidx.leanback.widget.GuidedAction;
import androidx.leanback.widget.GuidedActionsStylist;
import androidx.leanback.widget.picker.TimePicker;

import com.droidlogic.setupwizard.R;

import java.util.Calendar;

public class GuidedActionsStylistExtended extends GuidedActionsStylist {

    public interface EditingModeChangeListener {
        void onEditingModeChange(ViewHolder vh, boolean editing, boolean withTransition);
    }

    private static final int VIEW_TYPE_TIME_PICKER = 2;

    private EditingModeChangeListener editingModeChangeListener;

    public void setEditingModeChangeListener(EditingModeChangeListener editingModeChangeListener) {
        this.editingModeChangeListener = editingModeChangeListener;
    }

    @Override
    protected void onEditingModeChange(ViewHolder vh, boolean editing, boolean withTransition) {
        super.onEditingModeChange(vh, editing, withTransition);
        if (editingModeChangeListener != null) {
            editingModeChangeListener.onEditingModeChange(vh, editing, withTransition);
        }
    }

    @Override
    public int getItemViewType(GuidedAction action) {
        if (action instanceof GuidedTimePickerAction) {
            return VIEW_TYPE_TIME_PICKER;
        } else {
            return super.getItemViewType(action);
        }
    }

    @Override
    public int onProvideItemLayoutId(int viewType) {
        if (viewType == VIEW_TYPE_TIME_PICKER) {
            return R.layout.guided_action_time_picker;
        } else {
            return super.onProvideItemLayoutId(viewType);
        }
    }

    public void onBindActivatorView(@NonNull GuidedActionsStylist.ViewHolder vh, GuidedAction action) {
        if (action instanceof GuidedTimePickerAction) {
            GuidedTimePickerAction timeAction = (GuidedTimePickerAction) action;
            TimePicker timeView = vh.itemView.findViewById(R.id.guidedactions_activator_item);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeAction.getTime());
            timeView.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            timeView.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            super.onBindActivatorView(vh, action);
        }
    }

    public boolean onUpdateActivatorView(@NonNull GuidedActionsStylist.ViewHolder vh, GuidedAction action) {
        if (action instanceof GuidedTimePickerAction) {
            GuidedTimePickerAction timeAction = (GuidedTimePickerAction) action;
            TimePicker timeView = vh.itemView.findViewById(R.id.guidedactions_activator_item);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeAction.getTime());
            calendar.set(Calendar.HOUR_OF_DAY, timeView.getHour());
            calendar.set(Calendar.MINUTE, timeView.getMinute());
            if (timeAction.getTime() != calendar.getTimeInMillis()) {
                timeAction.setTime(calendar.getTimeInMillis());
                return true;
            } else {
                return false;
            }
        } else {
            return super.onUpdateActivatorView(vh, action);
        }
    }
}