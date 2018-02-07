package com.ulfben.PlatformerMK3.fragments;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.engine.GameView;
import com.ulfben.PlatformerMK3.gui.PauseDialog;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class GameFragment extends BaseFragment implements PauseDialog.PauseDialogListener {
    private static final String TAG = "GameFragment";
    private GameEngine mGameEngine = null;

    public GameFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); //let the system know that we like to add items to the AppBar
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)  {
        inflater.inflate(R.menu.action_bar, menu);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.musicToggle).setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(final MenuItem menuItem) {
                    Toast.makeText(getMainActivity(), "tapped music thing", Toast.LENGTH_SHORT).show();
                    return false;
                }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.musicToggle:
                Log.d(TAG, "tapped musictoggle!");
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGameEngine.loadLevel("TestLevel"); //creates mLevel and mCamera
        mGameEngine.startGame();
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
        mGameEngine.onPause();
        final PauseDialog dialog = new PauseDialog(getMainActivity(), this);
        showDialog(dialog);
    }
    //Listeners for the PauseDialog
    @Override
    public void resumeGame() {
        mGameEngine.onResume();
    }
    @Override
    public void exitGame() {
        mGameEngine.onStop();
        getMainActivity().navigateBack();
    }
}
