package com.ulfben.PlatformerMK3.gameobjects;

import com.ulfben.PlatformerMK3.levels.LevelData;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class GameObjectFactory {
    private static final String TAG = "GameObjectFactory";

    private GameObjectFactory() {
        super();
    }

    public static GameObject makeObject(final String sprite, final float x, final float y){
        final GameObject o = makeObject(sprite);
        if(o != null) {
            float offset = 0f;
            if(o.width() < 1f){
                offset = 0.5f - (o.width()*0.5f); //center small objects on their tile
            }
            o.setPosition(x+offset, y);
        }
        return o;
    }

    private static GameObject makeObject(final String sprite){
        GameObject o;
        if(LevelData.PLAYER.equalsIgnoreCase(sprite)) {
            o = new Player();
        }else if(LevelData.SPEARS.equalsIgnoreCase(sprite)) {
            o = new Spears();
        }else if(LevelData.COIN.equalsIgnoreCase(sprite)) {
            o = new Collectible(sprite);
        }else if(LevelData.WALKER.equalsIgnoreCase(sprite)) {
            o = new Walker(sprite); //trivial "AI"
        }else{
            o = new GameObject(sprite);
        }
        return o;
    }


}
