package com.ulfben.PlatformerMK3.input;

import java.util.ArrayList;
import java.util.Arrays;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

public class CompositeGameInput extends GameInput {
    protected final ArrayList<GameInput> mInputs = new ArrayList<>();
    private int mCount = 0;

    public CompositeGameInput(final GameInput... inputs) {
        super();
        mInputs.addAll(Arrays.asList(inputs));
        refresh();
    }

    public void setInput(final GameInput im){
        onPause();
        onStop();
        mInputs.clear();
        addInput(im);
        refresh();
    }

    public void addInput(final GameInput im){
        mInputs.add(im);
        refresh();
    }

    protected void refresh(){
        mCount = mInputs.size();
    }

    @Override
    public void update(final float dt) {
        GameInput temp;
        mJump = false;
        mHorizontalFactor = ZERO_INPUT;
        mVerticalFactor = ZERO_INPUT;
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
        for(final GameInput im : mInputs){
            im.onStart();
        }
    }

    @Override
    public void onStop() {
        for(final GameInput im : mInputs){
            im.onStop();
        }
    }

    @Override
    public void onPause() {
        for(final GameInput im : mInputs){
            im.onPause();
        }
    }

    @Override
    public void onResume() {
        for(final GameInput im : mInputs){
            im.onResume();
        }
    }
}
