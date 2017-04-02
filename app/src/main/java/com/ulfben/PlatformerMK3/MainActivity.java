package com.ulfben.PlatformerMK3;

import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.ulfben.PlatformerMK3.engine.GameEngine;
import com.ulfben.PlatformerMK3.engine.GameView;
import com.ulfben.PlatformerMK3.input.Accelerometer;
import com.ulfben.PlatformerMK3.input.CompositeControl;
import com.ulfben.PlatformerMK3.input.Gamepad;
import com.ulfben.PlatformerMK3.input.GamepadListener;
import com.ulfben.PlatformerMK3.input.VirtualJoystick;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-01.
public class MainActivity extends AppCompatActivity implements InputManager.InputDeviceListener {
    private GameEngine mGameEngine = null;
    private GamepadListener mGamepadListener = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        hideSystemUI();
        setContentView(R.layout.activity_main);
        final GameView gameView = (GameView) findViewById(R.id.gameView);
        mGameEngine = new GameEngine(this, gameView);
        final CompositeControl control = new CompositeControl(
                //new VirtualKeypad(findViewById(R.id.keypad))
                new VirtualJoystick(findViewById(R.id.virtual_joystick)),
                new Gamepad(this)
        );
        if(!SysUtils.isProbablyEmulator()){
            //my emulator defaults to a slight slant, so the accelerometer controls drives me nuts.
            control.addInput(new Accelerometer(this));
        }
        mGameEngine.setInputManager(control);
        mGameEngine.loadLevel("TestLevel");
    }

    public void setGamepadListener(final GamepadListener listener) {
        mGamepadListener = listener;
    }

    @Override
    public boolean dispatchGenericMotionEvent(final MotionEvent ev) {
        if(mGamepadListener != null){
            if(mGamepadListener.dispatchGenericMotionEvent(ev)){
                return true;
            }
        }
        return super.dispatchGenericMotionEvent(ev);
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
    protected void onPause() {
        super.onPause();
        if (mGameEngine.isRunning()){
            pauseGameAndShowPauseDialog();
        }
    }

    private void pauseGameAndShowPauseDialog() {
        if (mGameEngine.isPaused()) {
            return;
        }
        mGameEngine.pauseGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isGameControllerConnected()){
            Toast.makeText(this, "Gamepad detected!", Toast.LENGTH_LONG).show();
        }
        mGameEngine.resumeGame();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGameEngine.startGame();
        mGameEngine.pauseGame();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameEngine.stopGame();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            return;
        }
        hideSystemUI();
    }


    private void hideSystemUI() {
        final View decorView = getWindow().getDecorView();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }else { //less than KITKAT
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
            );
        }
    }

    @Override
    public void onInputDeviceAdded(final int deviceId) {
        Toast.makeText(this, "Input Device Added!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInputDeviceRemoved(final int deviceId) {
        //probably pause the game and show some dialog?
        Toast.makeText(this, "Input Device Removed!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInputDeviceChanged(final int deviceId) {
        Toast.makeText(this, "Input Device Changed!", Toast.LENGTH_LONG).show();
    }
}
