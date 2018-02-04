package com.ulfben.PlatformerMK3.engine;

import android.util.Log;

import com.ulfben.PlatformerMK3.utilities.FrameTimer;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class UpdateThread extends Thread {
    private static final String TAG = "UpdateThread";
    private final GameEngine mGameEngine;
    private volatile boolean mIsRunning = true;
    private volatile boolean mIsPaused = false;
    private static final Object mLock = new Object();
    private final FrameTimer mTimer = new FrameTimer();
    private static final long FPS_CAP = 90;
    private static final long TARGET_FRAMETIME = (FrameTimer.SECOND_IN_NANOSECONDS / FPS_CAP);


    //TODO: consider throwing out the thread pausing. Just create a new thread on resume?
    public UpdateThread(final GameEngine gameEngine) {
        super();
        mGameEngine = gameEngine;
    }

    @Override
    public void start() {
        mIsRunning = true;
        mIsPaused = false;
        super.start();
    }

    public void stopThread() {
        mIsRunning = false;
        resumeThread(); //if we are currently paused, resume so we can die.
    }


    // Belts *and* Suspenders:
    //A user reported NPE and OOB exceptions with the game under some circumstances.
    //I was unable to recreate these crashes! After simplifying and cleaning up some of
    //the suspect code-paths, I also added this try/catch around the core loop to avoid
    //uncaught exceptions to bubble out. The game will just stop processing and wait for a quit.
    @Override
    public void run() {
        try {
            mTimer.reset();
            while (mIsRunning) {
                if (mIsPaused) {
                    waitUntilResumed();
                }
                mGameEngine.onUpdate(mTimer.tick());
                //maybeSleep(); //SurfaceView will enforce rate limiting for us in unlockCanvasAndPost
                                //maybeSleep will be necessary GLSurfaceView though!
            }
        }catch(NullPointerException npe){
            Log.e(TAG, "NPE in core loop! " + npe.toString());
        }catch(IndexOutOfBoundsException oob){
            Log.e(TAG, "Out of Bounds in core loop! " + oob.toString());
        }catch(Exception e){
            Log.e(TAG, "Exception in core loop! " + e.toString());
        }
    }

    private void maybeSleep(){
        long delayNS = TARGET_FRAMETIME - mTimer.getElapsedNanos();
        if(delayNS < FrameTimer.NANOSECONDS_TO_MILLISECONDS){
            return; //only sleep if there's more than a millisecond left.
        }
        try {
            final long delayMS = (long) (delayNS * FrameTimer.NANOSECONDS_TO_MILLISECONDS);
            delayNS = delayNS % FrameTimer.MILLISECOND_IN_NANOSECONDS;
            Thread.sleep(delayMS, (int) delayNS);
        } catch (final InterruptedException e) {
            //ignored
        }
    }

    private void waitUntilResumed(){
        while(mIsPaused){
            if(!mIsRunning){return;} //we were asked to shutdown while resting, let's bail
            try{
                synchronized(mLock){
                    mLock.wait();
                }
            } catch (final InterruptedException e) {
                //ignored
            }
        }
        mTimer.onResume();
    }

    public void pauseThread() {
        mIsPaused = true;
    }

    public void resumeThread() {
        if (mIsPaused) {
            mIsPaused = false;
            synchronized (mLock) {
                mLock.notify();
            }
        }
    }

    public int getAverageFPS(){
        return mTimer.getAverageFPS();
    }
    public boolean isGameRunning() {
        return mIsRunning;
    }
    public boolean isGamePaused() {
        return mIsPaused;
    }
}