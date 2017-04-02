package com.ulfben.PlatformerMK3;
//Created by Ulf Benjaminsson (ulfben) on 2017-04-02.

public class Random {
    public final static java.util.Random RNG = new java.util.Random();
    private Random(){super();}

    public static boolean coinFlip(){
        return RNG.nextFloat() > 0.5;
    }

    public static float nextFloat(){
        return RNG.nextFloat();
    }

    public static int nextInt(final int max){
        return RNG.nextInt(max);
    }

    public static int between(final int min, final int max){
        return RNG.nextInt(max-min)+min;
    }

    public static float between(final float min, final float max){
        return min+RNG.nextFloat()*(max-min);
    }
}
