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
    public ArrayList<GameObject> mGameObjects = new ArrayList<GameObject>();
    private ArrayList<GameObject> mObjectsToAdd = new ArrayList<GameObject>();
    private ArrayList<GameObject> mObjectsToRemove = new ArrayList<GameObject>();

    private LevelData mData = null;
    public Player mPlayer = null;
    private GameEngine mEngine = null;

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

    public void update(final float dt) {
        final int numObjects = mGameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            mGameObjects.get(i).update(dt);
        }
    }

    public void addAndRemoveObjects(){
        GameObject temp;
        while (!mObjectsToRemove.isEmpty()) {
            temp = mObjectsToRemove.remove(0);
            mGameObjects.remove(temp);
        }
        while (!mObjectsToAdd.isEmpty()) {
            temp = mObjectsToAdd.remove(0);
            mGameObjects.add(temp);
        }
    }

    public void addGameObject(final GameObject object) {
        if(object != null) { mObjectsToAdd.add(object); }
    }
    public void removeGameObject(final GameObject object) {
        if(object != null) { mObjectsToRemove.add(object); }
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
        //TODO: text output is a GUI task, not a game object task. Refactor!
        //mGameObjects.add(new DebugTextGameObject(mEngine, LevelData.NULLSPRITE));
    }

    public void cleanup(){
        for (final GameObject go : mGameObjects){
            go.destroy();
        }
        if(mPlayer != null){ mPlayer.destroy(); mPlayer = null;}
        mGameObjects = new ArrayList<>();
        BitmapPool.empty();
    }

    public void destroy(){
        cleanup();
        mObjectsToAdd.clear();
        mObjectsToRemove.clear();
        mGameObjects.clear();
        mGameObjects = null;
        mEngine = null;
        mData = null;
        mPlayer = null;
    }
}
