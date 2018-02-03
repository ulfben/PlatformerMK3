package com.ulfben.PlatformerMK3.engine;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.ulfben.PlatformerMK3.GameEvent;
import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.gameobjects.Player;
import com.ulfben.PlatformerMK3.input.GameInput;
import com.ulfben.PlatformerMK3.levels.LevelManager;
import com.ulfben.PlatformerMK3.utilities.Axis;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
import com.ulfben.PlatformerMK3.utilities.BitmapUtils;

import java.util.ArrayList;
import java.util.Locale;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class GameEngine {
    private static final String TAG = "GameEngine";
    private static final float SCALE_FACTOR = 0.5f; //< 1.0f for fullscreen scaling. 0.5 = use half the screens' resolution.
    private static final float METERS_TO_SHOW_X = 0f; //set the value you want fixed
    private static final float METERS_TO_SHOW_Y = 9f;  //the other is calculated at runtime!

    private UpdateThread mUpdateThread = null;
    public Activity mActivity = null;

    private final GameView mGameView;
    public LevelManager mLevelManager = null; //TODO: maybe move entity management out of the levelman
    public Viewport mCamera = null;
    public GameInput mControl = null;
    private ArrayList<GameObject> mVisibleObjects = new ArrayList<>();
    private final Jukebox mJukebox;

    public GameEngine(final Activity a, final GameView gameView) {
        super();
        mActivity = a;
        mGameView = gameView;
        //TODO: remove viewPort from gameView!
        mCamera = mGameView.createViewport(0f, 0f, METERS_TO_SHOW_X, METERS_TO_SHOW_Y, SCALE_FACTOR);
        mJukebox = new Jukebox(a);
        mControl = new GameInput(); //placeholder inputs
        BitmapPool.init();
        BitmapUtils.init(getResources());
    }


    public int getResourceID(final String filename){
        return getResourceID(filename, "drawable");
    }
    public int getResourceID(final String filename, final String type){
        return getResources().getIdentifier(filename, type, getPackageName());
    }
    public String getPackageName(){ return mActivity.getPackageName(); }
    public Resources getResources(){ return mActivity.getResources(); }
    public Context getContext(){ return mActivity; }
    public int getPixelsPerMeterY(){
        return mCamera.getPixelsPerMeterY();
    }
    public int getPixelsPerMeterX(){
        return mCamera.getPixelsPerMeterX();
    }
    public float getWorldWidth(){
        return mLevelManager.getLevelWidth();
    }
    public float getWorldHeight(){
        return mLevelManager.getLevelHeight();
    }
    public int getResolutionY(){ return mCamera.getScreenHeight(); }
    public int getResolutionX(){ return mCamera.getScreenWidth(); }

    public float screenToWorld(final float pixelDistance, final Axis axis){
        if(axis == Axis.X) {
            return pixelDistance / mCamera.getPixelsPerMeterX();
        }
        return pixelDistance / mCamera.getPixelsPerMeterY();
    }
    public float worldToScreen(final float worldDistance, final Axis axis){
        if(axis == Axis.X) {
            return worldDistance * mCamera.getPixelsPerMeterX();
        }
        return worldDistance * mCamera.getPixelsPerMeterY();
    }
    public void worldToScreen(final PointF worldLocation, final Point screenCord){
        mCamera.worldToScreen(worldLocation, screenCord);
    }
    public void worldToScreen(final RectF worldLocation, final Point screenCord){
        mCamera.worldToScreen(worldLocation, screenCord);
    }
    public void onGameEvent(final GameEvent e, final GameObject source){
        mJukebox.playSoundForGameEvent(e);
        if(e == GameEvent.PlayerCoinPickup){
            mLevelManager.removeGameObject(source);
        }
    }

    public void loadLevel(final String levelName){
        if(mLevelManager != null){
            mLevelManager.destroy(); //release loaded assets
        }
        mLevelManager = new LevelManager(this, levelName);
        mCamera.lookAt(mLevelManager.mPlayer);
        mCamera.follow(mLevelManager.mPlayer);
        mCamera.setBounds(getWorldWidth(), getWorldHeight());
    }

    public Jukebox getJukebox(){
        return mJukebox;
    }

    public void setGameInput(final GameInput controller) {
        mControl = controller;
    }

    public void startGame() {
        stopGame(); // Stop a game if it is running
        if (mControl != null) {
            mControl.onStart();
        }
        mUpdateThread = new UpdateThread(this);
        mUpdateThread.start();
        mJukebox.resumeBgMusic();
    }

    public void stopGame() {
        if (mControl != null) {
            mControl.onStop();
        }
        if (mUpdateThread != null) {
            mUpdateThread.stopThread();
        }
        //TODO maybe stop jukebox?

    }

    public void pauseGame() {
        if (mUpdateThread != null) {
            mUpdateThread.pauseThread();
        }
        if (mControl != null) {
            mControl.onPause();
        }
        if(mJukebox != null){
            mJukebox.pauseBgMusic();
        }
    }

    public void resumeGame() {
        if(mJukebox != null){
            mJukebox.resumeBgMusic();
        }
        if (mControl != null) {
            mControl.onResume();
        }
        if (mUpdateThread != null) {
            mUpdateThread.resumeThread();
        }
    }

    public void onDestroy(){
        if(mJukebox != null){
            mJukebox.destroy();
        }
        //if(mCamera != null){mCamera.destroy();}
        if(mLevelManager != null) { mLevelManager.destroy(); mLevelManager = null;}
        if(mUpdateThread != null){ mUpdateThread.stopThread(); mUpdateThread = null; }
        if(mControl != null){ mControl.onDestroy(); mControl = null; }
        if(mGameView != null){ mGameView.destroy(); }
        mActivity = null;
    }



    //called from UpdateThread
    public void onUpdate(final float dt) {
        mControl.update(dt);
        mCamera.update(dt);
        mLevelManager.update(dt);
        checkCollisions(mLevelManager.mGameObjects);
        mLevelManager.addAndRemoveObjects();
        buildVisibleSet(mLevelManager.mGameObjects, mCamera);
        render(mVisibleObjects, mCamera); //will wait on UI thread on post
    }

    private void buildVisibleSet(final ArrayList<GameObject> gameObjects, final Viewport camera){
        GameObject temp;
        mVisibleObjects.clear();
        final int numObjects = gameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            temp =  gameObjects.get(i);
            if (camera.inView(temp.mBounds)) {
                mVisibleObjects.add(temp);
            }
        }
    }

    public void render(final ArrayList<GameObject> visibleObjects, final Viewport camera) {
        mGameView.render(visibleObjects);
    }

    //checkCollisions will test all game entities against eachother.
    //Note the offsets in these loops: [0]-[size-1] and [i+1]-[size]
    //This ensure we never redundantly test a pair.
    //For details, refer to my slides (10-17): https://goo.gl/po4YkK
    private void checkCollisions(final ArrayList<GameObject> gameObjects) {
        final int count = gameObjects.size();
        GameObject a, b;
        for(int i = 0; i < count-1; i++){
            a = gameObjects.get(i);
            for(int j = i+1; j < count; j++){
                b = gameObjects.get(j);
                if(a.isColliding(b)){
                    a.onCollision(b);
                    b.onCollision(a);
                }
            }
        }
    }

    public boolean isRunning() {
        return mUpdateThread != null && mUpdateThread.isGameRunning();
    }

    public boolean isPaused() {
        return mUpdateThread != null && mUpdateThread.isGamePaused();
    }

    //TODO: probably move this out of the GameEngine.
    final String[] DBG_STRINGS = new String[4];
    final static String DBG_UPS = "Ticks/s: %d";
    final static String DBG_OBJ_RENDER_COUNT =  "Objects rendered: %d / %d";
    final static String DBG_PLAYER_INFO =  "Player: [%.2f, %.2f]";
    final static Locale LOCALE = Locale.getDefault();
    public String[] getDebugStrings(){
        DBG_STRINGS[0] = mCamera.toString();
        DBG_STRINGS[1] = String.format(LOCALE, DBG_OBJ_RENDER_COUNT, mVisibleObjects.size(),  mLevelManager.mGameObjects.size());
        DBG_STRINGS[2] = String.format(LOCALE, DBG_PLAYER_INFO,  mLevelManager.mPlayer.x(),  mLevelManager.mPlayer.y());
        DBG_STRINGS[3] = String.format(LOCALE, DBG_UPS, mUpdateThread.getAverageFPS());
        return DBG_STRINGS;
    }
}
