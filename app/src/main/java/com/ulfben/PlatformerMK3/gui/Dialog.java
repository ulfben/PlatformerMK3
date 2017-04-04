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
public class Dialog implements View.OnTouchListener {
    protected final MainActivity mParent;
    private ViewGroup mRootLayout; //background common to all dialogues
    private View mRootView; //the dialog itself
    private boolean mIsShowing = false;

    public Dialog(final MainActivity activity) {
        super();
        mParent = activity;
    }

    protected void onViewClicked() {
        // Ignore clicks on this view
    }

    protected void setContentView(final int dialogResId) {
        final ViewGroup activityRoot = (ViewGroup) mParent.findViewById(android.R.id.content);
        mRootView = LayoutInflater.from(mParent).inflate(dialogResId, activityRoot, false);
    }

    public void show() {
        if (mIsShowing) {
            return;
        }
        mIsShowing = true;
        final ViewGroup activityRoot = (ViewGroup) mParent.findViewById(android.R.id.content);
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
        final ViewGroup activityRoot = (ViewGroup) mParent.findViewById(android.R.id.content);
        activityRoot.removeView(mRootLayout);
    }

    protected View findViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        return true; //ignore touch
        //call dismiss() if you want to exit dialogues by clicking outside of them
    }

    public boolean isShowing() {
        return mIsShowing;
    }
}
