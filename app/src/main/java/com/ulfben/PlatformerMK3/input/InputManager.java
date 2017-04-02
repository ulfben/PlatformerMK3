package com.ulfben.PlatformerMK3.input;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-14.

public abstract class InputManager {
    public static final float MIN = -1.0f;
    public static final float MAX = 1.0f;
    public float mVerticalFactor = 0.0f;
    public float mHorizontalFactor = 0.0f;
    public boolean mJump = false;

    protected void clampInputs(){
        if(mVerticalFactor < MIN){
            mVerticalFactor = MIN;
        }else if(mVerticalFactor > MAX){
            mVerticalFactor = MAX;
        }
        if(mHorizontalFactor < MIN){
            mHorizontalFactor = MIN;
        }else if(mHorizontalFactor > MAX){
            mHorizontalFactor = MAX;
        }
    }
    public void update(final float dt) {}

    public void onStart() {}

    public void onStop() {}

    public void onPause() {}

    public void onResume() {}
}
