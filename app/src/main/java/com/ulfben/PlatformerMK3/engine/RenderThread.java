package com.ulfben.PlatformerMK3.engine;

import com.ulfben.PlatformerMK3.FrameTimer;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-07.

public class RenderThread extends Thread {
    private final GameEngine mGameEngine;
    private volatile boolean mIsRunning = true;
    private volatile boolean mIsPaused = false;
    private Object mLock = new Object();
    private FrameTimer mTimer = new FrameTimer();
    private static final long FPS_CAP = 60;
    private static final long TARGET_FRAMETIME = (FrameTimer.SECOND_IN_NANOSECONDS / FPS_CAP);

    public RenderThread(final GameEngine gameEngine) {
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
        while (mIsRunning) {
            mTimer.tick();
            mGameEngine.render();
            if (mIsPaused) {
                waitUntilResumed();
            }
            if (mTimer.getElapsedNanos() < TARGET_FRAMETIME) {
                sleep();
            }
        }
    }

    private void sleep(){
        try {
            long delayNS = TARGET_FRAMETIME - mTimer.getElapsedNanos();
            final long delayMS = (long) (delayNS * FrameTimer.NANOSECONDS_TO_MILLISECONDS);
            delayNS = delayNS % FrameTimer.MILLISECOND_IN_NANOSECONDS;
            Thread.sleep(delayMS, (int) delayNS);
        } catch (InterruptedException e) {
        }
    }

    private void waitUntilResumed(){
        while(mIsPaused){
            try{
                synchronized(mLock){
                    mLock.wait();
                }
            } catch (InterruptedException e) {
                //ignored
            }
        }
        mTimer.reset();
    }
    public void pauseThread() {
        mIsPaused = true;
    }

    public void resumeThread() {
        mTimer.reset();
        if (mIsPaused) {
            mIsPaused = false;
            synchronized (mLock) {
                mLock.notify();
            }
        }
    }

    public int getAverageFPS(){
        synchronized (mTimer) {
            return mTimer.getAverageFPS();
        }
    }

    public boolean isGameRunning() {
        return mIsRunning;
    }

    public boolean isGamePaused() {
        return mIsPaused;
    }
}
