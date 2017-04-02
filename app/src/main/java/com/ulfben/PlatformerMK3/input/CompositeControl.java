package com.ulfben.PlatformerMK3.input;

import java.util.ArrayList;
import java.util.Arrays;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

public class CompositeControl extends InputManager {
    private ArrayList<InputManager> mInputs = new ArrayList<>();
    private int mCount = 0;

    public CompositeControl(final InputManager... inputs) {
        super();
        mInputs.addAll(Arrays.asList(inputs));
        mCount = mInputs.size();
    }

    public void setInput(final InputManager im){
        onPause();
        onStop();
        mInputs.clear();
        addInput(im);
    }

    public void addInput(final InputManager im){
        mInputs.add(im);
        mCount = mInputs.size();
    }

    @Override
    public void update(final float dt) {
        InputManager temp;
        mJump = false;
        mHorizontalFactor = 0f;
        mVerticalFactor = 0f;
        for(int i = 0; i < mCount; i++){
            temp = mInputs.get(i);
            temp.update(dt);
            mJump = mJump || temp.mJump;
            mHorizontalFactor += temp.mHorizontalFactor;
            mVerticalFactor += temp.mVerticalFactor;
        }
        clampInputs();
    }

    @Override
    public void onStart() {
        for(final InputManager im : mInputs){
            im.onStart();
        }
    }

    @Override
    public void onStop() {
        for(final InputManager im : mInputs){
            im.onStop();
        }
    }

    @Override
    public void onPause() {
        for(final InputManager im : mInputs){
            im.onPause();
        }
    }

    @Override
    public void onResume() {
        for(final InputManager im : mInputs){
            im.onResume();
        }
    }
}
