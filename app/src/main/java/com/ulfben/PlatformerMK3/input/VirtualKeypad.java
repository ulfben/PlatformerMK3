package com.ulfben.PlatformerMK3.input;
import android.view.MotionEvent;
import android.view.View;

import com.ulfben.PlatformerMK3.R;
//Created by Ulf Benjaminsson (ulfben) on 2017-02-15.

public class VirtualKeypad extends InputManager implements View.OnTouchListener {

    public VirtualKeypad(View view){
        view.findViewById(R.id.keypad_up).setOnTouchListener(this);
        view.findViewById(R.id.keypad_down).setOnTouchListener(this);
        view.findViewById(R.id.keypad_left).setOnTouchListener(this);
        view.findViewById(R.id.keypad_right).setOnTouchListener(this);
        view.findViewById(R.id.keypad_jump).setOnTouchListener(this);
    }
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        int action = event.getActionMasked();
        int id = v.getId();
        if(action == MotionEvent.ACTION_DOWN){
            if(id == R.id.keypad_up){
                mVerticalFactor -= 1;
            }else if (id == R.id.keypad_down) {
                mVerticalFactor += 1;
            }
            if (id == R.id.keypad_left) {
                mHorizontalFactor -= 1;
            } else if(id == R.id.keypad_right) {
                mHorizontalFactor += 1;
            }
            if (id == R.id.keypad_jump) {
                mJump = true;
            }
        } else if(action == MotionEvent.ACTION_UP) {
            if (id == R.id.keypad_up) {
                mVerticalFactor += 1;
            } else if (id == R.id.keypad_down) {
                mVerticalFactor -= 1;
            }
            if (id == R.id.keypad_left) {
                mHorizontalFactor += 1;
            } else if (id == R.id.keypad_right) {
                mHorizontalFactor -= 1;
            }
            if (id == R.id.keypad_jump) {
                mJump = false;
            }
        }
        return false;
    }
}
