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

    /* Since the list length almost never changes during the application lifetime I really want to cache the value.
    This makes it easy for child-classes to break things by forgetting to update the mCount when needed.
    Ergo: this is not elegant, but it's a compromise I'm willing to make.
    * */
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

    @Override
    public void onDestroy() {
        for(final GameInput im : mInputs){
            im.onDestroy();
        }
        mInputs.clear();
    }
}
