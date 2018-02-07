package com.ulfben.PlatformerMK3.levels;
import android.util.Log;
import android.util.SparseArray;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-13.

public class TestLevel extends LevelData {
    private static final String TAG = "TestLevel";
    SparseArray<String> mTileIdToSpriteName = new SparseArray<String>();
    public TestLevel(){
        super();
        mTileIdToSpriteName.put(0, "background");
        mTileIdToSpriteName.put(1, PLAYER); //player uses animation
        mTileIdToSpriteName.put(2, "ground");
        mTileIdToSpriteName.put(3, "enemyblockiron");
        mTileIdToSpriteName.put(4, SPEARS); //spears are animated
        mTileIdToSpriteName.put(5, COIN);
        mTileIdToSpriteName.put(6, WALKER);
        mTileIdToSpriteName.put(7, "ground_right");
        mTileIdToSpriteName.put(8, "ground_left");
        mTileIdToSpriteName.put(9, "mud_square");
        mTileIdToSpriteName.put(10, "ground_round");
        mTileIdToSpriteName.put(11, "mud_right");
        mTileIdToSpriteName.put(12, "mud_left");

        mTiles = new int[][]{
                {5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5},
                {4,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,0,0,0,5,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,4},
                {3,0,0,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,5,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,8,2,2,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,10,5,0,0,0,0,0,0,0,0,0,0,0,0,0,5,0,0,8,7,0,0,8,2,7,0,0,5,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,8,2,7,0,0,0,0,0,0,5,0,0,0,8,7,0,0,0,0,0,0,0,0,0,0,0,0,0,8,7,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,0,0,8,2,2,2,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,7,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,8,7,0,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,8,2,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,0,0,0,0,0,0,4,4,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,5,0,0,0,0,0,0,0,0,0,0,8,2,2,7,0,0,8,2,2,2,2,7,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,8,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3},
                {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2},
                {12,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,11}
        };
        onTilesLoaded();
    }

    public String getSpriteName(final int tileType){
        final String fileName = mTileIdToSpriteName.get(tileType);
        if(fileName == null){
            Log.d(TAG, "getSpriteName: Unknown tileType: " + tileType + ". Using null-sprite.");
            return NULLSPRITE;
        }
        return fileName;
    }
}
