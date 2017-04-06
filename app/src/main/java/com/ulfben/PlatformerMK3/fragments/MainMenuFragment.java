package com.ulfben.PlatformerMK3.fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.gui.ExitDialog;

public class MainMenuFragment extends BaseFragment implements View.OnClickListener, ExitDialog.ExitDialogListener{
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
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        if (id == R.id.btn_start){
            ((MainActivity) getActivity()).startGame();
        }
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
        for(final int deviceId : deviceIds) {
            final InputDevice dev = InputDevice.getDevice(deviceId);
            final int sources = dev.getSources();
            if(((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) ||
                    ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        final boolean consumed = super.onBackPressed();
        if(!consumed){
            Log.d(TAG, "exit game?");
            final ExitDialog exitDialog = new ExitDialog(getMainActivity());
            exitDialog.setListener(this);
            showDialog(exitDialog);
            return true;
        }
        return consumed;
    }

    @Override
    public void exit() {
        getMainActivity().finish();
    }
}
