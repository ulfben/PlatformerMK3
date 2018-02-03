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

    protected final GameEngine mEngine;
    public final PointF mWorldLocation = new PointF(DEFAULT_LOCATION, DEFAULT_LOCATION);
    public float mWidth = DEFAULT_WIDTH;
    public float mHeight = DEFAULT_HEIGHT;
    public final RectF mBounds = new RectF(DEFAULT_LOCATION, DEFAULT_LOCATION, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    protected Bitmap mBitmap = null; //We do not own the bitmap. The BitmapPool will recycle it!

    public GameObject(final GameEngine engine, final String sprite){
        super();
        mEngine = engine;
        init(sprite, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GameObject(final GameEngine engine, final String sprite, float width, float height){
        super();
        mEngine = engine;
        init(sprite, width, height);
    }

    private void init(final String sprite, float width, float height){
        mWidth = width;
        mHeight = height;
        updateBounds();
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
        return RectF.intersects(this.mBounds, that.mBounds);
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

    protected void updateBounds(){
        mBounds.left = mWorldLocation.x;
        mBounds.top = mWorldLocation.y;
        mBounds.right = mWorldLocation.x + mWidth;
        mBounds.bottom = mWorldLocation.y + mHeight;
    }

    public float x(){ return mBounds.left; }
    public float y(){ return mBounds.top; }
    public float centerX(){ return mBounds.centerX(); }
    public float centerY(){ return mBounds.centerY(); }
    public float width(){ return mBounds.width(); }
    public float height(){ return mBounds.height(); }
    public float left(){ return mBounds.left; }
    public float right(){ return mBounds.right; }
    public float top(){ return mBounds.top; }
    public float bottom(){ return mBounds.bottom; }

   // public void offset(final float x, final float y){ mBounds.left += x; mBounds.top += y; }
    public void setPosition(final float x, final float y){ mWorldLocation.x = x; mWorldLocation.y = y; updateBounds(); }
    public void setWidth(final float width) {mWidth = width; updateBounds();}
    public void setHeight(final float height) {mHeight = height; updateBounds();}
    public void setWidthHeight(final float w, final float h){mWidth = w; mHeight=h; updateBounds();}

}
