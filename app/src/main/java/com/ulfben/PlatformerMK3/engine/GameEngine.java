package com.ulfben.PlatformerMK3.engine;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;

import com.ulfben.PlatformerMK3.GameEvent;
import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.input.Accelerometer;
import com.ulfben.PlatformerMK3.input.ConfigurableGameInput;
import com.ulfben.PlatformerMK3.input.Gamepad;
import com.ulfben.PlatformerMK3.input.VirtualJoystick;
import com.ulfben.PlatformerMK3.levels.LevelManager;
import com.ulfben.PlatformerMK3.utilities.Axis;

import java.util.ArrayList;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class GameEngine implements SurfaceHolder.Callback{
    private static final String TAG = "GameEngine";
    public static final boolean SHOW_STATS = true; //print useful stats to screen during game play
    private static final float SCALE_FACTOR = 0.5f; // we render to buffer 0.5x the resolution of the physical screen.
        //and use the GPU to fill the screen with it. This is recommended practice: https://android-developers.googleblog.com/2013/09/using-hardware-scaler-for-performance.html
    private static final float METERS_TO_SHOW_X = 0f; //set the value you want fixed
    private static final float METERS_TO_SHOW_Y = 9f;  //the other is calculated at runtime!

    private GameThread mGameThread = null;
    private MainActivity mActivity;
    private GameView mGameView;
    private LevelManager mLevel = null;
    private Viewport mCamera;
    public ConfigurableGameInput mControls;
    private ArrayList<GameObject> mVisibleObjects = new ArrayList<>();
    private final Jukebox mJukebox;
    private boolean mIsSurfaceInitialized = false;

    //TODO: figure out what to do with the app bar
    //TODO: decide if you want to keep rotation. If so; sort out serialization of the game state...
    public GameEngine(final MainActivity a, final GameView gameView) {
        super();
        mActivity = a;
        mGameView = gameView;
        mJukebox = new Jukebox(mActivity.getApplicationContext());
        mControls = new ConfigurableGameInput(mActivity.getApplicationContext(),
                            new Gamepad(mActivity),
                            new Accelerometer(mActivity),
                            new VirtualJoystick(mActivity.findViewById(R.id.virtual_joystick))
                     );
        GameObject.mEngine = this; //NOTE: this reference must be nulled in onDestroy!
        mGameView.getHolder().addCallback(this); //call onFirstSurfaceChange as soon as the SurfaceView is available.
    }

    private void onFirstSurfaceChange(int frameBufferWidth, int frameBufferHeight){
        mCamera = createViewport(frameBufferWidth, frameBufferHeight, METERS_TO_SHOW_X, METERS_TO_SHOW_Y);
        loadLevel("TestLevel");
        startGame();
    }

    private Viewport createViewport(int frameBufferWidth, int frameBufferHeight, float metersToShowX, float metersToShowY){
        Viewport camera = new Viewport(frameBufferWidth, frameBufferHeight, metersToShowX, metersToShowY);
        if(mLevel != null) {
            camera.lookAt(mLevel.mPlayer);
            camera.follow(mLevel.mPlayer);
            camera.setBounds(mLevel.getWorldWidth(), mLevel.getWorldHeight());
        }
        return camera;
    }

    public void loadLevel(final String levelName){
        if(mLevel != null){
            mLevel.destroy(); //release loaded assets
        }
        mLevel = new LevelManager(levelName);
        if(mCamera != null) {
            mCamera.lookAt(mLevel.mPlayer);
            mCamera.follow(mLevel.mPlayer);
            mCamera.setBounds(mLevel.getWorldWidth(), mLevel.getWorldHeight());
        }
    }

    public void startGame() {
        Log.d(TAG, "startGame");
        stopGame(); // Stop a game if it is already running.
        mControls.onStart();
        mJukebox.resumeBgMusic();
        mGameThread = new GameThread(this);
        mGameThread.start(); //GameThread will call our "tick()" in a tight loop.
    }

    //tick is our core game loop, executed continuously from the GameThread
    //the core loop does three things, over and over again:
    // 1. process input
    // 2. update the game state (using time and any inputs from step 1)
    // 3. render - draw the new game state
    public void tick(final float dt) {
        input(dt);
        update(dt);
        if(SHOW_STATS){
            refreshStats();
        }
        render(mVisibleObjects, mCamera); //will block and wait on UI thread
    }

    private void input(final float dt){
        mControls.update(dt);
    }

    private void update(final float dt){
        mCamera.update(dt);
        mLevel.update(dt);
        checkCollisions(mLevel.mGameObjects);
        mLevel.addAndRemoveObjects();
        buildVisibleSet(mLevel.mGameObjects, mCamera);
    }

    private void render(final ArrayList<GameObject> visibleObjects, final Viewport camera) {
        mGameView.render(visibleObjects, camera); //will block and wait for the UI thread
    }

    private void buildVisibleSet(final ArrayList<GameObject> gameObjects, final Viewport camera){
        GameObject temp;
        mVisibleObjects.clear();
        final int numObjects = gameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            temp = gameObjects.get(i);
            if (camera.inView(temp)) {
                mVisibleObjects.add(temp);
            }
        }
    }

    //checkCollisions will test all game entities against each other.
    //Note the offsets in these loops: [0]-[size-1] and [i+1]-[size]
    //This ensure we never redundantly test a pair.
    //For details, refer to my slides (10-17): https://goo.gl/po4YkK
    private void checkCollisions(final ArrayList<GameObject> gameObjects) {
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

    public void onGameEvent(final GameEvent e, final GameObject source){
        mJukebox.playSoundForGameEvent(e);
        if(e == GameEvent.PlayerCoinPickup){
            mLevel.removeGameObject(source);
        }
    }

    public void pauseGame() {
        if (mGameThread != null) { mGameThread.pauseThread(); } //pause thread first!
        if (mControls != null) { mControls.onPause(); }
        if (mJukebox != null){ mJukebox.pauseBgMusic(); }
    }

    public void reloadPreferences(){
        if(mJukebox != null){
            mJukebox.reloadAndApplySettings();
        }
        if(mControls != null){
            mControls.reloadAndApplySettings();
        }
    }

    public void resumeGame() {
        if (mJukebox != null){ mJukebox.resumeBgMusic(); }
        if (mControls != null){ mControls.onResume(); }
        if (mGameThread != null) { mGameThread.resumeThread(); } //resume thread last!
    }

    public void stopGame() {
        if (mGameThread != null) { mGameThread.stopThread(); } //will unpause if needed, and then kill
        if (mControls != null) {
            mControls.onStop();
        }
    }

    public void onDestroy(){
        stopGame(); //stop the game thread and calls onStop for all relevant members
        mGameThread = null;
        if (mControls != null){ mControls.onDestroy(); mControls = null; }
        GameObject.mEngine = null;
        if (mJukebox != null){
            mJukebox.destroy();
        }
        if (mLevel != null) { mLevel.destroy(); mLevel = null;}
        if (mGameView != null){ mGameView.destroy(); }
        mActivity = null;
    }

    public Context getContext(){ return mActivity.getApplicationContext(); }
    public int getPixelsPerMeterY(){
        return mCamera.getPixelsPerMeterY();
    }
    public int getPixelsPerMeterX(){
        return mCamera.getPixelsPerMeterX();
    }
    public float getWorldWidth(){
        return mLevel.getWorldWidth();
    }
    public float getWorldHeight(){ return mLevel.getWorldHeight(); }
    public int getResolutionY(){ return mCamera.getScreenHeight(); } //our framebuffer is not tied
    public int getResolutionX(){ return mCamera.getScreenWidth(); }  //to the screens physical pixel count.
    public boolean isRunning() {
        return mGameThread != null && mGameThread.isGameRunning();
    }
    public boolean isPaused() {
        return mGameThread != null && mGameThread.isGamePaused();
    }
    public float screenToWorld(final float pixelDistance, final Axis axis){
        return (axis == Axis.X) ? pixelDistance / mCamera.getPixelsPerMeterX() : pixelDistance / mCamera.getPixelsPerMeterY();
    }
    public float worldToScreen(final float worldDistance, final Axis axis){
        return (axis == Axis.X) ? worldDistance * mCamera.getPixelsPerMeterX() : worldDistance * mCamera.getPixelsPerMeterY();
    }
    private void refreshStats(){
        DebugTextRenderer.VISIBLE_OBJECTS = mVisibleObjects.size();
        DebugTextRenderer.TOTAL_OBJECT_COUNT = mLevel.mGameObjects.size();
        DebugTextRenderer.PLAYER_POSITION.x = mLevel.mPlayer.x;
        DebugTextRenderer.PLAYER_POSITION.y = mLevel.mPlayer.y;
        DebugTextRenderer.FRAMERATE = mGameThread.getAverageFPS();
        DebugTextRenderer.CAMERA_INFO = mCamera.toString();
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        Log.d(TAG, "surfaceChanged: " + width +" : "+height);
        if(mIsSurfaceInitialized){return;}
        if(width > 0 && height > 0){
            int frameBufferWidth = (int) (width * SCALE_FACTOR);
            int frameBufferHeight = (int) (height * SCALE_FACTOR);
            if(SCALE_FACTOR != 1.0f){
                Log.d(TAG, "setFixedSize: " + frameBufferWidth +" : "+frameBufferHeight);
                mGameView.setFixedSize(frameBufferWidth, frameBufferHeight);
            }
            mIsSurfaceInitialized = true;
            onFirstSurfaceChange(frameBufferWidth, frameBufferHeight);
        }
    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        if(mGameThread == null){ return; } //GameView won't touch the surface if it's gone,
        while(!mGameThread.isGamePaused()){ //so this is redundant. But let's follow the letter of the law
            mGameThread.pauseThread();      // https://goo.gl/VrjXRW
        }
    }
}
