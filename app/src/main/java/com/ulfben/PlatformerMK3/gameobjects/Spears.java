package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.ulfben.PlatformerMK3.Animation;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-28.

public class Spears extends GameObject {
    private static final String ALIGN = "BOTTOM";
    private Animation mAnim = null;

    public Spears(final GameEngine engine){
        super(engine, "");
        mAnim = new Animation(mEngine, R.drawable.spear_anim, DEFAULT_WIDTH, 0f);
        mBitmap = mAnim.getCurrentBitmap();
        setPosition(mWorldLocation.x, mWorldLocation.y);
        updateBounds();
    }

    @Override
    protected void updateBounds(){
        if(mAnim != null) {
            mHeight = mAnim.getCurrentHeightMeters(); //scaled
            mWidth = mAnim.getCurrentWidthMeters();
        }
        mBounds.left = mWorldLocation.x;
        mBounds.top = mWorldLocation.y - mHeight;
        mBounds.right = mWorldLocation.x + mWidth;
        mBounds.bottom =  mBounds.top + mHeight;
    }

    @Override
    public void update(final float dt){
        mAnim.update(dt);
        mBitmap = mAnim.getCurrentBitmap();
        updateBounds();
    }

    @Override
    public void destroy(){
        if(mAnim != null) {
            mAnim.destroy();
            mAnim = null;
        }
        super.destroy();
    }

    @Override
    public void setPosition(final float x, final float y){
        mWorldLocation.x = x;
        mWorldLocation.y = y;
        if("BOTTOM".equals(ALIGN)){
            mWorldLocation.y += 1f; //add 1 meter, pushing us into the ground. We will move back from there.
        }
        updateBounds();
    }
}
