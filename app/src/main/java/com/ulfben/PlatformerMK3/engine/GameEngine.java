package com.ulfben.PlatformerMK3.engine;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.ulfben.PlatformerMK3.GameEvent;
import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.gameobjects.Player;
import com.ulfben.PlatformerMK3.input.Accelerometer;
import com.ulfben.PlatformerMK3.input.ConfigurableGameInput;
import com.ulfben.PlatformerMK3.input.GameInput;
import com.ulfben.PlatformerMK3.input.Gamepad;
import com.ulfben.PlatformerMK3.input.VirtualJoystick;
import com.ulfben.PlatformerMK3.levels.LevelManager;
import com.ulfben.PlatformerMK3.utilities.Axis;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
import com.ulfben.PlatformerMK3.utilities.BitmapUtils;

import java.util.ArrayList;
import java.util.Locale;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class GameEngine {
    private static final String TAG = "GameEngine";
    public static final boolean SHOW_STATS = true; //print useful stats to screen during game play
    private static final float SCALE_FACTOR = 0.5f; //< 1.0f for fullscreen scaling. 0.5 = use half the screens' resolution.
    private static final float METERS_TO_SHOW_X = 0f; //set the value you want fixed
    private static final float METERS_TO_SHOW_Y = 9f;  //the other is calculated at runtime!

    private UpdateThread mUpdateThread = null;
    private MainActivity mActivity;
    private GameView mGameView;
    public LevelManager mLevelManager = null;
    public Viewport mCamera = null;
    public ConfigurableGameInput mControls;
    private ArrayList<GameObject> mVisibleObjects = new ArrayList<>();
    private final Jukebox mJukebox;

    public GameEngine(final MainActivity a, final GameView gameView) {
        super();
        mActivity = a;
        mGameView = gameView;
        //TODO: remove viewPort from gameView!
        mCamera = mGameView.createViewport(0f, 0f, METERS_TO_SHOW_X, METERS_TO_SHOW_Y, SCALE_FACTOR);
        mJukebox = new Jukebox(mActivity);
        mControls = new ConfigurableGameInput(mActivity,
                    new Gamepad( mActivity),
                    new Accelerometer(mActivity),
                    new VirtualJoystick(mActivity.findViewById(R.id.virtual_joystick))
                );
        GameObject.mEngine = this;
        BitmapPool.init();
        BitmapUtils.init(a.getResources());
    }


    public boolean toggleMotionControl(){
        return mControls.toggleMotionControl(mActivity);
    }
    public boolean hasMotionControl(){
        return mControls.hasMotionControl();
    }

    public int getResourceID(final String filename){
        return getResourceID(filename, "drawable");
    }
    public int getResourceID(final String filename, final String type){
        return mGameView.getResources().getIdentifier(filename, type, mGameView.getContext().getPackageName());
    }
//    public String getPackageName(){ return mActivity.getPackageName(); }
//    public Resources getResources(){ return mActivity.getResources(); }
    public Context getContext(){ return mGameView.getContext(); }
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

    public void startGame() {
        stopGame(); // Stop a game if it is running
        mControls.onStart();
        mUpdateThread = new UpdateThread(this);
        mUpdateThread.start();
        mJukebox.resumeBgMusic();
    }

    public void stopGame() {
        mControls.onStop();
        if (mUpdateThread != null) {
            mUpdateThread.stopThread();
        }
        //TODO maybe stop jukebox?

    }

    public void pauseGame() {
        if (mUpdateThread != null) {
            mUpdateThread.pauseThread();
        }
        mControls.onPause();
        if(mJukebox != null){
            mJukebox.pauseBgMusic();
        }
    }

    public void resumeGame() {
        if(mJukebox != null){
            mJukebox.resumeBgMusic();
        }
        mControls.onResume();
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
        if(mControls != null){ mControls.onDestroy(); mControls = null; }
        if(mGameView != null){ mGameView.destroy(); }
        GameObject.mEngine = null;
    }

    //called from UpdateThread
    public void onUpdate(final float dt) {
        mControls.update(dt);
        mCamera.update(dt);
        mLevelManager.update(dt);
        checkCollisions(mLevelManager.mGameObjects);
        mLevelManager.addAndRemoveObjects();
        buildVisibleSet(mLevelManager.mGameObjects, mCamera);
        if(SHOW_STATS){
            updateStats();
        }
        render(mVisibleObjects, mCamera); //will wait on UI thread on post
    }

    private void updateStats(){
        DebugTextRenderer.VISIBLE_OBJECTS = mVisibleObjects.size();
        DebugTextRenderer.TOTAL_OBJECT_COUNT = mLevelManager.mGameObjects.size();
        DebugTextRenderer.PLAYER_POSITION = mLevelManager.mPlayer.mWorldLocation;
        DebugTextRenderer.FRAMERATE = mUpdateThread.getAverageFPS();
        DebugTextRenderer.CAMERA_INFO = mCamera.toString();
    }

    private void buildVisibleSet(final ArrayList<GameObject> gameObjects, final Viewport camera){
        GameObject temp;
        mVisibleObjects.clear();
        final int numObjects = gameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            temp =  gameObjects.get(i);
            if (camera.inView(temp.mWorldLocation, temp.mWidth, temp.mHeight)) {
                mVisibleObjects.add(temp);
            }
        }
    }

    private void render(final ArrayList<GameObject> visibleObjects, final Viewport camera) {
        mGameView.render(visibleObjects, camera);
    }

    //checkCollisions will test all game entities against each other.
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
}
