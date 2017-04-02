package com.ulfben.PlatformerMK3.engine;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ulfben.PlatformerMK3.gameobjects.GameObject;
import com.ulfben.PlatformerMK3.utilities.SysUtils;

import java.util.ArrayList;

// Created by Ulf Benjaminsson (ulfben) on 2017-02-12.

public class GameView extends SurfaceView {
    private static final String TAG = "GameView";
    private static final int AVD_TRUE_SCREEN_WIDTH = 1920; //q&d, see createViewport
    private static final int BG_COLOR = Color.rgb(135,206,235);//sky blue

    private ArrayList<GameObject> mVisibleGameObjects = new ArrayList<>();
    private Canvas mCanvas = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Paint mPaint = null;

    public GameView(final Context context) {
        super(context);
        init();
    }
    public GameView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GameView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init(){
        mPaint = new Paint();
        mSurfaceHolder = getHolder();
    }

    //TODO: probably move Viewport-creation out of the GameView
    public Viewport createViewport(final float worldWidth, final float worldHeight, final float metersToShowX, final float metersToShowY, final float scaleFactor){
        //WARNING: using unmodified widthPixels == AVD hard crash, on my development machine.
        //suspect it can be solved if I could get accurate resolution info. it seems the soft navigation keys of my AVD
        //is still encroaching on my SurfaceView. Might have to implement the SurfaceView.Callbacks.
        //for now, hardcode emulator's resolution to avoid hard crashes.
        int screenWidth = SysUtils.isProbablyEmulator() ? AVD_TRUE_SCREEN_WIDTH : getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        Log.i(TAG, "createViewport: " + screenWidth +" : " +screenHeight);
        if(scaleFactor != 1.0f){
            screenWidth = (int) (screenWidth * scaleFactor);
            screenHeight = (int) (screenHeight * scaleFactor);
            mSurfaceHolder.setFixedSize(screenWidth, screenHeight);
        }
        return new Viewport(worldWidth, worldHeight, screenWidth, screenHeight, metersToShowX, metersToShowY);
    }

    public void setVisibleObjects(ArrayList<GameObject> gameObjects) {
        mVisibleGameObjects = gameObjects;
    }

    public void render(){
        if(!lockAndSetCanvas()) {
            return;
        }
        mCanvas.drawColor(BG_COLOR);
        mPaint.setColor(Color.WHITE);
        final int numObjects = mVisibleGameObjects.size();
        for (int i = 0; i < numObjects; i++) {
            mVisibleGameObjects.get(i).render(mCanvas, mPaint);
        }
        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }

    private boolean lockAndSetCanvas() {
        if(!mSurfaceHolder.getSurface().isValid()){
            return false;
        }
        mCanvas = mSurfaceHolder.lockCanvas();
        return (mCanvas != null);
    }
}
