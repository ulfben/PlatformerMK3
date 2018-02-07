package com.ulfben.PlatformerMK3.input;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-14.

import com.ulfben.PlatformerMK3.utilities.Utils;
public class GameInput {
    private static final float MIN = -1.0f;
    private static final float MAX = 1.0f;
    static final float ZERO_INPUT = 0.0f;
    float mVerticalFactor = ZERO_INPUT;
    public float mHorizontalFactor = ZERO_INPUT;
    public boolean mJump = false;

    void clampInputs(){
        mVerticalFactor = Utils.clamp(mVerticalFactor, MIN, MAX);
        mHorizontalFactor = Utils.clamp(mHorizontalFactor, MIN, MAX);
    }
    void update(final float dt) {}

    void onStart() {}

    void onStop() {}

    void onPause() {}

    void onResume() {}

    void onDestroy(){

    }
}
