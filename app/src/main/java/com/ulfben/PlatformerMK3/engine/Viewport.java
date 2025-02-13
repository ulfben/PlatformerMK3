package com.ulfben.PlatformerMK3.engine;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.utilities.Utils;

import java.util.Locale;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

class Viewport {
    private static final float EASE_X = 0.125f;//% of tracking distance to close each update
    private static final float EASE_Y = 0.25f; //move faster on the shorter axis, so the player never falls out of view
    private final PointF mMaxPosition = new PointF(Float.MAX_VALUE, Float.MAX_VALUE);
    private final PointF mMinPosition = new PointF(-Float.MIN_VALUE, -Float.MIN_VALUE);
    private boolean mIsBounded = false;
    private float mWorldWidth = Float.MAX_VALUE;
    private float mWorldHeight = Float.MAX_VALUE;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenCenterX;
    private int mScreenCenterY;
    private float mMetersToShowX;
    private float mMetersToShowY;
    private int mPixelsPerMeterX;
    private int mPixelsPerMeterY;
    private float mHalfDistX;
    private float mHalfDistY;
    private final PointF mLookAt = new PointF(0f,0f); //current world coordinate in center view
    private GameObject mTarget = null;
    private final static float BUFFER = 2f; //render this many meters outside of the viewport on each axis, to avoid visual gaps
    private String DBG_VIEWPORT = "";

    // You can provide both or only one of the metersToShow arguments.
    // By setting one to 0, Viewport will calculate the optimal distance
    // based on the aspect ratio of screenWidth and screenHeight.
    public Viewport(final int screenWidth, final int screenHeight, final float metersToShowX, final float metersToShowY){
        super();
        init(screenWidth, screenHeight);
        setMetersToShow(metersToShowX, metersToShowY);
    }

    public Viewport(final int screenWidth, final int screenHeight, final float minimumMetersToShow){
        super();
        init(screenWidth, screenHeight);
        setMetersToShowOnShortestAxis(minimumMetersToShow, screenWidth, screenHeight);
    }

    private void init(final int screenWidth, final int screenHeight){
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mScreenCenterX = mScreenWidth / 2;
        mScreenCenterY = mScreenHeight / 2;
        mLookAt.x = 0.0f;
        mLookAt.y = 0.0f;
    }

    private void setMetersToShowOnShortestAxis(final float metersToShow, final int screenWidth, final int screenHeight){
        if(screenWidth < screenHeight){
            setMetersToShow(metersToShow, 0.0f);
        }else if(screenWidth > screenHeight){
            setMetersToShow(0.0f, metersToShow);
        }else{
            setMetersToShow(metersToShow, metersToShow);
        }
    }

    //setMetersToShow calculates the number of physical pixels per meters
    //so that we can translate our game world (meters) to the screen (pixels)
    //provide the dimension(s) you want to lock. The viewport will automatically
    // size the other axis to fill the screen perfectly.
    private void setMetersToShow(float metersToShowX, float metersToShowY){
        if (metersToShowX <= 0f && metersToShowY <= 0f) throw new IllegalArgumentException("One of the dimensions must be provided!");
        //formula: new height = (original height / original width) x new width
        mMetersToShowX = metersToShowX;
        mMetersToShowY = metersToShowY;
        if(metersToShowX == 0f || metersToShowY == 0f){
            if(metersToShowY > 0f) { //if Y is configured, calculate X
                mMetersToShowX = ((float) mScreenWidth / mScreenHeight) * metersToShowY;
            }else { //if X is configured, calculate Y
                mMetersToShowY = ((float) mScreenHeight / mScreenWidth) * metersToShowX;
            }
        }
        mHalfDistX = (mMetersToShowX+BUFFER) * 0.5f;
        mHalfDistY = (mMetersToShowY+BUFFER) * 0.5f;
        mPixelsPerMeterX = (int)(mScreenWidth / mMetersToShowX);
        mPixelsPerMeterY = (int)(mScreenHeight / mMetersToShowY);
        updateMinMaxPosition();
        DBG_VIEWPORT = String.format(Locale.getDefault(),"Viewport [%dpx, %dpx / %.1fm, %.1fm]", mScreenWidth, mScreenHeight, mMetersToShowX, mMetersToShowY);
    }

    void setBounds(final float width, final float height){
        mIsBounded = true;
        mWorldWidth = width;
        mWorldHeight = height;
        updateMinMaxPosition();
    }

