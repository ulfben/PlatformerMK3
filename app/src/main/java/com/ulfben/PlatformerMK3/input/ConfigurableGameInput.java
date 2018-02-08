package com.ulfben.PlatformerMK3.input;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

//Created by Ulf Benjaminsson (ulfben) on 2017-04-09.
//extending CompositeGameInput to add a few features (persistent settings, removing inputs at runtime)
// without ruining the relatively clean base class.
public class ConfigurableGameInput extends CompositeGameInput {
    public static final String ACCELEROMETER_PREF_KEY = "accelerometer_pref_key";
    private Accelerometer mMotionControl = null;
    private final SharedPreferences mPrefs;
    public ConfigurableGameInput(final Context context, final GameInput... inputs) {
        super(inputs);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        reloadAndApplySettings();
    }

    public void reloadAndApplySettings(){
        final boolean allowMotionControl = mPrefs.getBoolean(ACCELEROMETER_PREF_KEY, true);
        if(!allowMotionControl){
            removeAccelerometer();
        }else if(mMotionControl != null){
            super.addInput(mMotionControl);
            mMotionControl.onResume();
        }
    }

    private void removeAccelerometer(){
        for(final GameInput im : mInputs){
            if(Accelerometer.class.isInstance(im)){
                mMotionControl = (Accelerometer) im;
                removeInput(im);
                break;
            }
        }
    }

    private void removeInput(final GameInput im){
        synchronized (mInputs) {
            if (mInputs.remove(im)) {
                im.onPause();
                im.onStop();
            }
            refresh();
        }
    }
}
