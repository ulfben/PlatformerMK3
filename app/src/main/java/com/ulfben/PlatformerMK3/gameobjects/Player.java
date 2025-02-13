package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.ulfben.PlatformerMK3.Animation;
import com.ulfben.PlatformerMK3.GameEvent;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.input.GameInput;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

public class Player extends DynamicGameObject {
    private static final String TAG = "Player";
    private static final float PLAYER_HEIGHT = 0f; //calculated from sprite
    private static final float PLAYER_WIDTH = DEFAULT_WIDTH*0.9f; //slighly less than normal tiles.
    private static final float PLAYER_RUN_SPEED = 6.0f; //meters per second
    private static final float PLAYER_ACCELERATION_X = 1.0f; //add % of targetspeed per frame 0.75
    private static final float PLAYER_ACCELERATION_Y = 1.0f; //add % of targetspeed per frame 0.25
    private static final float JUMP_FORCE = -(GRAVITATIONAL_ACCELERATION/2f);
    private static final float MIN_ANIMATION_RATE = 0.4f; //the slowest speed we'll play the animation at
    private static final float MIN_TURN_DELAY = 0.11f; //don't allow flipping the sprite left/right more often than this (gyro filter)
    private static final float MIN_INPUT_TO_TURN = 0.05f; //5% input, or we don't bother flipping the sprite
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private volatile int mFacing = LEFT;

    private float mDirectionChangeCooldown = 0.0f;
    private Animation mAnim = null;

    Player() {
        super("", PLAYER_WIDTH, PLAYER_HEIGHT);
        mAcceleration.x = PLAYER_ACCELERATION_X;
        mAcceleration.y = PLAYER_ACCELERATION_Y;
        mAnim = new Animation(mEngine, R.drawable.player_anim, width, height);
        refreshSprite();
    }

    @Override
    public void resampleSprite(){
        if(mAnim != null){
            mAnim.resampleSprites();
        }
        refreshSprite();
    }

    private void refreshSprite(){
        mBitmap = mAnim.getCurrentBitmap();
        height = mAnim.getCurrentHeightMeters(); //scaled
        width = mAnim.getCurrentWidthMeters();
    }

    @Override
    public void render(final Canvas canvas, final Matrix transform, final Paint paint){
        transform.preScale(mFacing, 1.0f);
        if(mFacing == RIGHT){
            float offset = width *mEngine.getPixelsPerMeterX();
            transform.postTranslate(offset, 0);
        }
        canvas.drawBitmap(mBitmap, transform, paint);
    }

    @Override
    public void onCollision(final GameObject that){
        super.onCollision(that);
        if(Spears.class.isInstance(that)){
            mEngine.onGameEvent(GameEvent.PlayerSpikeCollision, this);
        }
    }

    @Override
    public void update(final float dt){
        final GameInput controls = mEngine.getControls();
        final float direction = controls.mHorizontalFactor;
        mTargetSpeed.x = direction * (PLAYER_RUN_SPEED);
        updateFacingDirection(direction, dt);
        updateAnimationRate();
        if(controls.mJump && mIsOnGround){
            mVelocity.y = JUMP_FORCE;
            mIsOnGround = false;
            mEngine.onGameEvent(GameEvent.PlayerJump, this);
        }
        mAnim.update(dt);
        refreshSprite();
        super.update(dt);
    }



    private void updateFacingDirection(final float controlDirection, final float dt){
        mDirectionChangeCooldown -= dt;
        if(mDirectionChangeCooldown < 0 &&  Math.abs(controlDirection) > MIN_INPUT_TO_TURN){
            mFacing = (controlDirection < 0) ? LEFT : (controlDirection > 0) ? RIGHT : mFacing;
            mDirectionChangeCooldown = MIN_TURN_DELAY;
        }
    }

    private void updateAnimationRate(){
        float rate = Math.abs(mTargetSpeed.x) / (PLAYER_RUN_SPEED*0.5f); //0-2
        if(rate < 0.1f){
            rate = 0f;
        }else if(rate < MIN_ANIMATION_RATE){
            rate = MIN_ANIMATION_RATE;
        }
        mAnim.setPlaybackRate(rate);
    }

    @Override
    public void destroy(){
        if(mAnim != null) {
            mAnim.destroy();
            mAnim = null;
        }
        mBitmap = null; //cleaned out by the animation
        super.destroy();
    }
}
