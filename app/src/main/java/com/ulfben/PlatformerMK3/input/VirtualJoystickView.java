package com.ulfben.PlatformerMK3.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.ulfben.PlatformerMK3.utilities.SysUtils;

public class VirtualJoystickView extends View {
    private static final String TAG = "VirtualJoystickView";
    private static final int SIZE_DP = 60; //TODO: make settings
    private static final int NUB_SIZE_DP = SIZE_DP/4;
    private static final int JOYSTICK_COLOR = Color.GREEN;
    private float mX;
    private float mY;
    private Path mRegion;
    private Path mNub;
    private Paint mPaint;
    private int mRadius = 0;
    private int mNubRadius = 0;

    public VirtualJoystickView(final Context context) {
        super(context);
        init();
    }

    public VirtualJoystickView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VirtualJoystickView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mRegion = new Path();
        mNub = new Path();
        mPaint.setAntiAlias(true);
        mPaint.setColor(JOYSTICK_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setStrokeWidth(8f);
        mRadius = SysUtils.dpToPx(SIZE_DP);
        mNubRadius = SysUtils.dpToPx(NUB_SIZE_DP);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mRegion, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mNub, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public void touchStart(final float x, final float y){
        mX = x;
        mY = y;
        mRegion.addCircle(mX, mY, mRadius, Path.Direction.CW);
        invalidate();
    }

    //TODO: project correctly within radius
    public void touchMove(final float x, final float y, final float horizontalFactor, final float verticalFactor){
        final float dx = x-mX;
        final float dy = y-mY;
        final float newX = mX + (mRadius * horizontalFactor);
        final float newY = mY + (mRadius * verticalFactor);
        mNub.reset();
        mNub.addCircle(newX, newY, mNubRadius, Path.Direction.CW);
        invalidate();
    }

    public void touchUp() {
        mRegion.reset();
        mNub.reset();
        invalidate();
    }
}