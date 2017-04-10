package com.ulfben.PlatformerMK3.input;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.ulfben.PlatformerMK3.MainActivity;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

public class Gamepad extends GameInput implements GamepadListener {
    private static final String TAG = "Gamepad";
    MainActivity mActivity = null;
    public Gamepad(final MainActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerListener();
    }

    private void registerListener(){
        if(mActivity != null) {
            mActivity.setGamepadListener(this);
        }
    }
    private void unregisterListener(){
        if(mActivity != null) {
            mActivity.setGamepadListener(null);
        }
    }

    @Override
    public void onDestroy(){
        if(mActivity != null) {
            mActivity.setGamepadListener(null);
        }
        mActivity = null;
    }

    @Override
    public boolean dispatchGenericMotionEvent(final MotionEvent event) {
        if((event.getSource() & InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK){
            return false; //we don't consume this event
        }
        mHorizontalFactor = getInputFactor(event, MotionEvent.AXIS_X, MotionEvent.AXIS_HAT_X);
        mVerticalFactor = getInputFactor(event, MotionEvent.AXIS_Y, MotionEvent.AXIS_HAT_Y);
        clampInputs();
        return true; //we did consume this event
    }

    private float getInputFactor(final MotionEvent event, final int axis, final int fallbackAxis){
        final InputDevice device = event.getDevice();
        if(device == null){ //this has happened.
            Log.e(TAG, "getInputFactor: no device in MotionEvent.");
            return ZERO_INPUT;
        }
        final int source = event.getSource();
        float result = event.getAxisValue(axis);
        InputDevice.MotionRange range = device.getMotionRange(axis, source);
        if(Math.abs(result) <= range.getFlat()){
            result = event.getAxisValue(fallbackAxis);
            range = device.getMotionRange(fallbackAxis, source);
            if(Math.abs(result) <= range.getFlat()){
                result = ZERO_INPUT;
            }
        }
        return result;
    }


    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        final int action = event.getAction();
        final int keyCode = event.getKeyCode();
        boolean wasConsumed = false;
        //MotionEvent.ACTION_BUTTON_RELEASE
        //event.getActionButton()

        if(action == MotionEvent.ACTION_DOWN){// User started pressing a button
            if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
                mVerticalFactor -= 1;
                wasConsumed = true;
            }else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                mVerticalFactor += 1;
                wasConsumed = true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mHorizontalFactor -= 1;
                wasConsumed = true;
            } else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mHorizontalFactor += 1;
                wasConsumed = true;
            }
            if(isJumpKey(keyCode)){
                mJump = true;
                wasConsumed = true;
            }
        } else if(action == MotionEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                mVerticalFactor += 1;
                wasConsumed = true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                mVerticalFactor -= 1;
                wasConsumed = true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                mHorizontalFactor += 1;
                wasConsumed = true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mHorizontalFactor -= 1;
                wasConsumed = true;
            }
            if(isJumpKey(keyCode)){
                mJump = false;
                wasConsumed = true;
            }

            //the B button is "back" on all modern Androids. For older devices, we must do so by hand.
            if (keyCode == KeyEvent.KEYCODE_BUTTON_B || keyCode == KeyEvent.KEYCODE_BUTTON_START) {
                mActivity.onBackPressed();
                wasConsumed = true;
            }
        }
        return wasConsumed;
    }

    public boolean isJumpKey(final int keyCode){
        return keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_BUTTON_A
                || keyCode == KeyEvent.KEYCODE_BUTTON_X
                || keyCode == KeyEvent.KEYCODE_BUTTON_Y;
    }
}
