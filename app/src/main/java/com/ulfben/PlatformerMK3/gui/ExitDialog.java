package com.ulfben.PlatformerMK3.gui;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-05.

import android.view.View;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
public class ExitDialog extends Dialog implements View.OnClickListener {
    private ExitDialogListener mListener;

    public ExitDialog(final MainActivity activity) {
        super(activity);
        setContentView(R.layout.dialog_exit);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnFocusChangeListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnFocusChangeListener(this);
    }

    public void setListener(final ExitDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        if (id == R.id.btn_exit) {
            dismiss();
            mListener.exit();
        } else if (id == R.id.btn_resume) {
            dismiss();
        }
    }

    public interface ExitDialogListener {
        void exit();
    }
}
