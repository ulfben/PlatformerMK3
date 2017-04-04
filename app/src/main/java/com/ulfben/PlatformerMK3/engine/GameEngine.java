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
import com.ulfben.PlatformerMK3.input.InputManager;
import com.ulfben.PlatformerMK3.input.NullInput;
import com.ulfben.PlatformerMK3.levels.LevelManager;
import com.ulfben.PlatformerMK3.utilities.Axis;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
import com.ulfben.PlatformerMK3.utilities.BitmapUtils;

import java.util.ArrayList;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class GameEngine {
    private static final String TAG = "GameEngine";
    private static final float SCALE_FACTOR = 0.5f; //< 1.0f for fullscreen scaling. 0.5 = use half resolution.
    private static final float METERS_TO_SHOW_X = 0f; //set the value you want fixed
    private static final float METERS_TO_SHOW_Y = 9f;  //the other is calculated at runtime!
    public  static boolean MULTITHREAD = false;
    private static final float THREADING_TOGGLE_TIMEOUT = 2f;
    private float mThreadingToggleTimer = 0f;

    private static final Object updateLock = new Object();
    private ArrayList<GameObject> mGameObjects = new ArrayList<GameObject>();
    private ArrayList<GameObject> mVisibleObjects = new ArrayList<>();
    private ArrayList<GameObject> mObjectsToAdd = new ArrayList<GameObject>();
    private ArrayList<GameObject> mObjectsToRemove = new ArrayList<GameObject>();
    private volatile int mVisibleObjectCount = 0;
    public Player mPlayer = null;
    public Viewport mCamera = null;
    private LevelManager mLevelManager = null;

    private UpdateThread mUpdateThread = null;
    private RenderThread mRenderThread = null;
    public Activity mActivity = null;
    public InputManager mControl = null;
    private final GameView mGameView;
    private final Jukebox mJukebox;

    public GameEngine(final Activity a, final GameView gameView) {
        super();
        mActivity = a;
        mGameView = gameView;
        mCamera = mGameView.createViewport(0f, 0f, METERS_TO_SHOW_X, METERS_TO_SHOW_Y, SCALE_FACTOR);
        mJukebox = new Jukebox(a);
        mControl = new NullInput();
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
    public void onGameEvent(final GameEvent e){
        mJukebox.playSoundForGameEvent(e);
    }

    public void loadLevel(final String levelName){
        if(mLevelManager != null){
            mLevelManager.destroy(); //release loaded assets
        }
        mLevelManager = new LevelManager(this, levelName);
        mGameObjects = mLevelManager.mGameObjects;
        mPlayer = mLevelManager.mPlayer;
        mCamera.lookAt(mPlayer.x(), mPlayer.y());
        mCamera.follow(mPlayer);
        mCamera.setBounds(getWorldWidth(), getWorldHeight());
        mGameView.setVisibleObjects(mVisibleObjects);
    }

    public Jukebox getJukebox(){
        return mJukebox;
    }

    public void setInputManager(final InputManager controller) {
        mControl = controller;
    }

    public void startGame() {
        stopGame(); // Stop a game if it is running
        if (mControl != null) {
            mControl.onStart();
        }
        mUpdateThread = new UpdateThread(this);
        mUpdateThread.start();

        //a thread object is relatively lightweight until asked to start, so I'm just going to always
        //create the thread, even if I don't use it. This is to keep me sane while developing and
        //flipping MULTITHREAD back and forth :P
        mRenderThread = new RenderThread(this);
        if(MULTITHREAD) {
            mRenderThread.start();
        }
        mJukebox.resumeBgMusic();
    }

    public void stopGame() {
        if (mUpdateThread != null) {
            mUpdateThread.stopThread();
        }
        if (mRenderThread != null && MULTITHREAD) {
            mRenderThread.stopThread();
        }
        if (mControl != null) {
            mControl.onStop();
        }
    }

    public void pauseGame() {
        if (mUpdateThread != null) {
            mUpdateThread.pauseThread();
        }
        if (mRenderThread != null && MULTITHREAD) {
            mRenderThread.pauseThread();
        }
        if (mControl != null) {
            mControl.onPause();
        }
        if(mJukebox != null){
            mJukebox.pauseBgMusic();
        }
    }

    public void resumeGame() {
        if (mRenderThread != null && MULTITHREAD) {
            mRenderThread.resumeThread();
        }
        if (mUpdateThread != null) {
            mUpdateThread.resumeThread();
        }
        if (mControl != null) {
            mControl.onResume();
        }
        if(mJukebox != null){
            mJukebox.resumeBgMusic();
        }
    }

    public void addGameObject(final GameObject gameObject) {
        if (isRunning()){
            mObjectsToAdd.add(gameObject);
        } else {
            addGameObjectNow(gameObject);
        }
    }
    public void removeGameObject(final GameObject gameObject) {
        mObjectsToRemove.add(gameObject);
    }

    //called from UpdateThread
    public void onUpdate(final float dt) {
        mControl.update(dt);
        mCamera.update(dt);
        final int numObjects = mGameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            mGameObjects.get(i).update(dt);
        }
        checkCollisions();
        addAndRemoveObjects();
        if(!MULTITHREAD) {
            render();
        }
        //checkForMaybeToggleThreading(dt);
    }
    public void toggleRenderThread(){
        MULTITHREAD = !MULTITHREAD;
        if(MULTITHREAD){
            Log.d(TAG, "Render thread ON.");
            mRenderThread.resumeThread();
        }else{
            Log.d(TAG, "Render thread OFF.");
            mRenderThread.stopThread();
        }
        mThreadingToggleTimer = 0f;
    }
    private void checkForMaybeToggleThreading(final float dt){
        if(mControl.mJump){ //hold jump for THREADING_TOGGLE_TIMEOUT to toggle threading on/off
            mThreadingToggleTimer+=dt;
            if(mThreadingToggleTimer > THREADING_TOGGLE_TIMEOUT){
                toggleRenderThread();
            }
        }else{
            mThreadingToggleTimer = 0f;
        }
    }

    private void addAndRemoveObjects(){
        GameObject temp;
        synchronized (updateLock) {//we only lock when changing the length of our entity list.
            while (!mObjectsToRemove.isEmpty()) {
                temp = mObjectsToRemove.remove(0);
                mGameObjects.remove(temp);
            }
            while (!mObjectsToAdd.isEmpty()) {
                temp = mObjectsToAdd.remove(0);
                addGameObjectNow(temp);
            }
        }
    }

    //called from RenderThread
    //note that the mVisibleObjects are not *copied* from the game state, it is a list of direct refs
    //meaning some of them might be touched (= move) by update() while being rendered.
    //The vast majority of objects are not movable so this is generally not perceptible,
    //except for one crucial aspect; the viewport!
    //Ergo: I need to pass the viewport settings (by copy) into the render thread each frame.
    //TODO: pass (a copy of) the current viewport "matrix" into render-call.
    public void render() {
        GameObject temp;
        mVisibleObjects.clear();
        synchronized (updateLock) {
            final int numObjects = mGameObjects.size();
            for (int i = 0; i < numObjects; i++) {
                temp = mGameObjects.get(i);
                if (mCamera.inView(temp.mBounds)) {
                    mVisibleObjects.add(mGameObjects.get(i));
                }
            }
        }
        mVisibleObjectCount = mVisibleObjects.size();
        mGameView.render();
    }

    private void checkCollisions() {
        final int count = mGameObjects.size();
        GameObject a, b;
        for(int i = 0; i < count-1; i++){
            a = mGameObjects.get(i);
            if(!a.mCanCollide){ continue; }
            for(int j = i+1; j < count; j++){
                b = mGameObjects.get(j);
                if(!b.mCanCollide){ continue; }
                if(a.isColliding(b)){
                    a.onCollision(b);
                    b.onCollision(a);
                }
            }
        }
    }

    private void addGameObjectNow(final GameObject object) {
        mGameObjects.add(object);
    }

    public boolean isRunning() {
        return mUpdateThread != null && mUpdateThread.isGameRunning();
    }

    public boolean isPaused() {
        return mUpdateThread != null && mUpdateThread.isGamePaused();
    }

    //TODO: probably move this out of the GameEngine.
    final String[] DBG_STRINGS = new String[5];
    final static String DBG_FPS = "Frames/s: %d";
    final static String DBG_UPS = "Ticks/s: %d";
    final static String DBG_OBJ_RENDER_COUNT =  "Objects rendered: %d / %d";
    final static String DBG_PLAYER_INFO =  "Player: [%.2f, %.2f]";
    final static String DBG_EMPTY_STRING = "";
    public String[] getDebugStrings(){
        DBG_STRINGS[0] = mCamera.toString();
        DBG_STRINGS[1] = String.format(DBG_OBJ_RENDER_COUNT, mVisibleObjectCount, mGameObjects.size());
        DBG_STRINGS[2] = String.format(DBG_PLAYER_INFO, mPlayer.x(), mPlayer.y());
        DBG_STRINGS[3] = (MULTITHREAD) ? String.format(DBG_FPS, mRenderThread.getAverageFPS()) : DBG_EMPTY_STRING;
        DBG_STRINGS[4] = String.format(DBG_UPS, mUpdateThread.getAverageFPS());
        return DBG_STRINGS;
    }
}
