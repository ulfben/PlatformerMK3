package com.ulfben.PlatformerMK3.utilities;
import android.graphics.Bitmap;

import com.ulfben.PlatformerMK3.engine.GameEngine;

import java.util.HashMap;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class BitmapPool {
    private static HashMap<String, Bitmap> mBitmaps = new HashMap<>();
    private BitmapPool(){super();}

    public static void init(){
        empty();
    }

    public static Bitmap createBitmap(final GameEngine engine, final String sprite, float widthMeters, float heightMeters){
        final String key = BitmapPool.makeKey(sprite, widthMeters, heightMeters);
        Bitmap bmp = BitmapPool.getBitmap(key);
        if(bmp == null){
            try {
                bmp = BitmapUtils.loadScaledBitmap(engine.getResourceID(sprite), (int)engine.worldToScreen(widthMeters, Axis.X), (int)engine.worldToScreen(heightMeters, Axis.Y));
                BitmapPool.put(key, bmp);
            }catch(final Exception e){
                e.printStackTrace();
            }
        }
        return bmp;
    }

    public static String makeKey(final String name, final Bitmap bmp){
        return name+"_"+bmp.getWidth()+"_"+bmp.getHeight();
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
    public static boolean contains(final Bitmap bmp){
        return mBitmaps.containsValue(bmp);
    }
    public static Bitmap getBitmap(final String key){
        return mBitmaps.get(key);
    }
    public static String getKey(final Bitmap bmp){
        for (HashMap.Entry<String, Bitmap> entry : mBitmaps.entrySet()) {
            if(bmp == entry.getValue()){
                return entry.getKey();
            }
        }
        return "";
    }
    public static void removeBitmap(final String key){
        Bitmap tmp = mBitmaps.get(key);
        if(tmp != null){
            mBitmaps.remove(key);
            tmp.recycle();
        }
    }
    public static void removeBitmap(Bitmap bmp){
        removeBitmap(getKey(bmp));
    }
    public static void empty(){
        for (final HashMap.Entry<String, Bitmap> entry : mBitmaps.entrySet()) {
            entry.getValue().recycle();
        }
        mBitmaps.clear();
    }
}
