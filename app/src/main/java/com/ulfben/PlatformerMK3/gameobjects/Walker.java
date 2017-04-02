package com.ulfben.PlatformerMK3.gameobjects;

import com.ulfben.PlatformerMK3.engine.GameEngine;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-05.

public class Walker extends DynamicGameObject {
    private static final float TARGET_SPEED = 3.0f;
    private static final float DEFAULT_HEIGHT = 0.40f;
    private float mDirection = 1f;

    public Walker(final GameEngine engine, final String sprite){
        super(engine, sprite, DEFAULT_LOCATION, DEFAULT_LOCATION, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        init();
    }
    public Walker(final GameEngine engine, final String sprite, final float x, final float y) {
        super(engine, sprite, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        init();
    }
    public Walker(final GameEngine engine, final String sprite, final float x, final float y, final float width, final float height){
        super(engine, sprite, x, y, width, height);
        init();
    }

    private void init(){
        mTargetSpeed.x = (mEngine.coinFlip()) ? TARGET_SPEED : -TARGET_SPEED;
        mTargetSpeed.y = 0.0f;
        mGravitationalAccel = 0.0f; //no gravity for this object
        updateBounds();
    }

    @Override
    public void update(final float dt){
        mTargetSpeed.x = (TARGET_SPEED*mDirection);
        super.update(dt);
    }

    @Override
    public void onCollision(final GameObject that){
        GameObject.getOverlap(this, that, GameObject.overlap);
        mDirection *= -1f; //invert direction when colliding on x
        synchronized (mWorldLocation) {
            mWorldLocation.offset(GameObject.overlap.x, 0f); //move us out of collisions on X axis
        }
        updateBounds();
    }
}

