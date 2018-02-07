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
import com.ulfben.PlatformerMK3.input.GameInput;
import com.ulfben.PlatformerMK3.input.Gamepad;
import com.ulfben.PlatformerMK3.input.VirtualJoystick;
import com.ulfben.PlatformerMK3.levels.LevelManager;
import com.ulfben.PlatformerMK3.utilities.Axis;

import java.util.ArrayList;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class GameEngine implements SurfaceHolder.Callback{
    private static final String TAG = "GameEngine";
    static final boolean SHOW_STATS = true; //print useful stats to screen during game play
    private static final float SCALE_FACTOR = 0.5f; // we render to a buffer 0.5x the resolution of the physical screen.
        //and use the GPU to scale it up: https://android-developers.googleblog.com/2013/09/using-hardware-scaler-for-performance.html
    private static final float METERS_TO_SHOW_X = 0f; //set the value you want fixed
    private static final float METERS_TO_SHOW_Y = 9f;  //the other is calculated at runtime!
    private static final float METERS_TO_SHOW_ON_SHORTEST_AXIS = 9f; //OR use this to always scale along the shorter axis.

    private final ArrayList<GameObject> mVisibleObjects = new ArrayList<>();
    private final MainActivity mActivity;
    private final GameView mGameView;
    private final ConfigurableGameInput mControls;
    private final Jukebox mJukebox;
    private GameThread mGameThread = null; //created in startGame
    private LevelManager mLevel = null; //created in loadLevel
    private Viewport mCamera = null; //created in loadLevel, and re-created whenever sufaceChange happens.
    private int mSurfaceChangeCount = 0; //for debug output.
    private int mFrameBufferWidth = 1280; //just a default while waiting for the surface to get ready
    private int mFrameBufferHeight = 720; //these will change once we get the surfaceChange callbacks
    private volatile boolean mCameraNeedsUpdating = false; //to let the viewport resize when the surface changes.


    //TODO: figure out what to do with the app bar
    //TODO: add rotation-toggle to the action-bar
    public GameEngine(final MainActivity a, final GameView gameView) {
        super();
        Log.d(TAG, "Constructing GameEngine");
        mActivity = a;
        mGameView = gameView;
        mGameView.getHolder().addCallback(this); //we listen to surface callbacks to adjust our ViewPort when needed.
        mJukebox = new Jukebox(mActivity.getApplicationContext());
        mControls = new ConfigurableGameInput(mActivity.getApplicationContext(),
                        new Gamepad(mActivity),
                        new Accelerometer(mActivity),
                        new VirtualJoystick(mActivity.findViewById(R.id.virtual_joystick))
                     );
        GameObject.mEngine = this; //NOTE: this reference must be nulled in onDestroy!
    }

    //called from GameFragment from onStart.
    public void startGame() {
        Log.d(TAG, "startGame");
        killGameThreadIfRunning(); //Belts & Suspenders.
        if(mLevel == null){
            Log.d(TAG, "You must call loadLevel before startGame!");
            return;
        }
        mControls.onStart();
        mJukebox.resumeBgMusic();
        mGameThread = new GameThread(this);
        mGameThread.start(); //GameThread will call "tick()" in a tight loop.
    }

    //tick is our core game loop, executed continuously from the GameThread
    //the core loop does three things, over and over again:
    // 1. process input
    // 2. update the game state (using time and any inputs from step 1)
    // 3. render - draw the new game state
    void tick(final float dt) {
        input(dt);
        update(dt);
        if(SHOW_STATS){
            refreshStats();
        }
        render(mVisibleObjects, mCamera); //will block and wait on UI thread
    }

    private void input(final float dt){
        mControls.update(dt);
        if(mCameraNeedsUpdating){ //"input" from our UI thread (see: the surfaceChanged callback)
           buildViewport(mFrameBufferWidth, mFrameBufferHeight, METERS_TO_SHOW_X, METERS_TO_SHOW_Y);
        }
    }
    private void update(final float dt){
        mCamera.update(dt);
        mLevel.update(dt); //update & check collisions for all living GameObjects.
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

    public void onGameEvent(final GameEvent e, final GameObject source){
        mJukebox.playSoundForGameEvent(e);
        if(e == GameEvent.PlayerCoinPickup){
            mLevel.removeGameObject(source);
        }
    }

    //lifecycle callbacks. Do not call these from the GameThread!
    // You might spin waiting for the thread to pause or kill itself.
    public void onPause() {
        pauseGameThreadIfRunning(); //spin until the GameThread completes the tick() and waits.
        if (mControls != null) { mControls.onPause(); }
        if (mJukebox != null){ mJukebox.pauseBgMusic(); }
    }
    public void onResume() {
        reloadPreferences(); //coming back from the pause-screen, user might have changed settings.
        if (mJukebox != null){ mJukebox.resumeBgMusic(); }
        if (mControls != null){ mControls.onResume(); }
        if (mGameThread != null) { mGameThread.resumeThread(); } //resume thread last!
    }
    public void onStop() {
        Log.d(TAG, "onStop");
        killGameThreadIfRunning();
        if (mControls != null) { mControls.onStop(); }
    }
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        killGameThreadIfRunning();
        mGameThread = null;
        if (mControls != null){ mControls.onDestroy(); }
        if (mJukebox != null){ mJukebox.destroy(); }
        if (mLevel != null) { mLevel.destroy();}
        if (mGameView != null){
            mGameView.getHolder().removeCallback(this);
            mGameView.destroy();
        }
        GameObject.mEngine = null;
    }

    //Keep in mind that these surface-callbacks are running on the UI thread.
    //Make sure you don't reach directly into any of the GameThread-state.
    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        mSurfaceChangeCount = 0;
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        mSurfaceChangeCount++;
        Log.d(TAG, "surfaceChanged (" + mSurfaceChangeCount + "): " + width +" : "+height);
        if(width != mFrameBufferWidth || height != mFrameBufferHeight){ //avoid infinite callbacks
            mFrameBufferWidth = (int) (width * SCALE_FACTOR);
            mFrameBufferHeight = (int) (height * SCALE_FACTOR);
            Log.d(TAG, "\tsetFixedSize: " + mFrameBufferWidth +" : "+ mFrameBufferHeight);
            mGameView.setFixedSize(mFrameBufferWidth, mFrameBufferHeight); //Will generate a new surfaceChanged-callback
            mCameraNeedsUpdating = true; //flag so the GameThread knows to resample all bitmaps
        }else{
            Log.d(TAG, "\tIgnoring. Viewport is already at the correct resolution.");
        }
    }
    // We literally must not return from surfaceDestroyed until we are sure that nobody
    // will touch the surface again. See: https://goo.gl/VrjXRW
    // Since we use lockCanvas / unlockAndPost for our rendering this callback isn't
    // important to us. While GameView holds the lock, the surface is guaranteed.
    // However, we will follow the law and spin here until the GameThread has come out
    // of the tick() for sure. It's redunant, but I am in Belt & Suspenders-mode. :P
    @Override
    public void surfaceDestroyed(final SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        pauseGameThreadIfRunning();//If the thread is still running at this point, I'll pause it.
        //Termination is the job of the onStop and onDestroy life cycle callbacks.
    }


    //GameEngine "internal" implementation

    // loadLevel creates a LevelManager, which constructs all GameObjects and load their assets.
    // It also provides an interface to add / remove / update GameObjects.
    // Note: loading assets is dependent on the Viewport (for correct density), so loadLevel will
    //  build a default Viewport if it doesn't exist.
    public void loadLevel(final String levelName){
        Log.d(TAG, "Loading Level: " + levelName);
        if(mLevel != null){
            mLevel.destroy();
        }
        if(mCamera == null || mCameraNeedsUpdating){ //We need Viewport, so LevelManager can load assets with correct pixel density (pixels-per-meter)
            buildViewport(mFrameBufferWidth, mFrameBufferHeight, METERS_TO_SHOW_X, METERS_TO_SHOW_Y);
        }
        mLevel = new LevelManager(levelName);
        setCameraBoundsAndFollowPlayer(); //tell the camera to track our player, and stay within the game world
    }
    //The Viewport provides our density (pixels-per-meter).
    // If a level exists when the viewport is (re)built, LevelManager will be asked to resample all bitmaps to match the new density.
    private void buildViewport(int frameBufferWidth, int frameBufferHeight, float metersToShowX, float metersToShowY){
        mCameraNeedsUpdating = false; //reset the flag
        if(mCamera != null && (mCamera.getScreenWidth() == frameBufferWidth && mCamera.getScreenHeight() == frameBufferHeight)){
            Log.d(TAG, "Viewport already matches the framebuffer. No action taken.");
        }
        Log.d(TAG, "Building viewport for: " + frameBufferWidth +" : "+frameBufferHeight);
        mCamera = new Viewport(frameBufferWidth, frameBufferHeight, metersToShowX, metersToShowY);
        if(METERS_TO_SHOW_ON_SHORTEST_AXIS > 0){
            mCamera = new Viewport(frameBufferWidth, frameBufferHeight, METERS_TO_SHOW_ON_SHORTEST_AXIS);
        }
        Log.d(TAG, "\t Density (pixels-per-meter): " + mCamera.getPixelsPerMeterX() +" : "+mCamera.getPixelsPerMeterY());
        if(mLevel != null) {
            Log.d(TAG, "Reloading all bitmaps.");
            mLevel.reloadBitmaps();
        }
        setCameraBoundsAndFollowPlayer();
    }
    private void pauseGameThreadIfRunning(){
        if(mGameThread == null){ return; }
        if(mGameThread.isTerminated()){ return; }
        while(!mGameThread.isGamePaused()){ //spin until the GameThread comes out of the tick()
            mGameThread.pauseThread();      //and waits()
        }
    }
    private void killGameThreadIfRunning(){
        if(mGameThread == null) {return;}
        if(mGameThread.isTerminated()) { return; }
        while(!mGameThread.isTerminated()) {
            mGameThread.stopThread(); //will un-pause if needed, and then kill
        }
        Log.d(TAG, "GameThread terminated!");
    }
    private void setCameraBoundsAndFollowPlayer(){
        if(mCamera == null || mLevel == null){ return; }
        mCamera.lookAt(mLevel.mPlayer);
        mCamera.follow(mLevel.mPlayer);
        mCamera.setBounds(mLevel.getWorldWidth(), mLevel.getWorldHeight());
    }
    private void reloadPreferences(){
        if(mJukebox != null){ mJukebox.reloadAndApplySettings(); }
        if(mControls != null){ mControls.reloadAndApplySettings(); }
    }
    private void refreshStats(){
        DebugTextRenderer.VISIBLE_OBJECTS = mVisibleObjects.size();
        DebugTextRenderer.TOTAL_OBJECT_COUNT = mLevel.mGameObjects.size();
        DebugTextRenderer.PLAYER_POSITION.x = mLevel.mPlayer.x;
        DebugTextRenderer.PLAYER_POSITION.y = mLevel.mPlayer.y;
        DebugTextRenderer.FRAMERATE = mGameThread.getAverageFPS();
        DebugTextRenderer.CAMERA_INFO = mCamera.toString();
    }
    public Context getContext(){ return mActivity.getApplicationContext(); }
    public GameInput getControls() { return mControls; }
    public int getPixelsPerMeterY(){
        return mCamera.getPixelsPerMeterY();
    }
    public int getPixelsPerMeterX(){
        return mCamera.getPixelsPerMeterX();
    }
    public float getWorldWidth(){ return mLevel.getWorldWidth(); } //world is always measured in meters
    public float getWorldHeight(){ return mLevel.getWorldHeight(); } //world is always measured in meters
    public int getResolutionY(){ return mCamera.getScreenHeight(); } //our framebuffer is not tied to the screens physical pixel count.
    public int getResolutionX(){ return mCamera.getScreenWidth(); }
    public boolean isRunning() { return mGameThread != null && mGameThread.isGameRunning(); }
    public boolean isPaused() {
        return mGameThread != null && mGameThread.isGamePaused();
    }
    public float screenToWorld(final float pixelDistance, final Axis axis){
        return (axis == Axis.X) ? pixelDistance / mCamera.getPixelsPerMeterX() : pixelDistance / mCamera.getPixelsPerMeterY();
    }
    public float worldToScreen(final float worldDistance, final Axis axis){
        return (axis == Axis.X) ? worldDistance * mCamera.getPixelsPerMeterX() : worldDistance * mCamera.getPixelsPerMeterY();
    }
}
