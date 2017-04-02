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

    public VirtualJoystick(View view) {
        super();
        view.findViewById(R.id.joystick_region)
                .setOnTouchListener(new JoystickTouchListener());
        view.findViewById(R.id.button_region)
                .setOnTouchListener(new ActionButtonTouchListener());
        mMaxDistance = SysUtils.dpToPx(48*2); //48dp = minimum hit target.
        Log.d(TAG, "MaxDistance (pixels): " + mMaxDistance);
    }

    private class ActionButtonTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event){
            int action = event.getActionMasked();
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
        public boolean onTouch(View v, MotionEvent event){
            int action = event.getActionMasked();
            if(action == MotionEvent.ACTION_DOWN){
                mStartingPositionX = event.getX(0);
                mStartingPositionY = event.getY(0);
            }else if(action == MotionEvent.ACTION_UP){
                mHorizontalFactor = 0.0f;
                mVerticalFactor = 0.0f;
            }else if(action == MotionEvent.ACTION_MOVE){
                //get the proportion to the maxDistance
                mHorizontalFactor = (event.getX(0) - mStartingPositionX)/mMaxDistance;
                mVerticalFactor = (event.getY(0) - mStartingPositionY)/mMaxDistance;
                clampInputs();
            }
            return true;
        }
    }
}
