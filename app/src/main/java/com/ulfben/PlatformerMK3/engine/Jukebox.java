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
    private static final float DEFAULT_SFX_VOLUME = 0.4f; //and make settings
    private static final int MAX_STREAMS = 5;

    private Context mContext = null;
    private SoundPool mSoundPool = null;
    private MediaPlayer mMediaPlayer = null;
    private HashMap<GameEvent, Integer> mSoundsMap = null;

    public static final String SOUNDS_PREF_KEY = "sounds_pref_key";
    public static final String MUSIC_PREF_KEY = "music_pref_key";
    private final SharedPreferences mPrefs;
    private boolean mSoundEnabled = false;
    private boolean mMusicEnabled = false;

    //https://developer.android.com/guide/topics/media-apps/volume-and-earphones.html
    Jukebox(final Context context) {
        super();
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        reloadAndApplySettings();
    }

    private void loadSounds(){
        createSoundPool();
        mSoundsMap = new HashMap<>();
        loadEventSound(GameEvent.PlayerJump, "sfx/jump.wav"); //TODO: move to settings!
        loadEventSound(GameEvent.PlayerCoinPickup, "sfx/pickup_coin.wav");
    }
    private void loadMusic(){
        final String[] bgm = { //TODO: move to settings
                "bgm/ChibiNinja.mp3",
                "bgm/AllofUs.mp3"
        };
        final String track = bgm[Random.nextInt(bgm.length)];
        Log.d(TAG, "Loading " + track);
        try{
            mMediaPlayer = new MediaPlayer();
            final AssetFileDescriptor afd = mContext.getAssets().openFd(track);
            mMediaPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
            mMediaPlayer.prepare();
        }catch(final IOException e){
            Log.d(TAG, e.toString());
        }
    }
    void playSoundForGameEvent(final GameEvent event){
        if(!mSoundEnabled || mSoundPool == null){return;}
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

    void reloadAndApplySettings(){
        Log.d(TAG, "reloading settings...");
        mSoundEnabled = mPrefs.getBoolean(SOUNDS_PREF_KEY, true);
        if(mSoundEnabled && mSoundPool == null){
            loadSounds(); //only load again if we have unloaded before.
        } else if(!mSoundEnabled){
            unloadSounds();
        }
        mMusicEnabled = mPrefs.getBoolean(MUSIC_PREF_KEY, true);
        if(mMusicEnabled && mMediaPlayer == null){
            Log.d(TAG, "loading music");
            loadMusic();
            resumeBgMusic();
        }else if(!mMusicEnabled){
            Log.d(TAG, "unloading music");
            unloadMusic();
        }
    }

    void destroy(){
        if(mSoundEnabled){
            unloadSounds();
        }
        if(mMusicEnabled){
            unloadMusic();
        }
        mContext = null;
    }

    void pauseBgMusic(){
        if(!mMusicEnabled){ return; }
        mMediaPlayer.pause();
    }
    void resumeBgMusic(){
        if(!mMusicEnabled){ return; }
        mMediaPlayer.start();
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
        if(mMediaPlayer == null) { return; }
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    //the SoundPool API was changed in Lollipop (SDK 21) so I implement both
    //the old and new, and decorate the method to silence lint warnings.
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