    private void updateMinMaxPosition(){
        if(!mIsBounded) {
            return;
        }
        mMinPosition.x = (mMetersToShowX*0.5f);
        mMinPosition.y = (mMetersToShowY*0.5f);
        mMaxPosition.x = Math.max(mMinPosition.x, mWorldWidth-(mMetersToShowX*0.5f));
        mMaxPosition.y = Math.max(mMinPosition.y, mWorldHeight-(mMetersToShowY*0.5f));
    }

    public void follow(final GameObject go){
        mTarget = go;
    }

    public void update(final float dt){
        if(mTarget != null) {
            mLookAt.x += (mTarget.centerX() - mLookAt.x) * EASE_X;
            mLookAt.y += (mTarget.centerY() - mLookAt.y) * EASE_Y;
        }
        if(mIsBounded){
            Utils.clamp(mLookAt, mMinPosition, mMaxPosition);
        }
    }

    public void lookAt(final GameObject obj){
        mLookAt.x = obj.centerX();
        mLookAt.y = obj.centerY();
    }

    public void lookAt(final PointF pos){
        mLookAt.x = pos.x;
        mLookAt.y = pos.y;
    }

    public void lookAt(final float x, final float y){
        mLookAt.x = x;
        mLookAt.y = y;
    }

    public void worldToScreen(final PointF worldPos, final Point screenPos){
        screenPos.x = (int) (mScreenCenterX - ((mLookAt.x - worldPos.x) * mPixelsPerMeterX));
        screenPos.y = (int) (mScreenCenterY - ((mLookAt.y - worldPos.y) * mPixelsPerMeterY));
    }

    public void worldToScreen(final RectF bounds, final Point screenPos){
        screenPos.x = (int) (mScreenCenterX - ((mLookAt.x - bounds.left) * mPixelsPerMeterX));
        screenPos.y = (int) (mScreenCenterY - ((mLookAt.y - bounds.top) * mPixelsPerMeterY));
    }

    public void worldToScreen(final GameObject obj, final Point screenPos){
        screenPos.x = (int) (mScreenCenterX - ((mLookAt.x - obj.x) * mPixelsPerMeterX));
        screenPos.y = (int) (mScreenCenterY - ((mLookAt.y - obj.y) * mPixelsPerMeterY));
    }

    public void worldToScreen(final RectF bounds, final Rect out){
        final int left = (int) (mScreenCenterX - ((mLookAt.x - bounds.left) * mPixelsPerMeterX));
        final int top = (int) (mScreenCenterY - ((mLookAt.y - bounds.top) * mPixelsPerMeterY));
        final int right = (int) (left + (bounds.width() * mPixelsPerMeterX));
        final int bottom = (int) (top + (bounds.height() * mPixelsPerMeterY));
        out.set(left, top, right, bottom);
    }

    public void worldToScreen(final PointF worldPos, final float objectWidth, final float objectHeight, final Rect out){
        final int left = (int) (mScreenCenterX - ((mLookAt.x - worldPos.x) * mPixelsPerMeterX));
        final int top = (int) (mScreenCenterY - ((mLookAt.y - worldPos.y) * mPixelsPerMeterY));
        final  int right = (int) (left + (objectWidth * mPixelsPerMeterX));
        final int bottom = (int) (top + (objectHeight * mPixelsPerMeterY));
        out.set(left, top, right, bottom);
    }

    public boolean inView(final RectF bounds) {
        final float right = (mLookAt.x + mHalfDistX);
        final float left = (mLookAt.x - mHalfDistX);
        final float bottom = (mLookAt.y + mHalfDistY);
        final float top  = (mLookAt.y - mHalfDistY);
        return (bounds.left < right && bounds.right > left)
                && (bounds.top < bottom && bounds.bottom > top);
    }

	public boolean inView(final GameObject obj) {
        final float maxX = (mLookAt.x + mHalfDistX);
        final float minX = (mLookAt.x - mHalfDistX)-obj.width;
        final float maxY = (mLookAt.y + mHalfDistY);
        final float minY  = (mLookAt.y - mHalfDistY)-obj.height;
        return (obj.x > minX && obj.x < maxX)
                && (obj.y > minY && obj.y < maxY);
    }

    public float getHorizontalView(){ return mMetersToShowX; }
    public float getVerticalView(){ return mMetersToShowY; }
    public int getScreenWidth() {
        return mScreenWidth;
    }
    public int getScreenHeight(){
        return mScreenHeight;
    }
    public int getPixelsPerMeterX(){
        return mPixelsPerMeterX;
    }
    public int getPixelsPerMeterY(){
        return mPixelsPerMeterY;
    }
    public String toString(){ return DBG_VIEWPORT; }
}
