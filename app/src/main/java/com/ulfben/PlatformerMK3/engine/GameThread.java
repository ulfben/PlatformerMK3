package com.ulfben.PlatformerMK3.engine;

import android.util.Log;

import com.ulfben.PlatformerMK3.utilities.FrameTimer;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class GameThread extends Thread {
    private static final String TAG = "GameThread";
    private final GameEngine mGameEngine;
    private volatile boolean mIsRunning = true;
    private volatile boolean mIsPaused = false;
    private static final Object mLock = new Object(); //used
    private final FrameTimer mTimer = new FrameTimer();
    private static final long FPS_CAP = 60;
    private static final long TARGET_FRAMETIME = (FrameTimer.SECOND_IN_NANOSECONDS / FPS_CAP);

    public GameThread(final GameEngine gameEngine) {
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

    @Override
    public void run() {
        mTimer.reset();
        while (mIsRunning) {
            mGameEngine.tick(mTimer.tick());
            //maybeSleep(); //SurfaceView.unlockCanvasAndPost will enforce rate limiting for us
                            //when we render to a GLSurfaceView  we will need maybeSleep though!
            if (mIsPaused) { //pause at end of frame, so if we are waking up only to shut down
                waitUntilResumed(); //we don't execute another frame unnecessarily.
            }
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