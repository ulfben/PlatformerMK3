package com.ulfben.PlatformerMK3.gameobjects;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.utilities.Random;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-05.

public class Walker extends DynamicGameObject {
    private static final float TARGET_SPEED = 3.0f;
    private static final float WALKER_HEIGHT = 0.40f;
    private float mDirection = 1f;

    public Walker(final String sprite){
        super(sprite, DEFAULT_WIDTH, WALKER_HEIGHT);
        mTargetSpeed.x = (Random.coinFlip()) ? TARGET_SPEED : -TARGET_SPEED;
        mTargetSpeed.y = 0.0f;
        mGravity = 0.0f; //no gravity for this object
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
        x += GameObject.overlap.x; //move us out of collisions on X axis
    }
}

