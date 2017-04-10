package com.ulfben.PlatformerMK3.input;
import android.view.MotionEvent;
import android.view.View;

import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.utilities.SysUtils;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

public class VirtualJoystick extends GameInput {
    private static final String TAG = "VirtualJoystick";
    private static final int SIZE_DP = 60; //TODO: make settings.
    protected float mRadius = 0;
    protected float mStartingPositionX = 0;
    protected float mStartingPositionY = 0;

    public VirtualJoystick(final View view) {
        super();
        view.findViewById(R.id.joystick_region)
                .setOnTouchListener(new JoystickTouchListener());
        view.findViewById(R.id.button_region)
                .setOnTouchListener(new ActionButtonTouchListener());
        mRadius = SysUtils.dpToPx(SIZE_DP);
    }

    private class ActionButtonTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(final View v, final MotionEvent event){
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
                mHorizontalFactor = 0.0f;
                mVerticalFactor = 0.0f;
                joystickView.touchUp();
            }
            return true;
        }
    }
}
