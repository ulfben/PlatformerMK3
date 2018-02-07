package com.ulfben.PlatformerMK3.fragments;
import android.support.v4.app.Fragment;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.gui.Dialog;

//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class BaseFragment extends Fragment {
    private Dialog mCurrentDialog = null;

    void showDialog(final Dialog newDialog) {
        showDialog(newDialog, false);
    }

    private void showDialog(final Dialog newDialog, final boolean dismissOther) {
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

    MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
