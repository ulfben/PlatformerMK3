package com.ulfben.PlatformerMK3.gameobjects;
import com.ulfben.PlatformerMK3.GameEvent;
import com.ulfben.PlatformerMK3.engine.GameEngine;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class Collectible extends DynamicGameObject {
    private static final String TAG = "Collectible";
    private static final float DEFAULT_WIDTH_HEIGHT = 0.3f;

    public Collectible(final GameEngine engine, final String sprite) {
        super(engine, sprite, DEFAULT_LOCATION, DEFAULT_LOCATION, DEFAULT_WIDTH_HEIGHT, DEFAULT_WIDTH_HEIGHT);
    }

    public Collectible(final GameEngine engine, final String sprite, final float x, final float y, final float width, final float height){
        super(engine, sprite, x, y, width, height);
    }

    @Override
    public void onCollision(final GameObject that){
        super.onCollision(that);
        if(Player.class.isInstance(that)){
            mEngine.onGameEvent(GameEvent.CoinPickup);
            mEngine.removeGameObject(this);
        }
    }
}
