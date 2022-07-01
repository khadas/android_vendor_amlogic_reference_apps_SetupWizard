package com.droidlogic.setupwizard.utils;

import android.view.KeyEvent;

public class Backdoor {

    private final int[] defaultKeycodeOrderArray = {KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT};
    private final int[] inputKeycodeArray = new int[defaultKeycodeOrderArray.length];
    private int index = 0;

    public boolean input(KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
            inputKeycodeArray[index] = keyEvent.getKeyCode();
            if (inputKeycodeArray[index] == KeyEvent.KEYCODE_DPAD_RIGHT && verify()) {
                return true;
            }
            index++;
            if (index >= inputKeycodeArray.length) {
                index = 0;
            }
        }
        return false;
    }

    private boolean verify() {
        int lastIndex = inputKeycodeArray.length - 1;
        for (int i = lastIndex; i >= 0; i--) {
            int currentIndex = index - (lastIndex - i);
            if (currentIndex < 0) {
                currentIndex += inputKeycodeArray.length;
            }
            if (inputKeycodeArray[currentIndex] != defaultKeycodeOrderArray[i]) {
                return false;
            }
        }
        return true;
    }

}
