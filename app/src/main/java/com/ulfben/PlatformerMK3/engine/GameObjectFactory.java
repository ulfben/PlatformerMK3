package com.ulfben.PlatformerMK3.engine;
import com.ulfben.PlatformerMK3.LevelData;
import com.ulfben.PlatformerMK3.gameobjects.Collectible;
import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.gameobjects.Player;
import com.ulfben.PlatformerMK3.gameobjects.Spears;
import com.ulfben.PlatformerMK3.gameobjects.Walker;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class GameObjectFactory {
    private static final String TAG = "GameObjectFactory";

    private GameObjectFactory() {
        super();
    }

    //TODO: figure out a solution that let's me keep the factory, but add object-configuration.
    public static GameObject makeObject(final GameEngine engine, final String sprite, final float x, final float y){
        final GameObject o = makeObject(engine, sprite);
        if(o != null) {
            float offset = 0f;
            if(o.width() < 1f){
                offset = 0.5f - (o.width()*0.5f); //center small objects
            }
           o.setPosition(x+offset, y);
        }
        return o;
    }

    public static GameObject makeObject(final GameEngine engine, final String sprite){
        GameObject o = null;
        if(LevelData.PLAYER.equalsIgnoreCase(sprite)) {
            o = new Player(engine, sprite);
        }else if(LevelData.SPEARS.equalsIgnoreCase(sprite)) {
            o = new Spears(engine, sprite);
        }else if(LevelData.COIN.equalsIgnoreCase(sprite)) {
            o = new Collectible(engine, sprite);
        }else if(LevelData.WALKER.equalsIgnoreCase(sprite)) {
            o = new Walker(engine, sprite); //trivial "AI"
        }else{
            o = new GameObject(engine, sprite);
        }
        o.postConstruct(); //Q&D HACK
        return o;
    }
}
