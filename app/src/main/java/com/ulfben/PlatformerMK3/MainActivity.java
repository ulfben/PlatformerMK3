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

import com.ulfben.PlatformerMK3.fragments.BaseFragment;
import com.ulfben.PlatformerMK3.fragments.GameFragment;
import com.ulfben.PlatformerMK3.fragments.MainMenuFragment;
import com.ulfben.PlatformerMK3.input.GamepadListener;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-01.

public class MainActivity extends AppCompatActivity implements InputManager.InputDeviceListener {
    private static final String TAG_FRAGMENT = "content";
    private GamepadListener mGamepadListener = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        hideSystemUI();
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainMenuFragment(), TAG_FRAGMENT)
                    .commit();
        }
    }

    public void startGame() {
        // Navigate the the game fragment, which makes the start automatically
        navigateToFragment(new GameFragment());
    }

    private void navigateToFragment(final BaseFragment dst) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, dst, TAG_FRAGMENT)
                .addToBackStack(null)
                .commit();
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
    public void onBackPressed() {
        final BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
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
    protected void onResume() {
        super.onResume();
        if(isGameControllerConnected()){
            Toast.makeText(this, "Gamepad detected!", Toast.LENGTH_LONG).show();
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
