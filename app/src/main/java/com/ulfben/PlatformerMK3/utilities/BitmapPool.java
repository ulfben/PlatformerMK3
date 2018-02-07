package com.ulfben.PlatformerMK3.utilities;
import android.graphics.Bitmap;

import com.ulfben.PlatformerMK3.engine.GameEngine;

import java.util.HashMap;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class BitmapPool {
    private static final HashMap<String, Bitmap> mBitmaps = new HashMap<>();
    private BitmapPool(){super();}

    public static Bitmap createBitmap(final GameEngine engine, final String sprite, float widthMeters, float heightMeters){
        final String key = BitmapPool.makeKey(sprite, widthMeters, heightMeters);
        Bitmap bmp = BitmapPool.getBitmap(key);
        if(bmp == null){
            try {
                bmp = BitmapUtils.loadScaledBitmap(engine.getContext(), sprite, (int)engine.worldToScreen(widthMeters, Axis.X), (int)engine.worldToScreen(heightMeters, Axis.Y));
                BitmapPool.put(key, bmp);
            }catch(final Exception e){
                e.printStackTrace();
            }
        }
        return bmp;
    }

    public static String makeKey(final String name, final float width, final float height){
        return name+"_"+width+"_"+height;
    }
    public static void put(final String key, final Bitmap bmp){
        if(mBitmaps.containsKey(key)) {
            return;
        }
        mBitmaps.put(key, bmp);
    }
    public static boolean contains(final String key){
        return mBitmaps.containsKey(key);
    }
    public static boolean contains(final Bitmap bmp){ return mBitmaps.containsValue(bmp); }
    public static Bitmap getBitmap(final String key){
        return mBitmaps.get(key);
    }
    private static String getKey(final Bitmap bmp){
        if(bmp != null) {
            for (HashMap.Entry<String, Bitmap> entry : mBitmaps.entrySet()) {
                if (bmp == entry.getValue()) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }
    private static void remove(final String key){
        Bitmap tmp = mBitmaps.get(key);
        if(tmp != null){
            mBitmaps.remove(key);
            tmp.recycle();
        }
    }
    public static void remove(Bitmap bmp){
        if(bmp == null){return;}
        remove(getKey(bmp));
    }
    public static void empty(){
        for (final HashMap.Entry<String, Bitmap> entry : mBitmaps.entrySet()) {
            entry.getValue().recycle();
        }
        mBitmaps.clear();
    }
}
