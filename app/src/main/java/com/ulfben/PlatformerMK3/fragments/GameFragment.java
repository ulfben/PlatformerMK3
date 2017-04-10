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
import com.ulfben.PlatformerMK3.input.ConfigurableGameInput;
import com.ulfben.PlatformerMK3.input.Gamepad;
import com.ulfben.PlatformerMK3.input.VirtualJoystick;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class GameFragment extends BaseFragment implements View.OnClickListener, PauseDialog.PauseDialogListener {
    private static final String TAG = "GameFragment";
    private GameEngine mGameEngine;
    private ConfigurableGameInput mControls;
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
        mControls = new ConfigurableGameInput(activity,
                //new VirtualKeypad(findViewById(R.id.keypad)),
                new VirtualJoystick(view.findViewById(R.id.virtual_joystick)),
                new Gamepad(activity),
                new Accelerometer(activity)
        );
        mGameEngine.setGameInput(mControls);
        mGameEngine.loadLevel("TestLevel");
        mGameEngine.startGame();
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.btn_play_pause) {
            if (mGameEngine.isRunning()) {
                pauseGameAndShowPauseDialog();
            }
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
        mGameEngine.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        boolean consumed = super.onBackPressed();
        if(!consumed) {
            if (mGameEngine.isRunning()) {
                pauseGameAndShowPauseDialog();
                consumed = true;
            }
        }
        return consumed;
    }
    public void pauseGameAndShowPauseDialog() {
        mGameEngine.pauseGame();
        final PauseDialog dialog = new PauseDialog(getMainActivity());
        dialog.setListener(this);
        showDialog(dialog);
        updatePauseButton();
    }

    public Jukebox getJukebox(){
        return mGameEngine.getJukebox();
    }

    public boolean toggleMotionControl(){
        return mControls.toggleMotionControl();
    }

    public boolean hasMotionControl(){
        return mControls.hasMotionControl();
    }

    private void updatePauseButton(){
        final View view = getView();
        if(view == null){
            Log.e(TAG, "View not available!");
            return;
        }
        final Button button = (Button) view.findViewById(R.id.btn_play_pause);
        if (mGameEngine.isPaused()) {
            button.setText(R.string.resume);
        } else {
            button.setText(R.string.pause);
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
