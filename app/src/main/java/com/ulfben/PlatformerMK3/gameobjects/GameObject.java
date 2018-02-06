package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
//Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

public class GameObject {
    //The engine reference is set by the engine, and must be nulled by the engine!
    public static GameEngine mEngine;
    private static final String TAG = "GameObject";
    public static final float DEFAULT_LOCATION = 0f; //meters
    public static final float DEFAULT_HEIGHT = 1f;   //meters
    public static final float DEFAULT_WIDTH = 1f;    //meters
    public float x = DEFAULT_LOCATION; //world location, meters!
    public float y = DEFAULT_LOCATION;
    public float width = DEFAULT_WIDTH;
    public float height = DEFAULT_HEIGHT;
    protected Bitmap mBitmap = null; //We do not own the bitmap. The BitmapPool will recycle it!
    private String mSprite ="";

    public GameObject(final String sprite){
        super();
        init(sprite, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GameObject(final String sprite, float width, float height){
        super();
        init(sprite, width, height);
    }

    private void init(final String sprite, float width, float height){
        this.width = width;
        this.height = height;
        mSprite = sprite;
        if(sprite.isEmpty()) { //some child classes will want to load their own assets.
            return;
        }
        resampleSprite();
    }

    public void resampleSprite(){
        if(mBitmap != null) {
            BitmapPool.remove(mBitmap);
        }
        mBitmap = BitmapPool.createBitmap(mEngine, mSprite, width, height);
        if(mBitmap == null){
            Log.e(TAG, "Failed to create game object bitmap! Sprite: " + mSprite);
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

    //Some good reading on bounding-box intersection tests:
    //https://gamedev.stackexchange.com/questions/586/what-is-the-fastest-way-to-work-out-2d-bounding-box-intersection
    public static boolean isAABBOverlapping(final GameObject a, final GameObject b){
        return  !(a.right() <= b.left()
                || b.right() <= a.left()
                || a.bottom() <= b.top()
                || b.bottom() <= a.top());
    }

    //SAT intersection test. http://www.metanetsoftware.com/technique/tutorialA.html
    //returns true on intersection, and sets the least intersecting axis in overlap
    protected static final PointF overlap = new PointF(0,0); //Q&D PointF pool for collision detection. Assumes single threading.
    public static boolean getOverlap(final GameObject a, final GameObject b, final PointF overlap) {
        overlap.x = 0.0f;
        overlap.y = 0.0f;
        final float centerDeltaX = a.centerX() - b.centerX();
        final float halfWidths = (a.width() + b.width()) * 0.5f;
        float dx = Math.abs(centerDeltaX);//cache the abs, we need it twice

        if (dx > halfWidths) return false; //no overlap on x == no collision

        final float centerDeltaY = a.centerY() - b.centerY();
        final float halfHeights = (a.height() + b.height()) * 0.5f;
        float dy = Math.abs(centerDeltaY);

        if (dy > halfHeights) return false; //no overlap on y == no collision

        dx = halfWidths - dx; //overlap on x
        dy = halfHeights - dy; //overlap on y
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

    public float x(){ return x; }
    public float y(){ return y; }
    public float centerX(){ return x + width*0.5f; }
    public float centerY(){ return y + height*0.5f; }
    public float width(){ return width; }
    public float height(){ return height; }
    public float left(){ return x; }
    public float right(){ return x + width; }
    public float top(){ return y; }
    public float bottom(){ return y + height; }

   // public void offset(final float x, final float y){ mBounds.left += x; mBounds.top += y; }
    public void setPosition(final float x, final float y){ this.x = x; this.y = y; }
}
