package com.ulfben.PlatformerMK3.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
import com.ulfben.PlatformerMK3.gui.ExitDialog;

public class MainMenuFragment extends BaseFragment implements View.OnClickListener, ExitDialog.ExitDialogListener{
    private static final String TAG = "MainMenuFragment";

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
        view.findViewById(R.id.btn_exit).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        if (id == R.id.btn_start){
            ((MainActivity) getActivity()).startGame();
        }else if(id == R.id.btn_exit){
            onBackPressed();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        boolean consumed = super.onBackPressed();
        if(!consumed){
            final ExitDialog exitDialog = new ExitDialog(getMainActivity(), this);
            showDialog(exitDialog);
            consumed = true;
        }
        return consumed;
    }

    @Override
    public void exit() {
        getMainActivity().finish();
    }
}
