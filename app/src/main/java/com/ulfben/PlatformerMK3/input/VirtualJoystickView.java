package com.ulfben.PlatformerMK3.input;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

//used to render the joystick under the thumb. :P
public class VirtualJoystickView extends View {
    private float mX;
    private float mY;
    private Path mPath;
    private Paint mPaint;

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
        mPath = new Path();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setStrokeWidth(4f);
    }


    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath,  mPaint);
    }

    public void touchStart(final float x, final float y){
        mX = x;
        mY = y;
    }

    private static final float TOUCH_TOLERANCE = 4;
    public void touchMove(final float x, final float y) {
        final float dx = Math.abs(x - mX);
        final float dy = Math.abs(y - mY);
        mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
        mX = x;
        mY = y;
        mPath.reset();
        mPath.addCircle(mX, mY, 30, Path.Direction.CW);

    }

    public void touchUp() {
        mPath.lineTo(mX, mY);
        mPath.reset();
    }
}
