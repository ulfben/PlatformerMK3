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
    private GameEngine mGameEngine = null;

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
        final GameView gameView = view.findViewById(R.id.gameView);
        if(mGameEngine != null){
            Log.w(TAG, "Fragment re-created without cleanup!"); //if this ever happens I want to know about it.
            mGameEngine.onDestroy();
        }
        mGameEngine = new GameEngine(getMainActivity(), gameView);
        view.findViewById(R.id.btn_play_pause).setOnClickListener(this);
        mGameEngine.startGame();
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.btn_play_pause && mGameEngine.isRunning()) {
            pauseGameAndShowPauseDialog();
        }
    }
    @Override
    public void onPause() {
        if (mGameEngine.isRunning()){
            pauseGameAndShowPauseDialog();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mGameEngine.stopGame();
        mGameEngine.onDestroy();
        super.onDestroy();
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
        final PauseDialog dialog = new PauseDialog(getMainActivity(), this);
        showDialog(dialog);
        updatePauseButton();
    }

    // Q&D: ugly hack to let UI buttons and dialog boxes read/write game settings.
    // TODO: figure out how to signal GameEngine from the UI and PauseDialog elements, indirectly
    // I'm not comfortable having the UI reach through the  fragment -> engine -> controls.
    public Jukebox getJukebox(){
        return mGameEngine.getJukebox();
    }
    public boolean toggleMotionControl(){
        return mGameEngine.toggleMotionControl();
    }
    public boolean hasMotionControl(){
        return mGameEngine.hasMotionControl();
    }

    private void updatePauseButton(){
        final View view = getView();
        if(view == null){
            Log.i(TAG, "View not available!");
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
