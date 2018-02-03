package com.ulfben.PlatformerMK3.engine;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.ulfben.PlatformerMK3.engine.GameEngine;

import java.util.Locale;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-08.

public class DebugTextRenderer {
    private DebugTextRenderer(){super();}

    //Updated from the GameEngine
    public static int VISIBLE_OBJECTS = 0;
    public static int TOTAL_OBJECT_COUNT = 0;
    public static PointF PLAYER_POSITION = new PointF(0f,0f);
    public static int FRAMERATE = 0;
    public static String CAMERA_INFO = "";

    private final static String[] DBG_STRINGS = new String[4];
    //[0] = CAMERA_INFO
    private final static String DBG_OBJ_RENDER_COUNT =  "Objects rendered: %d / %d"; //[1] rendering stats
    private final static String DBG_PLAYER_INFO =  "Player: [%.2f, %.2f]"; //[2] player position
    private final static String DBG_UPS = "FPS: %d"; //[3] frames per second
    private final static Locale LOCALE = Locale.getDefault();

    private static String[] getDebugStrings(){
        DBG_STRINGS[0] = CAMERA_INFO;
        DBG_STRINGS[1] = String.format(LOCALE, DBG_OBJ_RENDER_COUNT, VISIBLE_OBJECTS,  TOTAL_OBJECT_COUNT);
        DBG_STRINGS[2] = String.format(LOCALE, DBG_PLAYER_INFO,  PLAYER_POSITION.x,  PLAYER_POSITION.y);
        DBG_STRINGS[3] = String.format(LOCALE, DBG_UPS, FRAMERATE);
        return DBG_STRINGS;
    }

    public static void render(final Canvas canvas, final Viewport camera, final Paint paint){
        final int textSize = (int) camera.getPixelsPerMeterY() / 3;
        final int margin = 10;
        int y = camera.getScreenHeight()-margin;
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        for(final String s : getDebugStrings()){
            canvas.drawText(s, margin, y, paint);
            y -= textSize;
        }
    }
}
