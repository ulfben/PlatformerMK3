package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.ulfben.PlatformerMK3.Animation;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.GameEngine;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

public class Player extends DynamicGameObject {
    private static final String TAG = "Player";
    private static final float PLAYER_HEIGHT = 0f; //meters
    private static final float PLAYER_WIDTH = 0.90f;
    private static final float PLAYER_RUN_SPEED = 6.0f; //meters per second
    private static final float PLAYER_FRICTION = 0.95f;
    private static final float PLAYER_ACCELERATION_X = 1f; //add % of targetspeed per frame 0.75
    private static final float PLAYER_ACCELERATION_Y = 1.0f; //add % of targetspeed per frame 0.25
    private static final float PLAYER_JUMP_DURATION = 0.120f; //in x seconds 0.180f
    private static final float PLAYER_JUMP_IMPULSE = -(GRAVITATIONAL_ACCELERATION*4f);
    private static final float MIN_ANIMATION_RATE = 0.2f; //stop animating when moving slower than this
    private static final float MIN_TURN_DELAY = 0.11f; //don't allow flipping the sprite left/right more often than this (gyro filter)
    private static final float MIN_INPUT_TO_TURN = 0.05f; //20% input, or we don't bother flipping the sprite
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private volatile int mFacing = LEFT;

    private float mTurnCooldown = 0.0f;
    private float mJumpTime = 0.0f;
    private Animation mAnim = null;

    public Player(final GameEngine engine, final String sprite) {
        super(engine, sprite, DEFAULT_LOCATION, DEFAULT_LOCATION, PLAYER_WIDTH, PLAYER_HEIGHT);
        init();
    }

    public Player(final GameEngine engine, final String sprite, final float x, final float y) {
        super(engine, sprite, x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
        init();
    }

    private void init(){
        mAcceleration.x = PLAYER_ACCELERATION_X;
        mAcceleration.y = PLAYER_ACCELERATION_Y;
        mFriction = PLAYER_FRICTION;
        mAnim = new Animation(mEngine, R.drawable.player_anim, mWidth, mHeight);
        updateBounds();
    }

    @Override
    public void postConstruct(){
        //no-op atm. using animations.
    }

    @Override
    public void onCollision(final GameObject that){
        if(!GameObject.getOverlap(this, that, GameObject.overlap)){
            Log.d(TAG, "getOverlap false negative. Always check AABB first!");
        }
        if(overlap.y != 0f){
            mJumpTime = PLAYER_JUMP_DURATION;
            mTargetSpeed.y = 0f;
            mVelocity.y = 0f;
            mJumping = false;
            if(overlap.y < 0f){ //feet
                isOnGround = true;
                mJumpTime = 0.0f; //back on ground, reset jumpTime
                if(Spears.class.isInstance(that)){
                    //bounce!
                }
            }//else if(overlap.y > 0){ //head
        }
        mWorldLocation.offset(GameObject.overlap.x, GameObject.overlap.y);
        updateBounds();
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
    public void render(final Canvas canvas, final Paint paint){
        mTransform.reset();
        mTransform.setScale(mFacing, 1.0f);
        mEngine.worldToScreen(mBounds, GameObject.screenCord);
        float offset = 0;
        if(mFacing == RIGHT){
            offset = mWidth*mEngine.getPixelsPerMeter();
        }
        mTransform.postTranslate(GameObject.screenCord.x+offset, GameObject.screenCord.y);
        canvas.drawBitmap(mAnim.getCurrentBitmap(), mTransform, paint);
    }

    @Override
    public void update(final float dt){
        final float direction = mEngine.mControl.mHorizontalFactor;
        mTargetSpeed.x = direction * (PLAYER_RUN_SPEED);
        updateFacingDirection(direction, dt);
        updateAnimationRate();
        if(isJumping()){
            mTargetSpeed.y = PLAYER_JUMP_IMPULSE;
            mJumpTime += dt;
            mJumping = true;
        }else{
            mTargetSpeed.y = 0; //gravity gets added in super-class
        }
        mAnim.update(dt);
        super.update(dt);
    }

    private void updateFacingDirection(final float controlDirection, final float dt){
        mTurnCooldown-=dt;
        if(mTurnCooldown < 0 &&  Math.abs(controlDirection) > MIN_INPUT_TO_TURN){
            mFacing = (controlDirection < 0) ? LEFT : (controlDirection > 0) ? RIGHT : mFacing;
            mTurnCooldown = MIN_TURN_DELAY;
        }
    }

    private void updateAnimationRate(){
        float rate = 0.0f;
        if(isOnGround()) {
            rate = Math.abs(mTargetSpeed.x) / PLAYER_RUN_SPEED;
            if(rate > 0f && rate < MIN_ANIMATION_RATE){
                rate = MIN_ANIMATION_RATE;
            }
        }
        mAnim.setPlaybackRate(rate);
    }

    private boolean mJumping = false;
    private boolean isOnGround = false;
    private boolean didJustJump(){
        return (!mJumping) && mEngine.mControl.mJump && isOnGround && canJump();
    }

    private boolean isJumping(){
        return mEngine.mControl.mJump && canJump();
    }

    private boolean canJump(){
        //note, doesn't care about ground. Can jump in mid-air for JUMP_DURATION
        //simply because single-frame impulses were tricky to get right. :P
        return mJumpTime < PLAYER_JUMP_DURATION;
    }

    private boolean isOnGround(){
        return mJumpTime == 0.0f;
    }

    @Override
    public void destroy(){
        super.destroy();
        mAnim.destroy();
        mAnim = null;
    }
}
