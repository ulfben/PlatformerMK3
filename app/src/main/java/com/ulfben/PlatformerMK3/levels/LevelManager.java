package com.ulfben.PlatformerMK3.levels;
import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.gameobjects.DebugTextGameObject;
import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.gameobjects.GameObjectFactory;
import com.ulfben.PlatformerMK3.gameobjects.Player;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;

import java.util.ArrayList;

//Created by Ulf Benjaminsson (ulfben) on 2017-02-13.
public class LevelManager {
    private static final String TAG = "LevelManager";
    public ArrayList<GameObject> mGameObjects = new ArrayList<>();
    public LevelData mData = null;
    public Player mPlayer = null;
    public GameEngine mEngine = null;

    public LevelManager(final GameEngine engine, final String levelName){
        super();
        mEngine = engine;
        switch(levelName){
            default:
                mData = new TestLevel();
                break;
        }
        loadMapAssets(mData);// Load all the GameObjects and Bitmaps
    }

    public float getLevelWidth(){ return mData.mWidth; }
    public float getLevelHeight(){ return mData.mHeight; }

    private void loadMapAssets(final LevelData data){
        cleanup();
        int tileType;
        GameObject temp;
        String sprite = LevelData.NULLSPRITE;
        for(int y = 0; y < data.mHeight; y++){
            final int[] row = data.getRow(y);
            for(int x = 0; x < row.length; x++) {
                tileType = row[x];
                if(tileType == LevelData.NO_TILE){ continue; }  //ignoring "background tiles"
                sprite = mData.getSpriteName(tileType);
                temp = GameObjectFactory.makeObject(mEngine, sprite, x, y);
                if(temp == null){ continue;}
                if(mPlayer == null && Player.class.isInstance(temp)){
                    mPlayer = (Player) temp;
                }
                mGameObjects.add(temp);
            }
        }
        mGameObjects.add(new DebugTextGameObject(mEngine, LevelData.NULLSPRITE));
    }

    public void cleanup(){
        for (final GameObject go : mGameObjects){
            go.destroy();
        }
        mGameObjects = new ArrayList<>();
        BitmapPool.empty();
    }

    public void destroy(){
        cleanup();
        mGameObjects = null;
        mEngine = null;
        mData = null;
    }
}
