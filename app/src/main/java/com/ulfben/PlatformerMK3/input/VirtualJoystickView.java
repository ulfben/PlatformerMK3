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

//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

//used to render the joystick under the thumb.
public class VirtualJoystickView extends View {
    private static final String TAG = "VirtualJoystickView";
    private float mX;
    private float mY;
    private Path mRegion;
    private Path mNub;
    private Paint mPaint;
    private float mMaxDistance = 0;

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
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setStrokeWidth(8f);
        mMaxDistance = SysUtils.dpToPx(48*2)/2;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
        //mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawCircle(mX, mY, SysUtils.dpToPx(48*2), mPaint);
        canvas.drawPath(mNub, mPaint);
        canvas.drawPath(mRegion, mPaint);
    }

    public void touchStart(final float x, final float y){
        mX = x;
        mY = y;
        mRegion.addCircle(mX, mY, mMaxDistance*2f, Path.Direction.CW);
        invalidate();
    }

    //TODO: feed the factors for the nub, rather than recalculate.
    //probably swap to bitmap rendering as well.
    public void touchMove(final float x, final float y) {
        final float horizontalFactor = (x - mX) / (mMaxDistance);
        final float verticalFactor = (y - mY) / (mMaxDistance);
        float newX = mX + (mMaxDistance*horizontalFactor);
        float newY = mY + (mMaxDistance*verticalFactor);
        if(Math.abs(horizontalFactor) < 1f && Math.abs(verticalFactor) < 1f) {
            mNub.reset();
            mNub.addCircle(newX, newY, mMaxDistance / 3, Path.Direction.CW);
        }
        invalidate();
    }

    public void touchUp() {
        mRegion.reset();
        mNub.reset();
        invalidate();
    }
}
