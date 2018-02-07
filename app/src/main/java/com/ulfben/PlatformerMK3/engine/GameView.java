package com.ulfben.PlatformerMK3.engine;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.ulfben.PlatformerMK3.gameobjects.GameObject;

import java.util.ArrayList;

// Created by Ulf Benjaminsson (ulfben) on 2017-02-12.

public class GameView extends SurfaceView{
    private static final String TAG = "GameView";
    private static final int BG_COLOR = Color.rgb(135,206,235);//sky blue
    public static final int DEFAULT_WIDTH = 1920;
    public static final int DEFAULT_HEIGHT = 1280;
    private Canvas mCanvas = null;
    private SurfaceHolder mSurfaceHolder = null;
    private DisplayMetrics mScreenMetrics = new DisplayMetrics(); //used to hold display information.
    private Paint mPaint = new Paint();
    private final Point mScreenCord = new Point(); //re-usable object for the render-loop
    private final Matrix mTransform = new Matrix(); //re-usable object for the render-loop
    private int mFixedWidth = DEFAULT_WIDTH;
    private int mFixedHeight = DEFAULT_HEIGHT;

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
        mSurfaceHolder = getHolder();
        readDisplayInfo();
    }

    private void readDisplayInfo(){
        final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if(wm != null){
            wm.getDefaultDisplay().getMetrics(mScreenMetrics);
        }
    }

    //getSafeWidth / getSafeHeight will always return a reasonable number, even if the surface hasn't been created yet.
    // they return either SurfaceView.getWidth() or  DefaultDisplay.widthPixels or DEFAULT_WIDTH
    //I need this so I don't have to deal with two lifecycles (the Application/GameEngine and the SurfaceView)
    //the can simply start with the place-holder values, and once the Holder.callbacks starts coming
    //the engine will switch over to the most up-to-date and correct values.
    public int getSafeWidth(){
        int width = getWidth();
        if(width > 0){ return width; } //width seems valid
        if(mFixedWidth != DEFAULT_WIDTH){ return mFixedWidth; } //if our fixed buffer size has been touched, use that value
        readDisplayInfo(); //no other values available, so lets use the screen width
        if(mScreenMetrics.widthPixels > 0) { return mScreenMetrics.widthPixels; }
        return DEFAULT_WIDTH;
    }

    public int getSafeHeight(){
        int height = getHeight();
        if(height > 0){ return height;} //the self-reported height seems sane
        if(mFixedHeight != DEFAULT_HEIGHT){  return mFixedHeight; } //if our fixed buffer size has been touched, use that value
        readDisplayInfo(); //no other values available, so lets use the screen height
        if(mScreenMetrics.heightPixels > 0){ return mScreenMetrics.heightPixels; }
        return DEFAULT_HEIGHT;
    }

    //Render to a fixed-size buffer and then scale on the GPU
    //https://android-developers.googleblog.com/2013/09/using-hardware-scaler-for-performance.html
    public void setFixedSize(int newWidth, int newHeight){
        if(newWidth < 1 || newHeight < 1){ return; }
        if(newWidth != mFixedWidth || newHeight != mFixedHeight) { //make sure we're actually applying *new* values
            mFixedWidth = newWidth;                              //to avoid redundant onSurfaceChanged callbacks!
            mFixedHeight = newHeight;
            mSurfaceHolder.setFixedSize(mFixedWidth, mFixedHeight);
        }
    }

    public void render(final ArrayList<GameObject> visibleGameObjects, final Viewport camera){
        if(!lockAndAcquireCanvas()) {
            return;
        }
        try {
            mCanvas.drawColor(BG_COLOR);
            mPaint.setColor(Color.WHITE);
            final int numObjects = visibleGameObjects.size();

            GameObject obj;
            for (int i = 0; i < numObjects; i++) {
                obj = visibleGameObjects.get(i);
                camera.worldToScreen(obj, mScreenCord);
                mTransform.reset();
                mTransform.postTranslate(mScreenCord.x, mScreenCord.y);
                obj.render(mCanvas, mTransform, mPaint);
            }
            if (GameEngine.SHOW_STATS) {
                DebugTextRenderer.render(mCanvas, camera, mPaint);
            }
        } finally { //Belt and Suspenders! Make sure we unlock the canvas no matter what happened during rendering.
            if(mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
    private boolean lockAndAcquireCanvas() {
        if(!mSurfaceHolder.getSurface().isValid()){
            return false;
        }
        mCanvas = mSurfaceHolder.lockCanvas();
        return (mCanvas != null);
    }
    public void destroy(){
        mCanvas = null;
        mSurfaceHolder = null;
        mPaint = null;
    }
}
