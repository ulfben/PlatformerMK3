package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.utilities.Axis;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
import com.ulfben.PlatformerMK3.utilities.BitmapUtils;
//Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

public class GameObject {
    private static final String TAG = "GameObject";
    public static final float DEFAULT_LOCATION = 0f;
    public static final float DEFAULT_HEIGHT = 1f; //meters
    public static final float DEFAULT_WIDTH = 1f;

    public static GameEngine mEngine; //set by GameEngine, shared by all GameObjects
    public final PointF mWorldLocation = new PointF(DEFAULT_LOCATION, DEFAULT_LOCATION);
    public float mWidth = DEFAULT_WIDTH;
    public float mHeight = DEFAULT_HEIGHT;
    protected Bitmap mBitmap = null; //We do not own the bitmap. The BitmapPool will recycle it!

    public GameObject(final String sprite){
        super();
        init(sprite, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GameObject(final String sprite, float width, float height){
        super();
        init(sprite, width, height);
    }

    private void init(final String sprite, float width, float height){
        mWidth = width;
        mHeight = height;
        if(sprite.isEmpty()) { //some objects will want to load their own assets.
            return;
        }
        mBitmap = BitmapPool.createBitmap(mEngine, sprite, mWidth, mHeight);
        if(mBitmap == null){
            throw new AssertionError("Failed to intitialize game object!");
        }
    }

    public void render(final Canvas canvas, final Matrix transform, final Paint paint){
        canvas.drawBitmap(mBitmap, transform, paint);
    }

    public void update(final float dt){}

    public void destroy(){
        mBitmap = null;
    }

    public void onCollision(final GameObject that){}

    public boolean isColliding(final GameObject that){
        return GameObject.isAABBOverlapping(this, that);
    }

    public static boolean isAABBOverlapping(final GameObject a, final GameObject b){
        return !(a.right() < b.left()
                || b.right() < a.left()
                || a.bottom() < b.top()
                || b.bottom() < a.top());
    }
    //SAT intersection test. http://www.metanetsoftware.com/technique/tutorialA.html
    //returns true on intersection, and sets the least intersecting axis in overlap
    protected static final PointF overlap = new PointF(0,0); //Q&D PointF pool, for collision reactions
    public static boolean getOverlap(final GameObject a, final GameObject b, final PointF overlap) {
        overlap.x = 0.0f;
        overlap.y = 0.0f;
        final float centerDeltaX = a.centerX() - b.centerX();
        final float halfWidths = (a.width() + b.width()) * 0.5f;

        if (Math.abs(centerDeltaX) > halfWidths) return false; //no overlap on x == no collision

        final float centerDeltaY = a.centerY() - b.centerY();
        final float halfHeights = (a.height() + b.height()) * 0.5f;

        if (Math.abs(centerDeltaY) > halfHeights) return false; //no overlap on y == no collision

        final float dx = halfWidths - Math.abs(centerDeltaX); //overlap on x
        final float dy = halfHeights - Math.abs(centerDeltaY); //overlap on y
        if (dy < dx) {
            overlap.y = (centerDeltaY < 0) ? -dy : dy;
        } else if (dy > dx) {
            overlap.x = (centerDeltaX < 0) ? -dx : dx;
        } else {
            overlap.x = (centerDeltaX < 0) ? -dx : dx;
            overlap.y = (centerDeltaY < 0) ? -dy : dy;
        }
        return true;
    }

    public float x(){ return mWorldLocation.x; }
    public float y(){ return mWorldLocation.y; }
    public float centerX(){ return mWorldLocation.x + mWidth*0.5f; }
    public float centerY(){ return mWorldLocation.y + mHeight*0.5f; }
    public float width(){ return mWidth; }
    public float height(){ return mHeight; }
    public float left(){ return mWorldLocation.x; }
    public float right(){ return mWorldLocation.x + mWidth; }
    public float top(){ return mWorldLocation.y; }
    public float bottom(){ return mWorldLocation.y + mHeight; }

   // public void offset(final float x, final float y){ mBounds.left += x; mBounds.top += y; }
    public void setPosition(final float x, final float y){ mWorldLocation.x = x; mWorldLocation.y = y; }
    public void setWidth(final float width) {mWidth = width; }
    public void setHeight(final float height) {mHeight = height;}
    public void setWidthHeight(final float w, final float h){mWidth = w; mHeight=h;}

}
