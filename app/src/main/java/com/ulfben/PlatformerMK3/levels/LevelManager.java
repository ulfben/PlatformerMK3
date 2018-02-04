package com.ulfben.PlatformerMK3.levels;
import android.util.Log;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.gameobjects.GameObjectFactory;
import com.ulfben.PlatformerMK3.gameobjects.Player;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;

import java.util.ArrayList;

//Created by Ulf Benjaminsson (ulfben) on 2017-02-13.
//the LevelManager parses LevelData and uses a GameObjectFactory
//to create all necessary GameObjects and position them in the world.
//After construction it holds a list of all GameObjects (including the player-object)

public class LevelManager {
    private static final String TAG = "LevelManager";

    public ArrayList<GameObject> mGameObjects = new ArrayList<>();
    public Player mPlayer = null;

    private LevelData mData = null;
    private ArrayList<GameObject> mObjectsToAdd = new ArrayList<>();
    private ArrayList<GameObject> mObjectsToRemove = new ArrayList<>();

    public LevelManager(final String levelName){
        super();
        switch(levelName){
            default:
                mData = new TestLevel();
                break;
        }
        loadMapAssets(mData);
    }

    public void update(final float dt) {
        final int numObjects = mGameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            mGameObjects.get(i).update(dt);
        }
    }

    public void addAndRemoveObjects(){
        GameObject temp;
        try {
            for (int i = mObjectsToRemove.size() - 1; i >= 0; i--) {
                temp = mObjectsToRemove.remove(i); //remove from the back
                mGameObjects.remove(temp); //look up and remove.
            }
            for (int i = mObjectsToAdd.size() - 1; i >= 0; i--) {
                temp = mObjectsToAdd.remove(i);
                mGameObjects.add(temp);
            }
        }catch(Exception e){ //this should never happen.
            Log.e(TAG, "addAndRemoveObjects is misbehaving.");
            mObjectsToRemove = new ArrayList<>(); //empty and recreate
            mObjectsToAdd = new ArrayList<>(); //for good measure.
        }
    }

    public void addGameObject(final GameObject object) {
        if(object != null) { mObjectsToAdd.add(object); }
    }
    public void removeGameObject(final GameObject object) {
        if(object != null) { mObjectsToRemove.add(object); }
    }

    public float getWorldWidth(){ return mData.mWidth; }
    public float getWorldHeight(){ return mData.mHeight; }

    private void loadMapAssets(final LevelData data){
        cleanup();
        for(int y = 0; y < data.mHeight; y++){
            final int[] row = data.getRow(y);
            for(int x = 0; x < row.length; x++) {
                int tileType = row[x];
                if(tileType == LevelData.NO_TILE){ continue; }  //ignoring "background tiles"
                addGameObject(GameObjectFactory.makeObject(mData.getSpriteName(tileType), x, y)); //adds to temporary list, filters out any null-values (which should never happen)
            }
        }
        addAndRemoveObjects(); //commit the temporary list to our "live" list
        mPlayer = findPlayerInstance();
    }

    private Player findPlayerInstance(){
        for (final GameObject go : mGameObjects){
            if(Player.class.isInstance(go)){
                return (Player) go;
            }
        }
        throw new AssertionError("No player found in the level data!");
    }

    private void cleanup(){
        for (final GameObject go : mGameObjects){
            go.destroy();
        }
        mPlayer = null;
        mGameObjects = new ArrayList<>();
        BitmapPool.empty();
    }

    public void destroy(){
        cleanup();
        mObjectsToAdd.clear();
        mObjectsToRemove.clear();
        mGameObjects.clear();
        mData.unload();
        mData = null;
        mPlayer = null;
    }
}
