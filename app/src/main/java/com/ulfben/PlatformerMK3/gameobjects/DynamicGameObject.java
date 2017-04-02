package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.PointF;

import com.ulfben.PlatformerMK3.Utils;
import com.ulfben.PlatformerMK3.engine.GameEngine;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-24.

public class DynamicGameObject extends GameObject {
    private static final String TAG = "DynamicGameObject";
    private static final float MAX_DELTA = 0.48f; //maximum change in position over a single frame. keep < smallest object to avoid tunneling (1.0f = 1m)
    protected static final float TERMINAL_VELOCITY = 16.0f;
    protected static final float GRAVITATIONAL_ACCELERATION = 0.5f;
    protected PointF mVelocity = new PointF(0.0f, 0.0f); //current velocity
    protected PointF mTargetSpeed = new PointF(0.0f, 0.0f); //target velocity
    protected PointF mAcceleration = new PointF(1.0f, 1.0f); //fake acceleration; how fast we approach targetspeed
    protected float mGravitationalAccel = GRAVITATIONAL_ACCELERATION; //non-static member, some objects will not want gravity
    protected float mFriction = 1.0f; //1 == no friction, 0 == no motion.

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
            //if(overlap.y < 0f){ //feet
            //}else if(overlap.y > 0){ //head
        }
        updateBounds();
    }

    @Override
    public void update(final float dt){ //deltatime in seconds
        mVelocity.x += (mAcceleration.x * mTargetSpeed.x);
        if(Math.abs(mVelocity.x) > Math.abs(mTargetSpeed.x)){
            mVelocity.x = mTargetSpeed.x; //instantaneous deceleration.
        }
        mVelocity.y += (mAcceleration.y * mTargetSpeed.y)+mGravitationalAccel;
        if(mVelocity.y > TERMINAL_VELOCITY){
            mVelocity.y = TERMINAL_VELOCITY;
        }
        mWorldLocation.x += Utils.clamp(mVelocity.x*dt, -MAX_DELTA, MAX_DELTA);
        mWorldLocation.y += Utils.clamp(mVelocity.y*dt, -MAX_DELTA, MAX_DELTA);
        if(mWorldLocation.y > mEngine.getWorldHeight()){
            final float newX = GameEngine.RNG.nextFloat()*mEngine.getWorldWidth();
            setPosition(newX, 0);
        }
        updateBounds();
    }

    @Override
    public void destroy(){
        super.destroy();
        mVelocity = null;
        mTargetSpeed = null;
        mAcceleration = null;
    }
}
