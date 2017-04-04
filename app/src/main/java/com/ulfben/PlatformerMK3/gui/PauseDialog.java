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
        updateSoundAndMusicButtons();
    }

    private void updateSoundAndMusicButtons() {
        final Jukebox jukebox = mParent.getJukebox();
        if(jukebox == null){
            Log.d(TAG, "audio not available"); //TODO: set icons correctly first.
            return;
        }
        final boolean music = jukebox.ismMusicEnabled();
        final ImageView btnMusic = (ImageView) findViewById(R.id.btn_music);
        if (music) {
            btnMusic.setImageResource(R.drawable.music_on_no_bg);
        } else {
            btnMusic.setImageResource(R.drawable.music_off_no_bg);
        }
        final boolean sound = jukebox.isSoundEnabled();
        final ImageView btnSounds = (ImageView) findViewById(R.id.btn_sound);
        if (sound) {
            btnSounds.setImageResource(R.drawable.sounds_on_no_bg);
        } else {
            btnSounds.setImageResource(R.drawable.sounds_off_no_bg);
        }
    }

    public void setListener(final PauseDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(final View v) {
        final Jukebox jukebox = mParent.getJukebox();
        if (v.getId() == R.id.btn_sound && jukebox != null) {
            jukebox.toggleSoundStatus();
            updateSoundAndMusicButtons();
        }
        else if (v.getId() == R.id.btn_music && jukebox != null) {
            jukebox.toggleMusicStatus();
            updateSoundAndMusicButtons();
        }
        else if (v.getId() == R.id.btn_exit) {
            super.dismiss();
            mListener.exitGame();
        }
        else if (v.getId() == R.id.btn_resume) {
            dismiss();
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
