package com.ulfben.PlatformerMK3.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.Jukebox;
import com.ulfben.PlatformerMK3.gui.QuitDialog;

public class MainMenuFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG = "MainMenuFragment";
    private static final String PREF_SHOULD_DISPLAY_GAMEPAD_HELP = "com.ulfben.platformermk3.gamepad.help.boolean";

    public MainMenuFragment() {
        super();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_start).setOnClickListener(this);
        view.findViewById(R.id.btn_sound).setOnClickListener(this);
        view.findViewById(R.id.btn_music).setOnClickListener(this);
        updateSoundAndMusicButtons();
    }

    private void updateSoundAndMusicButtons() {
        final Jukebox jukebox = getMainActivity().getJukebox();
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

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_start){
            ((MainActivity) getActivity()).startGame();
        }
        final Jukebox jukebox = getMainActivity().getJukebox();
        if(jukebox == null){
            Log.d(TAG, "audio not available"); //TODO: set icons correctly first.
            return;
        }
        if (id == R.id.btn_music) {
            jukebox.toggleMusicStatus();
        }else if (id == R.id.btn_sound) {
            jukebox.toggleSoundStatus();
        }
        updateSoundAndMusicButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGameControllerConnected() && shouldDisplayGamepadHelp()) {
            displayGamepadHelp();
            PreferenceManager.getDefaultSharedPreferences(getActivity()) // Do not show the dialog again
                    .edit()
                    .putBoolean(PREF_SHOULD_DISPLAY_GAMEPAD_HELP, false)
                    .commit();
        }
    }

    private void displayGamepadHelp() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.gampad_help_title)
                .setMessage(R.string.gamepad_help_message)
                .create()
                .show();
    }

    private boolean shouldDisplayGamepadHelp() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(PREF_SHOULD_DISPLAY_GAMEPAD_HELP, true);
    }

    public boolean isGameControllerConnected() {
        final int[] deviceIds = InputDevice.getDeviceIds();
        for (final int deviceId : deviceIds) {
            final InputDevice dev = InputDevice.getDevice(deviceId);
            final int sources = dev.getSources();
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                    ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        final boolean consumed = super.onBackPressed();
        if (!consumed){
            final QuitDialog quitDialog = new QuitDialog(getMainActivity());
            quitDialog.setListener(this);
            showDialog(quitDialog);
            return true;
        }
        return consumed;
    }

    @Override
    public void exit() {
        getMainActivity().finish();
    }
}
