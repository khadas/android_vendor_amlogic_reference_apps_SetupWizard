package com.android.settingslib.core.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class Lifecycle extends LifecycleRegistry {
    public Lifecycle(@NonNull LifecycleOwner provider) {
        super(provider);
    }
}
