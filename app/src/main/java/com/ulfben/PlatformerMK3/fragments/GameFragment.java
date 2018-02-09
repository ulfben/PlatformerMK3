package com.ulfben.PlatformerMK3.fragments;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.engine.GameView;
import com.ulfben.PlatformerMK3.engine.Jukebox;
import com.ulfben.PlatformerMK3.gui.PauseDialog;
import com.ulfben.PlatformerMK3.input.ConfigurableGameInput;
import com.ulfben.PlatformerMK3.utilities.Utils;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class GameFragment extends BaseFragment implements PauseDialog.PauseDialogListener, GameEngine.EngineListener {
    private static final String TAG = "GameFragment";
    private GameEngine mGameEngine = null;
    private SharedPreferences mPrefs;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private boolean allowMotionControl = true;
    private float metersToShow = 9;
    private Toast mCurrentToast = null;

    public GameFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setHasOptionsMenu(true); //let the system know that we like to add items to the AppBar
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_game, container, false);
    }
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        final GameView gameView = view.findViewById(R.id.gameView);
        if(mGameEngine != null){
            Log.w(TAG, "Fragment re-created without cleanup!"); //if this ever happens I want to know about it.
            mGameEngine.onDestroy();
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getMainActivity().getApplicationContext());
        mGameEngine = new GameEngine(getMainActivity(), gameView, this);
        getMainActivity().showActionBar();
        loadPreferences();
        getMainActivity().hideSystemUI();
    }

    @Override
    public void onEngineReady() {
        Log.d(TAG, "onEngineReady");
        mGameEngine.loadLevel("TestLevel");
        mGameEngine.startGame();
    }

    //I keep these around to populate the app bar with
    private void loadPreferences(){
        metersToShow = mPrefs.getFloat(GameEngine.METERS_TO_SHOW_PREF_KEY, 9.0f);
        musicEnabled = mPrefs.getBoolean(Jukebox.MUSIC_PREF_KEY, true);
        soundEnabled = mPrefs.getBoolean(Jukebox.SOUNDS_PREF_KEY, true);
        allowMotionControl = mPrefs.getBoolean(ConfigurableGameInput.ACCELEROMETER_PREF_KEY, true);
    }
    private void updatePreferences(){
        metersToShow = Utils.clamp(metersToShow, 1f, 255f);
        mPrefs.edit().putFloat(GameEngine.METERS_TO_SHOW_PREF_KEY, metersToShow)
                .putBoolean(Jukebox.SOUNDS_PREF_KEY, soundEnabled)
                .putBoolean(Jukebox.MUSIC_PREF_KEY, musicEnabled)
                .putBoolean(ConfigurableGameInput.ACCELEROMETER_PREF_KEY, allowMotionControl).apply();
        mGameEngine.onSharedPreferenceChange();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.action_bar, menu);
    }

    //TODO: color icons on app bar (iconTint, tint something something)
    // TODO: check appbar when going between startmenu / fragment
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        super.onPrepareOptionsMenu(menu);
        //menu.findItem().getIcon().setColorFilter()
        /*menu.findItem(R.id.musicToggle).setOnMenuItemClickListener(
            new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(final MenuItem menuItem) {
                    return false;
                }
        });*/
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        final MainActivity a = getMainActivity();
        switch (item.getItemId()) {
            case R.id.musicToggle:
                musicEnabled = !musicEnabled;
                showToast("Music: " + musicEnabled, Toast.LENGTH_SHORT);
                updatePreferences();
                return true;
            case R.id.sfxToggle:
                soundEnabled = !soundEnabled;
                showToast("SFX: "+soundEnabled, Toast.LENGTH_SHORT);
                updatePreferences();
                return true;
            case R.id.rotationToggle:
                allowMotionControl = !allowMotionControl;
                showToast("Motion controls: "+allowMotionControl, Toast.LENGTH_SHORT);
                updatePreferences();
                return true;
            case R.id.zoomIn:
                if(metersToShow > 1){ metersToShow--; }
                showToast("Showing: "+metersToShow+" meters", Toast.LENGTH_SHORT);
                updatePreferences();
                return true;
            case R.id.zoomOut:
                if(metersToShow < 20){ metersToShow++; }
                showToast("Showing: "+metersToShow+" meters", Toast.LENGTH_SHORT);
                updatePreferences();
                return true;
            case R.id.screenLockLandscape:
                showToast("Lock landscape!", Toast.LENGTH_SHORT);
                if(a != null) { a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); }
                return true;
            case R.id.screenLockPortrait:
                showToast("Lock portrait!", Toast.LENGTH_SHORT);
                if(a != null) { a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); }
                return true;
        }
        return false;
    }

    @SuppressLint("ShowToast")
    private void showToast(final String text, final int duration){
        if(mCurrentToast == null){
            mCurrentToast = Toast.makeText(getMainActivity(), text, duration);
        }else {
            mCurrentToast.setText(text);
        }
        mCurrentToast.setDuration(duration);
        mCurrentToast.show();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        if (mGameEngine.isRunning()){
            pauseGameAndShowPauseDialog();
        }
        super.onPause();
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
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
