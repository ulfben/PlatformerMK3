package com.ulfben.PlatformerMK3.utilities;
import android.graphics.PointF;
// Created by Ulf Benjaminsson (ulfben) on 2017-02-24.


public class Utils {
    private Utils() {
        super();
    }

    public static float roundToNth(final float val, final float precision){
        return ((int) ((val * precision) + ((val < 0.0) ? -0.5 : 0.5))) / precision;
    }

    public static PointF clamp(final PointF val, final PointF min, final PointF max){
        if(val.x < min.x){
            val.x = min.x;
        }else if(val.x > max.x){
            val.x = max.x;
        }
        if(val.y < min.y){
            val.y = min.y;
        }else if(val.y > max.y){
            val.y = max.y;
        }
        return val;
    }

    public static float clamp(float val, final float min, final float max){
        if(val < min){
            val = min;
        }else if(val > max){
            val = max;
        }
        return val;
    }

    public static int clamp(int val, final int min, final int max){
        if(val < min){
            val = min;
        }else if(val > max){
            val = max;
        }
        return val;
    }

    public static int wrap(int val, final int min, final int max){
        if(val < min){
            val = max;
        }else if(val > max){
            val = min;
        }
        return val;
    }

    //if either target dimension is is 0, the other will be calculated - scaled according the src aspect ratio.
    public static void calcWidthOrHeight(final PointF out, final float targetWidth, final float targetHeight, final float srcWidth, final float srcHeight){
        if (targetWidth <= 0 && targetHeight <= 0) throw new IllegalArgumentException("One of the dimensions must be provided!");
        //formula: new height = (original height / original width) x new width
        out.x = targetWidth;
        out.y = targetHeight;
        if(targetWidth == 0 || targetHeight == 0){
            if(targetHeight > 0) { //if Y is configured, calculate X
                out.x = (srcWidth / srcHeight) * targetHeight;
            }else { //if X is configured, calculate Y
                out.y = (srcHeight / srcWidth) * targetWidth;
            }
        }
    }
}
