package com.ulfben.PlatformerMK3.input;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.utilities.SysUtils;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

public class VirtualJoystick extends InputManager {
    private static final String TAG = "VirtualJoystick";
    protected float mMaxDistance = 0;
    protected float mStartingPositionX = 0;
    protected float mStartingPositionY = 0;

    public VirtualJoystick(final View view) {
        super();
        view.findViewById(R.id.joystick_region)
                .setOnTouchListener(new JoystickTouchListener());
        view.findViewById(R.id.button_region)
                .setOnTouchListener(new ActionButtonTouchListener());
        //TODO: find a better way to configure the size of the virtual joystick.
        mMaxDistance = SysUtils.dpToPx(48*2); //48dp = minimum hit target.
        Log.d(TAG, "MaxDistance (pixels): " + mMaxDistance);
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
                //get the proportion to the maxDistance
                mHorizontalFactor = (x - mStartingPositionX)/mMaxDistance;
                mVerticalFactor = (y - mStartingPositionY)/mMaxDistance;
                joystickView.touchMove(x, y);
                clampInputs();
            }else if(action == MotionEvent.ACTION_UP){
                mHorizontalFactor = 0.0f;
                mVerticalFactor = 0.0f;
                joystickView.touchUp();
            }
            return true;
        }

    }
}
