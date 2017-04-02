package com.ulfben.PlatformerMK3.levels;
import java.util.HashSet;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

public abstract class LevelData {

    public static final int NO_TILE = 0;
    public int[][] mTiles;
    public int mHeight;
    public int mWidth;
    public int mTileCount;

    public static final String NULLSPRITE = "nullsprite";
    public static String PLAYER = "player";
    public static String COIN   = "coinyellow_shade";
    public static String WALKER = "walker";
    public static String SPEARS = "spears";

    protected void onTilesLoaded(){
        updateLevelDimensions();
        countUniqueTiles();
    }

    protected void countUniqueTiles(){
        int tileType;
        final HashSet<Integer> set = new HashSet<>();
        for(int y = 0; y < mHeight; y++){
            for(int x = 0; x < mWidth; x++){
                tileType = mTiles[y][x];
                set.add(tileType);
            }
        }
        mTileCount = set.size();
    }

    public int getTile(final int x, final int y){ //NEW
        return mTiles[y][x];
    }
    public int[] getRow(final int y){ //NEW
        return mTiles[y];
    }


    protected void updateLevelDimensions(){
        mHeight = mTiles.length;
        mWidth = mTiles[0].length;
    }
    abstract public String getSpriteName(final int tileType);
}
