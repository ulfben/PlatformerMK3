package com.ulfben.PlatformerMK3.fragments;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.gui.ExitDialog;

public class MainMenuFragment extends BaseFragment implements View.OnClickListener,  ExitDialog.ExitDialogListener{
    private static final String TAG = "MainMenuFragment";

    public MainMenuFragment() {
        super();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        setHasOptionsMenu(true); //let the system know that we like to add items to the AppBar
        return inflater.inflate(R.layout.fragment_main_menu, container, false);
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_start).setOnClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.start_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        if (id == R.id.btn_start){
            getMainActivity().startGame();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        if(item.getItemId() == R.id.startGame) {
            getMainActivity().startGame();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        boolean consumed = super.onBackPressed();
        if(!consumed){
            final ExitDialog exitDialog = new ExitDialog(getMainActivity(), this);
            showDialog(exitDialog);
        }
        return true;
    }

    @Override
    public void exit() {
        getMainActivity().finish();
    }
}
