package com.ulfben.PlatformerMK3.input;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-14.

public class GameInput {
    public static final float MIN = -1.0f;
    public static final float MAX = 1.0f;
    public static final float ZERO_INPUT = 0.0f;
    public float mVerticalFactor = ZERO_INPUT;
    public float mHorizontalFactor = ZERO_INPUT;
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

    public void onDestroy(){

    }
}
