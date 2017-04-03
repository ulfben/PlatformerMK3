package com.ulfben.PlatformerMK3.engine;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ulfben.PlatformerMK3.GameEvent;
import com.ulfben.PlatformerMK3.utilities.Random;

import java.io.IOException;
import java.util.HashMap;
// Created by Ulf Benjaminsson (ulfben) on 2017-01-29.


public class Jukebox {
    private static final String TAG = "Jukebox";
    private static final float DEFAULT_MUSIC_VOLUME = 0.4f; //TODO: move to xml
    private static final float DEFAULT_SFX_VOLUME = 0.4f; //and make prefs
    private static final int MAX_STREAMS = 5;

    private Context mContext = null;
    private SoundPool mSoundPool = null;
    private MediaPlayer mBgPlayer = null;
    private HashMap<GameEvent, Integer> mSoundsMap = null;

    private static final String SOUNDS_PREF_KEY = "sounds_pref_key";
    private static final String MUSIC_PREF_KEY = "music_pref_key";
    public boolean mSoundEnabled;
    public boolean mMusicEnabled;

    //https://developer.android.com/guide/topics/media-apps/volume-and-earphones.html
    public Jukebox(final Context context) {
        super();
        mContext = context;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mSoundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true);
        mMusicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true);
        loadIfNeeded();
    }

    private void loadIfNeeded(){
        if(mSoundEnabled){
            loadSounds();
        }
        if(mMusicEnabled){
            loadMusic();
        }
    }
    private void loadSounds(){
        createSoundPool();
        mSoundsMap = new HashMap<GameEvent, Integer>();
        loadEventSound(GameEvent.PlayerJump, "sfx/jump.wav");
        loadEventSound(GameEvent.PlayerCoinPickup, "sfx/pickup_coin.wav");
       // loadEventSound(GameEvent.PlayerJump, "sfx/button_select.wav");

    }
    private void loadMusic(){
        final String[] bgm = {
                "bgm/ChibiNinja.mp3",
                "bgm/AllofUs.mp3",
                "bgm/Prologue.mp3"
        };
        final String track = bgm[Random.nextInt(bgm.length)];
        Log.d(TAG, "Loading " + track);
        try{
            mBgPlayer = new MediaPlayer();
            final AssetFileDescriptor afd = mContext.getAssets().openFd(track);
            mBgPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            mBgPlayer.setLooping(true);
            mBgPlayer.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
            mBgPlayer.prepare();
        }catch(final IOException e){
            e.printStackTrace();
        }
    }
    public void playSoundForGameEvent(final GameEvent event){
        if(!mSoundEnabled){return;}
        final float leftVolume = DEFAULT_SFX_VOLUME;
        final float rightVolume = DEFAULT_SFX_VOLUME;
        final int priority = 1;
        final int loop = 0; //-1 loop forever, 0 play once
        final float rate = 1.0f;
        final Integer soundID = mSoundsMap.get(event);
        if(soundID != null){
            mSoundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }
    public void toggleSoundStatus(){
        mSoundEnabled = !mSoundEnabled;
        if(mSoundEnabled){
            loadSounds();
        }else{
            unloadSounds();
        }
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit().putBoolean(SOUNDS_PREF_KEY, mSoundEnabled)
                .commit();
    }
    public void toggleMusicStatus(){
        mMusicEnabled = !mMusicEnabled;
        if(mMusicEnabled){
            loadMusic();
        }else{
            unloadMusic();
        }
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit().putBoolean(MUSIC_PREF_KEY, mSoundEnabled)
                .commit();
    }

    public void destroy(){
        if(mSoundEnabled){
            unloadSounds();
        }
        if(mMusicEnabled){
            unloadMusic();
        }
    }

    public void pauseBgMusic(){
        if(!mMusicEnabled){ return; }
        mBgPlayer.pause();
    }
    public void resumeBgMusic(){
        if(!mMusicEnabled){ return; }
        mBgPlayer.start();
    }

    private void loadEventSound(final GameEvent event, final String fileName){
        try {
            final AssetFileDescriptor afd = mContext.getAssets().openFd(fileName);
            final int soundId = mSoundPool.load(afd, 1);
            mSoundsMap.put(event, soundId);
        }catch(final IOException e){
            Log.e(TAG, "Error loading sound "+e.toString());
        }
    }
    private void unloadSounds(){
        if(mSoundPool == null) { return; }
        mSoundPool.release();
        mSoundPool = null;
        mSoundsMap.clear();
    }

    private void unloadMusic(){
        if(mBgPlayer == null) { return; }
        mBgPlayer.stop();
        mBgPlayer.release();
    }

    @SuppressWarnings("deprecation")
    private void createSoundPool(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }else{
            final AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
           mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(MAX_STREAMS)
                    .build();
        }
    }
}
