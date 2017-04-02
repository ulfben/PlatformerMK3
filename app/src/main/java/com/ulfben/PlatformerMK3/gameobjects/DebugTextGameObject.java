package com.ulfben.PlatformerMK3.gameobjects;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.ulfben.PlatformerMK3.engine.GameEngine;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-08.

public class DebugTextGameObject extends GameObject {
    String[] mDebugStrings = new String[0];

    public DebugTextGameObject(final GameEngine engine, final String sprite){
        super(engine, sprite, 0f, 0f, 0.0f, 0.3f);
        mCanCollide = false;
    }

    @Override
    public boolean isColliding(final GameObject that){
        return false;
    }

    @Override
    public void postConstruct(){
    }

    @Override
    public void render(final Canvas canvas, final Paint paint){
        final int textSize = (int) (mEngine.getPixelsPerMeter()*mHeight);
        int y = textSize;
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        mDebugStrings = mEngine.getDebugStrings();
        for(final String s : mDebugStrings){
            if(!"".equals(s)) {
                canvas.drawText(s, 10, y, paint);
                y += textSize;
            }
        }
    }

    @Override
    public void update(final float dt){
        //ensure we're not clipped by the viewport.
        mWorldLocation.x = mEngine.mCamera.position().x;
        mWorldLocation.y = mEngine.mCamera.position().y;
        updateBounds();
    }
}
