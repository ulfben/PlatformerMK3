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
    private ArrayList<GameObject> mObjectsToAdd = new ArrayList<>();
    private ArrayList<GameObject> mObjectsToRemove = new ArrayList<>();
    public Player mPlayer = null;
    private float mLevelWidth = 0f; //meters
    private float mLevelHeight = 0f;

    public LevelManager(final String levelName){
        super();
        loadMapAssets(new TestLevel());
    }

    public void update(final float dt) {
        final int numObjects = mGameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            mGameObjects.get(i).update(dt);
        }
        checkCollisions(mGameObjects);
        addAndRemoveObjects();
    }

    //checkCollisions will test all game entities against each other.
    //Note the offsets in these loops: [0]-[size-1] and [i+1]-[size]
    //This ensure we never redundantly test a pair.
    //For details, refer to my slides (10-17): https://goo.gl/po4YkK
    private static void checkCollisions(final ArrayList<GameObject> gameObjects) {
        try { // Belts *and* Suspenders!
            final int count = gameObjects.size();
            GameObject a, b;
            for (int i = 0; i < count - 1; i++) {
                a = gameObjects.get(i);
                for (int j = i + 1; j < count; j++) {
                    b = gameObjects.get(j);
                    if (a.isColliding(b)) {
                        a.onCollision(b);
                        b.onCollision(a);
                    }
                }
            }
        } catch(NullPointerException npe){
            Log.e(TAG, "NPE in checkCollisions " + npe.toString());
        }catch(IndexOutOfBoundsException oob){
            Log.e(TAG, "Out of Bounds in checkCollisions " + oob.toString());
        }catch(Exception e){
            Log.e(TAG, "Exception in checkCollisions " + e.toString());
        }
        //A user reported NPE and OOB exceptions from checkCollisions under some circumstances.
        //I was unable to recreate these crashes! After simplifying and cleaning up all the
        //the suspect code-paths, I also added this try/catch around the collision testing to avoid
        //uncaught exceptions bubbling out.
        //The exceptions simply should not happen, but if they do, we log and continue running instead of crashing.
    }

    private void addAndRemoveObjects(){
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

    public float getWorldWidth(){ return mLevelWidth; }
    public float getWorldHeight(){ return mLevelHeight; }

    private void loadMapAssets(final LevelData data){
        cleanup();
        mLevelHeight = data.mHeight;
        mLevelWidth = data.mWidth;
        for(int y = 0; y < mLevelHeight; y++){
            final int[] row = data.getRow(y);
            for(int x = 0; x < row.length; x++) {
                int tileType = row[x];
                if(tileType == LevelData.NO_TILE){ continue; }  //ignoring "background tiles"
                addGameObject(GameObjectFactory.makeObject(data.getSpriteName(tileType), x, y)); //adds to temporary list, filters out any null-values (which should never happen)
            }
        }
        addAndRemoveObjects(); //commit the temporary list to our "live" list
        mPlayer = findPlayerInstance();
    }

    //necessary if the gamecamera or surfaceview changes (= updates the number of pixels-per-meter)
    public void reloadBitmaps(){
        for (final GameObject go : mGameObjects){
            go.resampleSprite();
        }
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
        mGameObjects.clear();
        BitmapPool.empty();
    }

    public void destroy(){
        cleanup();
        mObjectsToAdd.clear();
        mObjectsToRemove.clear();
        mGameObjects.clear();
        mPlayer = null;
    }
}
