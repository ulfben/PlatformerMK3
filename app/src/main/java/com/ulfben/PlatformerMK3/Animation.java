package com.ulfben.PlatformerMK3;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.utilities.Axis;
import com.ulfben.PlatformerMK3.utilities.BitmapPool;
import com.ulfben.PlatformerMK3.utilities.BitmapUtils;
import com.ulfben.PlatformerMK3.utilities.Random;
import com.ulfben.PlatformerMK3.utilities.Utils;

// Created by Ulf Benjaminsson (ulfben) on 2017-02-20.

public class Animation {
    private static final String TAG = "Animation";
    private final float MAX_PLAYBACK_RATE = 2.0f;
    private final float MIN_PLAYBACK_RATE = 0f;
    private final float DEFAULT_PLAYBACK_RATE = 1f;
    private final GameEngine mEngine;
    private Bitmap[] mFrames = null;
    private float[] mFrameHeights = null; //pixels
    private float[] mFrameWidths = null; //pixels
    private int[] mFrameTimesMillis = null;
    private boolean mIsOneShot = false;
    private int mFrameCount = -1;
    private int mDuration = 0;
    private int mElapsedTime = 0;
    private volatile int mCurrentFrame = 0; //read by both the update and render-thread
    private float mPlaybackRate = DEFAULT_PLAYBACK_RATE;
    private final int mResourceID;
    private final float mWidthMeters; //original measures, so we can resample if need be
    private final float mHeightMeters;

    public Animation(final GameEngine engine, final int resourceID, final float width, final float height){
        super();
        mEngine = engine;
        mResourceID = resourceID;
        mWidthMeters = width;
        mHeightMeters = height;
        resampleSprites();
    }

    public void resampleSprites(){
        int elapsed = mElapsedTime; //save elapsed time before (re)loading the animation
        destroy(); //clears out everything
        prepareAnimation(mResourceID, mWidthMeters, mHeightMeters); //(re)builds everything
        mElapsedTime = elapsed; //restore our current frame
        if(!mIsOneShot && elapsed == 0) { //but if we were starting from 0, and is a repeating anim
            mElapsedTime = Random.nextInt(mDuration); //randomize the first start position.
        }
        updateCurrentFrame();
    }

    public Bitmap getCurrentBitmap(){
        return mFrames[mCurrentFrame];
    }
    public float getCurrentHeightMeters(){
        return mEngine.screenToWorld(mFrameHeights[mCurrentFrame], Axis.Y);
    }
    public float getCurrentWidthMeters(){
        return mEngine.screenToWorld(mFrameWidths[mCurrentFrame], Axis.X);
    }
    public void setPlaybackRate(final float rate){
        mPlaybackRate = Utils.clamp(rate, MIN_PLAYBACK_RATE, MAX_PLAYBACK_RATE);
    }

    public void update(final float secondsPassed) {
        final int elapsedMillis = (int) (1000.0f*secondsPassed);
        mElapsedTime += elapsedMillis*mPlaybackRate;
        if (mElapsedTime > mDuration) {
            if (mIsOneShot) { return; }
            mElapsedTime = mElapsedTime % mDuration;
        }
        updateCurrentFrame();
    }

    //TODO: only update currentFrame when needed, instead of every frame
    private void updateCurrentFrame(){
        int timeToNext = 0;
        for (int i = 0; i < mFrameCount; i++) {
            timeToNext += mFrameTimesMillis[i];
            if (timeToNext > mElapsedTime) {
                mCurrentFrame = i;
                break;
            }
        }
    }

    public void resetAnimation(){
        setPlaybackRate(DEFAULT_PLAYBACK_RATE);
        mElapsedTime = 0;
        updateCurrentFrame();
    }

    private void prepareAnimation(final int resourceID, final float widthMeters, final float heightMeters){
        final AnimationDrawable anim = (AnimationDrawable) ContextCompat.getDrawable(mEngine.getContext(), resourceID);
        mFrameCount = anim.getNumberOfFrames();
        mDuration = getAnimationLengthMillis(anim);
        mElapsedTime = 0;
        mIsOneShot = anim.isOneShot();
        final String spriteName = "Anim_"+resourceID+"_";
        mFrames = prepareAnimation(anim, spriteName, (int) mEngine.worldToScreen(widthMeters, Axis.X), (int) mEngine.worldToScreen(heightMeters, Axis.Y));
        mFrameHeights = getAnimationFrameHeights(mFrames);
        mFrameWidths = getAnimationFrameWidths(mFrames);
        mFrameTimesMillis = getAnimationFrameTimes(anim);
    }

    private static float[] getAnimationFrameHeights(final Bitmap[] frames){
        final int frameCount = frames.length;
        final float[] heights = new float[frameCount];
        for (int i = 0; i < frameCount; i++) {
            //Log.d(TAG, "Frame: " +  i + ", height: " + frames[i].getHeight() + "px");
            heights[i] = frames[i].getHeight();
        }
        return heights;
    }
    private static float[] getAnimationFrameWidths(final Bitmap[] frames){
        final int frameCount = frames.length;
        final float[] widths = new float[frameCount];
        for (int i = 0; i < frameCount; i++) {
            widths[i] = frames[i].getWidth();
        }
        return widths;
    }

    private static Bitmap[] prepareAnimation(final AnimationDrawable anim, final String sprite, final int widthPixels, final int heightPixels){
        final int frameCount = anim.getNumberOfFrames();
        final Bitmap[] frames = new Bitmap[frameCount];
        String key;
        for (int i = 0; i< frameCount; i++) {
            key = BitmapPool.makeKey(sprite+i, widthPixels, heightPixels);
            if(!BitmapPool.contains(key)){
                BitmapPool.put(key, BitmapUtils.scaleBitmap(((BitmapDrawable) anim.getFrame(i)).getBitmap(), widthPixels, heightPixels));
            }
            frames[i] = BitmapPool.getBitmap(key);
        }
        return frames;
    }

    private static int[] getAnimationFrameTimes(final AnimationDrawable anim){
        final int count = anim.getNumberOfFrames();
        final int[] frameTimes = new int[count];
        for (int i = 0; i < count; i++) {
            frameTimes[i] = anim.getDuration(i);
        }
        return frameTimes;
    }

    private static int getAnimationLengthMillis(final AnimationDrawable anim){
        int length = 0;
        final int count = anim.getNumberOfFrames();
        for (int i = 0; i< count; i++) {
            length += anim.getDuration(i);
        }
        return length;
    }

    public void destroy(){
        if(mFrames != null){
            for (final Bitmap b : mFrames){
                BitmapPool.remove(b);
            }
        }
        mFrames = null;
        mFrameHeights = null;
        mFrameWidths = null;
        mFrameTimesMillis = null;
    }
}
