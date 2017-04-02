package com.ulfben.PlatformerMK3;
import android.graphics.Bitmap;

import java.util.HashMap;
// Created by Ulf Benjaminsson (ulfben) on 2017-03-29.

public class BitmapPool {
    private static HashMap<String, Bitmap> mBitmaps = new HashMap<>();
    private BitmapPool(){super();}

    public static void init(){
        empty();
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
    public static void empty(){
        for (final HashMap.Entry<String, Bitmap> entry : mBitmaps.entrySet()) {
            entry.getValue().recycle();
        }
        mBitmaps.clear();
    }
}
