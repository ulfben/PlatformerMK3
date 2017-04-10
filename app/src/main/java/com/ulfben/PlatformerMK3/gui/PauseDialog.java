package com.ulfben.PlatformerMK3.gui;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.Jukebox;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-04.

public class PauseDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "PauseDialog";
    private PauseDialogListener mListener;

    public PauseDialog(final MainActivity activity) {
        super(activity);
        setContentView(R.layout.dialog_pause);
        findViewById(R.id.btn_music).setOnClickListener(this);
        findViewById(R.id.btn_sound).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        findViewById(R.id.btn_accelerometer).setOnClickListener(this);
        updateButtonStates();
    }

    private void updateButtonStates() {
        final Jukebox jukebox = mParent.getJukebox();
        boolean musicEnabled = false;
        boolean sfxEnabled = false;
        if(jukebox != null){
            musicEnabled = jukebox.ismMusicEnabled();
            sfxEnabled = jukebox.isSoundEnabled();
        }else{
            Log.d(TAG, "audio not available");
        }
        final ImageView btnMusic = (ImageView) findViewById(R.id.btn_music);
        if (musicEnabled) {
            btnMusic.setImageResource(R.drawable.music_on_no_bg);
        } else {
            btnMusic.setImageResource(R.drawable.music_off_no_bg);
        }
        final ImageView btnSounds = (ImageView) findViewById(R.id.btn_sound);
        if (sfxEnabled) {
            btnSounds.setImageResource(R.drawable.sounds_on_no_bg);
        } else {
            btnSounds.setImageResource(R.drawable.sounds_off_no_bg);
        }
        final ImageView motionControls = (ImageView) findViewById(R.id.btn_accelerometer);
        if(mParent.hasMotionControl()){
            motionControls.setImageResource(R.drawable.ic_screen_rotation_black_24dp);
        }else{
            motionControls.setImageResource(R.drawable.ic_screen_lock_rotation_black_24dp);
        }
    }

    public void setListener(final PauseDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(final View v) {
        final Jukebox jukebox = mParent.getJukebox();
        final int id = v.getId();
        if (id == R.id.btn_sound && jukebox != null) {
            jukebox.toggleSoundStatus();
        }
        else if (id == R.id.btn_music && jukebox != null) {
            jukebox.toggleMusicStatus();
        }
        else if (id == R.id.btn_resume) {
            dismiss();
        }else if(id == R.id.btn_accelerometer){
            mParent.toggleMotionControl();
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
