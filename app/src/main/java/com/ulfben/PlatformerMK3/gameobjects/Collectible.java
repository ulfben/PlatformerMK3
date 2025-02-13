package com.ulfben.PlatformerMK3.gameobjects;
import com.ulfben.PlatformerMK3.GameEvent;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class Collectible extends DynamicGameObject {
    private static final String TAG = "Collectible";
    private static final float DEFAULT_WIDTH_HEIGHT = 0.3f;

    public Collectible(final String sprite) {
        super(sprite, DEFAULT_WIDTH_HEIGHT, DEFAULT_WIDTH_HEIGHT);
    }

    @Override
    public void onCollision(final GameObject that){
        super.onCollision(that);
        if(Player.class.isInstance(that)){
            mEngine.onGameEvent(GameEvent.PlayerCoinPickup, this);
        }
    }
}
