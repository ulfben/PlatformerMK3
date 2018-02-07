package com.ulfben.PlatformerMK3.levels;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

public abstract class LevelData {

    static final int NO_TILE = 0;//empty space in the dataset
    public static final String NO_SPRITE = ""; //for objects that will load their own custom assets, like animations.
    int[][] mTiles;
    int mHeight;
    int mWidth;

    static final String NULLSPRITE = "nullsprite";
    public static final String PLAYER = "player";
    public static final String COIN   = "coinyellow_shade";
    public static final String WALKER = "walker";
    public static final String SPEARS = "spears";

    void onTilesLoaded(){
        updateLevelDimensions();
    }

    public int getTile(final int x, final int y){ //NEW
        return mTiles[y][x];
    }
    int[] getRow(final int y){ //NEW
        return mTiles[y];
    }


    private void updateLevelDimensions(){
        mHeight = mTiles.length;
        mWidth = mTiles[0].length;
    }
    abstract public String getSpriteName(final int tileType);
}
