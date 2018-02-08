package com.ulfben.PlatformerMK3;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.ulfben.PlatformerMK3.fragments.BaseFragment;
import com.ulfben.PlatformerMK3.fragments.GameFragment;
import com.ulfben.PlatformerMK3.fragments.MainMenuFragment;
import com.ulfben.PlatformerMK3.input.GamepadListener;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-01.

public class MainActivity extends AppCompatActivity implements InputManager.InputDeviceListener {
    private static final String TAG = "MainActivity";
    private static final String FRAGMENT_TAG = "platformermk3";
    private GamepadListener mGamepadListener = null;
    private boolean mHadGamepad = false;
    private boolean mShowGamepadHelp = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final InputManager inputManager = (InputManager) getSystemService(Context.INPUT_SERVICE);
            if(inputManager != null) {
                inputManager.registerInputDeviceListener(this, null);
            }
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new MainMenuFragment(), FRAGMENT_TAG)
                    .commit();
        }
        Toolbar toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        hideActionBar(); //only show the actionbar during game play
        hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final InputManager inputManager = (InputManager) getSystemService(Context.INPUT_SERVICE);
            if(inputManager != null) {
                inputManager.unregisterInputDeviceListener(this);
            }
        }
        super.onDestroy();
    }

    public void showActionBar(){
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.show();
        }
    }

    public void hideActionBar(){
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.hide();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    public void startGame() {
        navigateToFragment(new GameFragment(), FRAGMENT_TAG);
    }

    private void navigateToFragment(final BaseFragment dst, final String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, dst, tag)
                .addToBackStack(null)
                .commit();
    }

    public void setGamepadListener(final GamepadListener listener) {
        mGamepadListener = listener;
    }

    @Override
    public boolean dispatchGenericMotionEvent(final MotionEvent ev) {
        return mGamepadListener != null && mGamepadListener.dispatchGenericMotionEvent(ev) || super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public void onBackPressed() {
        final BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null || !fragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public void navigateBack() {
        super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent ev) {
        if(mGamepadListener != null){
            if(mGamepadListener.dispatchKeyEvent(ev)){
                return true;
            }
        }
        return super.dispatchKeyEvent(ev);
    }

    private boolean isGameControllerConnected() {
        final int[] deviceIds = InputDevice.getDeviceIds();
        for(final int deviceId : deviceIds) {
            final InputDevice device = InputDevice.getDevice(deviceId);
            final int sources = device.getSources();
            if(((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                    ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isGameControllerConnected()){
            mHadGamepad = true;
            displayGamepadHelp();
        }
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            return;
        }
        hideSystemUI();
    }

    public void hideSystemUI() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }else { //less than KITKAT
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
            );
        }
    }

    @Override
    public void onInputDeviceAdded(final int deviceId) {
        if(isGameControllerConnected()){
            mHadGamepad = true;
            pauseOnGamepadChanges();
            displayGamepadHelp();
        }
    }

    @Override
    public void onInputDeviceRemoved(final int deviceId) {
        if(mHadGamepad && !isGameControllerConnected()){
            mHadGamepad = false;
            pauseOnGamepadChanges();
            Toast.makeText(this, "Gamepad Removed!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInputDeviceChanged(final int deviceId) {
        //Toast.makeText(this, "Input Device Changed!", Toast.LENGTH_LONG).show();
        //not sure how to deal with this, so I'll just sink it.
    }

    private void pauseOnGamepadChanges(){
        try {
            final GameFragment fragment = (GameFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            fragment.pauseGameAndShowPauseDialog();
        }catch(final ClassCastException e){
            //game is not running yet
        }
    }

    private void displayGamepadHelp() {
        if(!mShowGamepadHelp){
            Toast.makeText(this, "Gamepad detected!", Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.gampad_help_title)
                .setMessage(R.string.gamepad_help_message)
                .create()
                .show();
        mShowGamepadHelp = false; //only show help once.
    }

}
