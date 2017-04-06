package com.ulfben.PlatformerMK3.gui;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-05.

import android.view.View;

import com.ulfben.PlatformerMK3.MainActivity;
import com.ulfben.PlatformerMK3.R;
public class QuitDialog extends Dialog implements View.OnClickListener {
        private QuitDialogListener mListener;

        public QuitDialog(final MainActivity activity) {
            super(activity);
            setContentView(R.layout.dialog_quit);
            findViewById(R.id.btn_exit).setOnClickListener(this);
            findViewById(R.id.btn_resume).setOnClickListener(this);
        }

        public void setListener(final QuitDialogListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(final View v) {
            if (v.getId() == R.id.btn_exit) {
                dismiss();
                mListener.exit();
            }
            else if (v.getId() == R.id.btn_resume) {
                dismiss();
            }
        }

        public interface QuitDialogListener {
            void exit();
        }
    }
}
