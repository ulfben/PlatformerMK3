package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Bitmap;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.levels.LevelData;
import com.ulfben.PlatformerMK3.utilities.Axis;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
import com.ulfben.PlatformerMK3.utilities.BitmapUtils;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class GameObjectFactory {
    private static final String TAG = "GameObjectFactory";

    private GameObjectFactory() {
        super();
    }

    public static GameObject makeObject(final GameEngine engine, final String sprite, final float x, final float y){
        final GameObject o = makeObject(engine, sprite);
        if(o != null) {
            float offset = 0f;
            if(o.width() < 1f){
                offset = 0.5f - (o.width()*0.5f); //center small objects on their tile
            }
            o.setPosition(x+offset, y);
        }
        return o;
    }

    public static GameObject makeObject(final GameEngine engine, final String sprite){
        GameObject o = null;
        if(LevelData.PLAYER.equalsIgnoreCase(sprite)) {
            o = new Player(engine);
        }else if(LevelData.SPEARS.equalsIgnoreCase(sprite)) {
            o = new Spears(engine);
        }else if(LevelData.COIN.equalsIgnoreCase(sprite)) {
            o = new Collectible(engine, sprite);
        }else if(LevelData.WALKER.equalsIgnoreCase(sprite)) {
            o = new Walker(engine, sprite); //trivial "AI"
        }else{
            o = new GameObject(engine, sprite);
        }
        return o;
    }


}
