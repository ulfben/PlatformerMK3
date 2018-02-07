package com.ulfben.PlatformerMK3.input;
import android.view.MotionEvent;
import android.view.View;

import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.utilities.SysUtils;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

public class VirtualJoystick extends GameInput {
    private static final String TAG = "VirtualJoystick";
    private static final int SIZE_DP = 60; //TODO: make jooystick dimensions a setting
    private View mView;
    private float mRadius = 0;
    private float mStartingPositionX = 0;
    private float mStartingPositionY = 0;

    public VirtualJoystick(final View view) {
        super();
        mView = view;
        mRadius = SysUtils.dpToPx(SIZE_DP);
        registerListeners();
    }

    private class ActionButtonTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(final View v, final MotionEvent event){
            v.performClick();
            final int action = event.getActionMasked();
            if(action == MotionEvent.ACTION_DOWN){
                mJump = true;
            }else if(action == MotionEvent.ACTION_UP){
                mJump = false;
            }
            return true;
        }
    }

    private class JoystickTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(final View v, final MotionEvent event){
            v.performClick();
            final VirtualJoystickView joystickView = (VirtualJoystickView) v;
            final int action = event.getActionMasked();
            final float x = event.getX(0);
            final float y = event.getY(0);
            if(action == MotionEvent.ACTION_DOWN){
                mStartingPositionX = x;
                mStartingPositionY = y;
                joystickView.touchStart(mStartingPositionX, mStartingPositionY);
            }else if(action == MotionEvent.ACTION_MOVE){
                mHorizontalFactor = (x - mStartingPositionX)/ mRadius;
                mVerticalFactor = (y - mStartingPositionY)/ mRadius;
                clampInputs();
                joystickView.touchMove(x, y, mHorizontalFactor, mVerticalFactor);
            }else if(action == MotionEvent.ACTION_UP){
                mHorizontalFactor = ZERO_INPUT;
                mVerticalFactor = ZERO_INPUT;
                joystickView.touchUp();
            }
            return true;
        }
    }

    private void registerListeners(){
        View v = mView.findViewById(R.id.joystick_region); //can give NPE. this is intentional.
        v.setOnTouchListener(new JoystickTouchListener());
        v = mView.findViewById(R.id.button_region);
        v.setOnTouchListener(new ActionButtonTouchListener());
    }
    private void unregisterListeners(){
        if(mView != null) {
            View v = mView.findViewById(R.id.joystick_region); //can give NPE. this is intentional.
            v.setOnTouchListener(null);
            v = mView.findViewById(R.id.button_region);
            v.setOnTouchListener(null);
        }
    }

    @Override
    public void onDestroy(){
        unregisterListeners();
        mView = null;
    }
}
