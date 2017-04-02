package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.ulfben.PlatformerMK3.Animation;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.GameEngine;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-28.

public class Spears extends GameObject {
    private static final String ALIGN = "BOTTOM";
    private Animation mAnim = null;
    private Bitmap mCurrentFrame = null;

    public Spears(final GameEngine engine, final String sprite){
        super(engine, sprite, DEFAULT_LOCATION, DEFAULT_LOCATION, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        init();
    }

    public Spears(final GameEngine engine,final String sprite, final float x, final float y){
        super(engine, sprite, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        init();
    }

    private void init(){
        mAnim = new Animation(mEngine, R.drawable.spear_anim, DEFAULT_WIDTH, 0f);
        mCurrentFrame = mAnim.getCurrentBitmap();
        setPosition(mWorldLocation.x, mWorldLocation.y);
        updateBounds();
    }

    @Override
    public void postConstruct(){
        //no-op at the moment. using animations
    }

    @Override
    public void render(final Canvas canvas, final Paint paint){
        mTransform.reset();
        mEngine.worldToScreen(mBounds, GameObject.screenCord);
        mTransform.postTranslate(GameObject.screenCord.x, GameObject.screenCord.y);
        canvas.drawBitmap(mCurrentFrame, mTransform, paint);
    }

    @Override
    protected synchronized void updateBounds(){
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
        mCurrentFrame = mAnim.getCurrentBitmap();
        updateBounds();
    }

    @Override
    public void destroy(){
        super.destroy();
        mAnim.destroy();
        mAnim = null;
        mCurrentFrame = null;
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
