package com.ulfben.PlatformerMK3.fragments;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.engine.GameView;
import com.ulfben.PlatformerMK3.engine.Jukebox;
import com.ulfben.PlatformerMK3.gui.PauseDialog;
import com.ulfben.PlatformerMK3.input.Accelerometer;
import com.ulfben.PlatformerMK3.input.CompositeControl;
import com.ulfben.PlatformerMK3.input.Gamepad;
import com.ulfben.PlatformerMK3.input.VirtualJoystick;
import com.ulfben.PlatformerMK3.utilities.SysUtils;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class GameFragment extends BaseFragment implements View.OnClickListener, PauseDialog.PauseDialogListener {
    private static final String TAG = "GameFragment";
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

    @Override
    public boolean onBackPressed() {
        if (mGameEngine.isRunning()) {
            pauseGameAndShowPauseDialog();
            return true;
        }
        return false;
    }
    private void pauseGameAndShowPauseDialog() {
        mGameEngine.pauseGame();
        final PauseDialog dialog = new PauseDialog(getMainActivity());
        dialog.setListener(this);
        showDialog(dialog);
        updatePauseButton();
    }

    public Jukebox getJukebox(){
        return mGameEngine.getJukebox();
    }

    private void updatePauseButton(){
        final View view = getView();
        if(view == null){
            Log.e(TAG, "View not available!");
            return;
        }
        final Button button = (Button) view.findViewById(R.id.btn_play_pause);
        if (mGameEngine.isPaused()) {
            button.setText(R.string.pause);
        } else {
            button.setText(R.string.resume);
        }
    }

    public void resumeGame() {
        mGameEngine.resumeGame();
        updatePauseButton();
    }

    @Override
    public void exitGame() {
        mGameEngine.stopGame();
        getMainActivity().navigateBack();
    }
}
