package com.ulfben.PlatformerMK3.utilities;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-12.

public class FrameTimer {
    public static long SECOND_IN_NANOSECONDS = 1000000000;
    public static long MILLISECOND_IN_NANOSECONDS = 1000000;
    public static float NANOSECONDS_TO_MILLISECONDS = 1.0f / MILLISECOND_IN_NANOSECONDS;
    public static float NANOSECONDS_TO_SECONDS = 1.0f / SECOND_IN_NANOSECONDS;

    private static final long SAMPLE_INTERVAL = (long) (SECOND_IN_NANOSECONDS/2);
    private long mStartFrameTime = 0;
    private long mElapsedTime = 0;
    private int mFrameCount = 0;
    private long mNanosCount = 0;
    private int mAvgFPS = 0;

    public FrameTimer() {
        super();
        reset();
    }

    public void reset(){
        mStartFrameTime = System.nanoTime();
        mElapsedTime = 0;
        mFrameCount = 0;
        mNanosCount = 0;
    }

    //we call this whenever the gameplay has been paused
    //to avoid spikes in the deltatime
    public void onResume(){
        mStartFrameTime = System.nanoTime();
    }

    public float tick(){
        mFrameCount++;
        mElapsedTime = System.nanoTime()-mStartFrameTime;
        mNanosCount += mElapsedTime;
        mStartFrameTime = System.nanoTime();
        return mElapsedTime * NANOSECONDS_TO_SECONDS;
    }

    public long getElapsedNanos() { return mElapsedTime; }
    public long getElapsedMillis(){
        return (long) (mElapsedTime * NANOSECONDS_TO_MILLISECONDS);
    }
    public float getElapsedSeconds(){
        return mElapsedTime * NANOSECONDS_TO_SECONDS;
    }

    public int getAverageFPS(){
        if(mNanosCount > SAMPLE_INTERVAL) {
            mAvgFPS = (int) (mFrameCount * SECOND_IN_NANOSECONDS / mNanosCount);
            mFrameCount = 0;
            mNanosCount = 0;
        }
        return mAvgFPS;
    }

    //frames per second, at the current delta time. Will oscillate wildly.
    public int getCurrentFPS(){
        if(mElapsedTime > 0){
            return (int) (SECOND_IN_NANOSECONDS / mElapsedTime);
        }
        return 0;
    }
}
