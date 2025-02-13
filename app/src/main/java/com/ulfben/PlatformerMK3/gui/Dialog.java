package com.ulfben.PlatformerMK3.gui;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-04.
//this base dialog displays a gray overlay for other
//dialogues to draw over
public class Dialog implements View.OnTouchListener, View.OnFocusChangeListener {
    private static final String TAG = "Dialog";
    private final MainActivity mParent;
    private ViewGroup mRootLayout; //background common to all dialogues
    private View mRootView; //the dialog itself
    private boolean mIsShowing = false;

    Dialog(final MainActivity activity) {
        super();
        mParent = activity;
    }

    void setContentView(final int dialogResId) {
        final ViewGroup activityRoot = mParent.findViewById(android.R.id.content);
        mRootView = LayoutInflater.from(mParent).inflate(dialogResId, activityRoot, false);
    }

    public void show() {
        if (mIsShowing) {
            return;
        }
        mIsShowing = true;
        final ViewGroup activityRoot = mParent.findViewById(android.R.id.content);
        mRootLayout = (ViewGroup) LayoutInflater.from(mParent).inflate(R.layout.dialog_overlay, activityRoot, false);
        activityRoot.addView(mRootLayout);
        mRootLayout.setOnTouchListener(this);
        mRootLayout.addView(mRootView);
    }

    public void dismiss() {
        if (!mIsShowing) {
            return;
        }
        mIsShowing = false;
        hideViews();
    }

    private void hideViews() {
        mRootLayout.removeView(mRootView);
        final ViewGroup activityRoot = mParent.findViewById(android.R.id.content);
        activityRoot.removeView(mRootLayout);
    }

    View findViewById(final int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        v.performClick();
        return true; //ignore touch
        //call dismiss() if you want to exit dialogues by clicking outside of them
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    @Override
    public void onFocusChange(final View view, final boolean b) {
       // Log.d(TAG, "onFocusChange: " + view.toString() +" isFocused: " + b);
    }
}
