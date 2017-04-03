package com.ulfben.PlatformerMK3.fragments;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.engine.GameView;
import com.ulfben.PlatformerMK3.input.Accelerometer;
import com.ulfben.PlatformerMK3.input.CompositeControl;
import com.ulfben.PlatformerMK3.input.Gamepad;
import com.ulfben.PlatformerMK3.input.VirtualJoystick;
import com.ulfben.PlatformerMK3.utilities.SysUtils;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class GameFragment extends BaseFragment implements View.OnClickListener {
    private GameEngine mGameEngine;

    public GameFragment() {
        super();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final GameView gameView = (GameView) view.findViewById(R.id.gameView);
        final MainActivity activity = (MainActivity) getActivity();
        mGameEngine = new GameEngine(activity, gameView);
        view.findViewById(R.id.btn_play_pause).setOnClickListener(this);
        final CompositeControl control = new CompositeControl(
                //new VirtualKeypad(findViewById(R.id.keypad))
                new VirtualJoystick(view.findViewById(R.id.virtual_joystick)),
                new Gamepad(activity)
        );
        if(!SysUtils.isProbablyEmulator()){
            //my emulator defaults to a slight slant, so the accelerometer controls drives me nuts.
            control.addInput(new Accelerometer(activity));
        }
        mGameEngine.setInputManager(control);
        mGameEngine.loadLevel("TestLevel");
        mGameEngine.startGame();
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.btn_play_pause) {
            pauseGameAndShowPauseDialog();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGameEngine.isRunning()){
            pauseGameAndShowPauseDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGameEngine.stopGame();
    }

    public boolean onBackPressed() {
        if (mGameEngine.isRunning()) {
            pauseGameAndShowPauseDialog();
            return true;
        }
        return false;
    }
    private void pauseGameAndShowPauseDialog() {
        mGameEngine.pauseGame();
        final Handler handler = new Handler();
        final int delay = 250; //ms
        final Runnable delayedResume = new Runnable() { //give the dialog time to go away, before we onResume
            @Override
            public void run() {
                mGameEngine.resumeGame();
            }
        };
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pause_dialog_title)
                .setMessage(R.string.pause_dialog_message)
                .setPositiveButton(R.string.resume, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        handler.postDelayed(delayedResume, delay);
                    }
                })
                .setNegativeButton(R.string.stop, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                        mGameEngine.stopGame();
                        ((MainActivity) getActivity()).navigateBack();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        handler.postDelayed(delayedResume, delay);
                    }
                })
                .create()
                .show();

    }

    private void playOrPause() {
        final Button button = (Button) getView().findViewById(R.id.btn_play_pause);
        if (mGameEngine.isPaused()) {
            mGameEngine.resumeGame();
            button.setText(R.string.pause);
        }
        else {
            mGameEngine.pauseGame();
            button.setText(R.string.resume);
        }
    }
}
