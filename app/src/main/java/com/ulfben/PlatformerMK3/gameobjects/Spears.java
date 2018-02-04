package com.ulfben.PlatformerMK3.gameobjects;

import com.ulfben.PlatformerMK3.Animation;
import com.ulfben.PlatformerMK3.R;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-28.

public class Spears extends GameObject {
    private Animation mAnim = null;
    //Spears change height, but are not affected by gravity
    //I save initial position so I can offset from that when height changes
    private float mInitialPositionY = 0;

    public Spears(){
        super("");
        mAnim = new Animation(mEngine, R.drawable.spear_anim, DEFAULT_WIDTH, 0f);
        mBitmap = mAnim.getCurrentBitmap();
        setPosition(x, y);
        height = mAnim.getCurrentHeightMeters(); //scaled
        width = mAnim.getCurrentWidthMeters();
    }

    @Override
    public void update(final float dt){
        mAnim.update(dt);
        mBitmap = mAnim.getCurrentBitmap();
        height = mAnim.getCurrentHeightMeters(); //scaled
        width = mAnim.getCurrentWidthMeters();
        //ensure we're "bottom aligned" to our tile, even when height changes
        //assumes 1 meter per tile, and that Spears will be <1 meter tall.
        y = mInitialPositionY + (1f- height);
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
        this.x = x;
        this.mInitialPositionY = y;
    }
}
