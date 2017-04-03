package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.PointF;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.utilities.Random;
import com.ulfben.PlatformerMK3.utilities.Utils;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-24.

public class DynamicGameObject extends GameObject {
    private static final String TAG = "DynamicGameObject";
    private static final float MAX_DELTA = 0.48f; //maximum change in position over a single frame. keep < smallest object to avoid tunneling (1.0f = 1m)
    protected static final float GRAVITATIONAL_ACCELERATION = 40f;
    protected PointF mVelocity = new PointF(0.0f, 0.0f); //current velocity
    protected PointF mTargetSpeed = new PointF(0.0f, 0.0f); //target velocity
    protected PointF mAcceleration = new PointF(1.0f, 1.0f); //fake acceleration; how fast we approach targetspeed
    protected float mGravity = GRAVITATIONAL_ACCELERATION; //non-static member, some objects will not want gravity
    protected float mFriction = 1.0f; //1 == no friction, 0 == no motion.
    protected boolean mIsOnGround = false;

    DynamicGameObject(final GameEngine engine, final String sprite) {
        super(engine, sprite);
    }
    DynamicGameObject(final GameEngine engine, final String sprite, final float x, final float y) {
        super(engine, sprite, x, y);
    }
    public DynamicGameObject(final GameEngine engine, final String sprite, final float x, final float y, final float width, final float height){
        super(engine, sprite, x, y, width, height);
    }

    @Override
    public void onCollision(final GameObject that){
        GameObject.getOverlap(this, that, GameObject.overlap);
        mWorldLocation.offset(GameObject.overlap.x, GameObject.overlap.y); //move us out of the collision
        if(overlap.y != 0f){
            mTargetSpeed.y = 0f;
            mVelocity.y = 0f;
            if(overlap.y < 0f){ //feet
                mIsOnGround = true;
            }//else if(overlap.y > 0) { //head
        }
        updateBounds();
    }

    @Override
    public void update(final float dt){ //deltatime in seconds
        mVelocity.x += (mAcceleration.x * mTargetSpeed.x);
        if(Math.abs(mVelocity.x) > Math.abs(mTargetSpeed.x)){
            mVelocity.x = mTargetSpeed.x; //instantaneous deceleration.
        }
        if(!mIsOnGround){
            mVelocity.y += mGravity * dt;
            mWorldLocation.y += Utils.clamp(mVelocity.y*dt, -MAX_DELTA, MAX_DELTA);
        }
        mWorldLocation.x += Utils.clamp(mVelocity.x*dt, -MAX_DELTA, MAX_DELTA);

        if(mWorldLocation.y > mEngine.getWorldHeight()){
            final float margin = 2f;
            setPosition(Random.between(margin, mEngine.getWorldWidth()-margin), 0f);
        }
        updateBounds();
        mIsOnGround = false; //reset ground flag every frame
    }

    @Override
    public void destroy(){
        super.destroy();
        mVelocity = null;
        mTargetSpeed = null;
        mAcceleration = null;
    }
}
