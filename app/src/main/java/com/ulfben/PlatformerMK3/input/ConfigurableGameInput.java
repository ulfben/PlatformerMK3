package com.ulfben.PlatformerMK3.input;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

//Created by Ulf Benjaminsson (ulfben) on 2017-04-09.
//extending CompositeGameInput to add a few features (persistent settings, removing inputs at runtime)
// without ruining the relatively clean base class.
public class ConfigurableGameInput extends CompositeGameInput {
    private static final String ACCELEROMETER_PREF_KEY = "accelerometer_pref_key";
    private Context mContext = null;
    private boolean mAllowMotionControl;
    private Accelerometer mMotionControl = null;
    public ConfigurableGameInput(final Context context, final GameInput... inputs) {
        super(inputs);
        mContext = context;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        mAllowMotionControl = prefs.getBoolean(ACCELEROMETER_PREF_KEY, true);
        if(!mAllowMotionControl){
            removeAccelerometer();
        }
    }

    public boolean hasMotionControl(){
        return mAllowMotionControl;
    }

    public boolean toggleMotionControl(){
        mAllowMotionControl = !mAllowMotionControl;
        if(!mAllowMotionControl){
            removeAccelerometer();
        }else if(mMotionControl != null){
            super.addInput(mMotionControl);
        }
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit().putBoolean(ACCELEROMETER_PREF_KEY, mAllowMotionControl)
                .apply();
        return mAllowMotionControl;
    }

    public void removeAccelerometer(){
        for(final GameInput im : mInputs){
            if(Accelerometer.class.isInstance(im)){
                mMotionControl = (Accelerometer) im;
                removeInput(im);
                break;
            }
        }
    }

    public void removeInput(final GameInput im){
        synchronized (mInputs) {
            if (mInputs.remove(im)) {
                im.onPause();
                im.onStop();
            }
            refresh();
        }
    }
}
