package com.ulfben.PlatformerMK3.input;
import android.view.KeyEvent;
import android.view.MotionEvent;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

public interface GamepadListener {
    boolean dispatchGenericMotionEvent(final MotionEvent event);
    boolean dispatchKeyEvent(final KeyEvent event);
}
