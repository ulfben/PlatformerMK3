package com.ulfben.PlatformerMK3.input;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-11.

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.Surface;

import com.ulfben.PlatformerMK3.MainActivity;

public class Accelerometer extends InputManager {
    private static final String TAG = "Accelerometer";
    private static final float DEGREES_PER_RADIAN = (float) (180d / Math.PI); //~  57.2957795f;
    private static final int LENGTH = 3; //azimuth, pitch and roll
    private static final float MAX_ANGLE = 30f;
    private static final float SHAKE_THRESHOLD = 4.25f; // m/S^2
    private static final long SHAKE_COOLDOWN = 300;//ms
    private long mLastShake = 0;
    private final Activity mActivity;
    private final Display mDisplay; //to track orientation
    private final float[] mRotationMatrix = new float[4*4];
    private final float[] mOrientation = new float[LENGTH];
    private final float[] mLastMagFields = new float[LENGTH];
    private final float[] mLastAccels = new float[LENGTH];

    private SensorEventListener mMagneticListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent event) {
            System.arraycopy(event.values, 0, mLastMagFields, 0, LENGTH);
        }

        @Override
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        }
    };

    private SensorEventListener mAccelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent event) {
            System.arraycopy(event.values, 0, mLastAccels, 0, LENGTH);
        }

        @Override
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        }
    };

    public Accelerometer(final MainActivity activity) {
        super();
        mActivity = activity;
        mDisplay = mActivity.getWindowManager().getDefaultDisplay();
    }

    private void registerListeners() {
        final SensorManager sm = (SensorManager) mActivity.getSystemService(Activity.SENSOR_SERVICE);
        sm.registerListener(mAccelerometerListener,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(mMagneticListener,
                sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }

    private void unregisterListeners() {
        final SensorManager sm = (SensorManager) mActivity.getSystemService(Activity.SENSOR_SERVICE);
        sm.unregisterListener(mAccelerometerListener);
        sm.unregisterListener(mMagneticListener);
    }

    @Override
    public void onStart() {
        registerListeners();
    }

    @Override
    public void onStop() {
        unregisterListeners();
    }

    @Override
    public void onResume() {
        registerListeners();
    }

    @Override
    public void onPause() {
        unregisterListeners();
    }

    @Override
    public void update(final float dt) {
        mHorizontalFactor = getHorizontalAxis() / MAX_ANGLE;
        mVerticalFactor = 0.0f;
        mJump = isJumping();
        clampInputs();
    }

    private boolean isJumping(){
        if((System.currentTimeMillis()-mLastShake) < SHAKE_COOLDOWN){
            return mJump;
        }
        final float x = mLastAccels[0];
        final float y = mLastAccels[1];
        final float z = mLastAccels[2];
        final float acceleration = (float) Math.sqrt(x*x + y*y + z*z)
                - SensorManager.GRAVITY_EARTH;
        if(acceleration > SHAKE_THRESHOLD){
            mLastShake = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private float getHorizontalAxis() {
        final int rotation = mDisplay.getRotation();
        if (SensorManager.getRotationMatrix(mRotationMatrix, null, mLastAccels, mLastMagFields)) {
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) { //remap if in either of the portrait modes
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotationMatrix);
            }
            SensorManager.getOrientation(mRotationMatrix, mOrientation);
            //invert direction if in landscape or upside-down-portrait mode.
            final float dir = (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_180) ? -1f : 1f;
            return dir * mOrientation[1] * DEGREES_PER_RADIAN;
        } else { // Case for devices that DO NOT have magnetic sensors
            //TODO: this is largely untested, and does not deal with all orientations
            if (rotation == Surface.ROTATION_0) {
                return -mLastAccels[0] * 5;
            } else {
                return -mLastAccels[1] * -5;
            }
        }
    }
}
