package com.ulfben.PlatformerMK3.fragments;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.gui.Dialog;

//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class BaseFragment extends Fragment {
    Dialog mCurrentDialog = null;

    public void showDialog (final Dialog newDialog) {
        showDialog(newDialog, false);
    }

    public void showDialog (final Dialog newDialog, final boolean dismissOther) {
        if(mCurrentDialog != null && mCurrentDialog.isShowing()) {
            if(dismissOther) {
                mCurrentDialog.dismiss();
            } else {
                return;
            }
        }
        mCurrentDialog = newDialog;
        mCurrentDialog.show();
    }

    public boolean onBackPressed() {
        if (mCurrentDialog != null && mCurrentDialog.isShowing()) {
            mCurrentDialog.dismiss();
            return true;
        }
        return false;
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
