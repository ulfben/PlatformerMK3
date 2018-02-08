package com.ulfben.PlatformerMK3.gui;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.Jukebox;
import com.ulfben.PlatformerMK3.input.ConfigurableGameInput;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-04.

public class PauseDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "PauseDialog";
    private final PauseDialogListener mListener;
    private final SharedPreferences mPrefs;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private boolean allowMotionControl = true;
    public PauseDialog(final MainActivity activity, PauseDialogListener listener) {
        super(activity);
        mListener = listener;
        setContentView(R.layout.dialog_pause);
        findViewById(R.id.btn_music).setOnClickListener(this);
        findViewById(R.id.btn_sound).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        findViewById(R.id.btn_accelerometer).setOnClickListener(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        musicEnabled =  mPrefs.getBoolean(Jukebox.MUSIC_PREF_KEY, true);
        soundEnabled = mPrefs.getBoolean(Jukebox.SOUNDS_PREF_KEY, true);
        allowMotionControl = mPrefs.getBoolean(ConfigurableGameInput.ACCELEROMETER_PREF_KEY, true);
        updateButtonStates();
    }


    // Q&D: ugly hack to let UI buttons and dialog boxes read/write game settings.
    // TODO: figure out how to signal GameEngine from the UI / Dialog elements, inderectly
    // I'm not comfortable having the UI reach through the activity -> fragment -> engine -> controls.
    private void updateButtonStates() {
        final ImageView btnMusic = (ImageView) findViewById(R.id.btn_music);
        btnMusic.setImageResource((musicEnabled) ? R.drawable.music_on_no_bg : R.drawable.music_off_no_bg);
        final ImageView btnSounds = (ImageView) findViewById(R.id.btn_sound);
        btnSounds.setImageResource((soundEnabled) ? R.drawable.sounds_on_no_bg : R.drawable.sounds_off_no_bg);
        final ImageView motionControls = (ImageView) findViewById(R.id.btn_accelerometer);
        motionControls.setImageResource((allowMotionControl) ? R.drawable.ic_screen_rotation : R.drawable.ic_screen_lock_rotation);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        if (id == R.id.btn_sound) {
            soundEnabled = !soundEnabled;
            mPrefs.edit().putBoolean(Jukebox.SOUNDS_PREF_KEY, soundEnabled).apply();
        }
        else if (id == R.id.btn_music) {
            musicEnabled = !musicEnabled;
            mPrefs.edit().putBoolean(Jukebox.MUSIC_PREF_KEY, musicEnabled).apply();
        }
        else if (id == R.id.btn_resume) {
            dismiss();
        }else if(id == R.id.btn_accelerometer){
            allowMotionControl = !allowMotionControl;
            mPrefs.edit().putBoolean(ConfigurableGameInput.ACCELEROMETER_PREF_KEY, allowMotionControl).apply();
        }
        updateButtonStates();
        if (id == R.id.btn_exit) {
            super.dismiss();
            mListener.exitGame();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mListener.resumeGame();
    }

    public interface PauseDialogListener {
        void exitGame();
        void resumeGame();
    }
}
