package com.ulfben.PlatformerMK3.engine;

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
        resumeThread();
    }

    @Override
    public void run() {
        mTimer.reset();
        while(mIsRunning) {
            if(mIsPaused){
                waitUntilResumed();
            }
            mGameEngine.onUpdate(mTimer.tick());
            //maybeSleep(); //SurfaceView will enforce rate limiting for us in unlockCanvasAndPost
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