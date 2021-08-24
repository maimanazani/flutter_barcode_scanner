package com.amolg.flutterbarcodescanner.utils;
import android.graphics.Point;

import android.content.Context;
import android.util.DisplayMetrics;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class AppUtil {
    public static int dpToPx(Context context,int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static int getWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static float getDPI(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.density;
    }

    public static Point getCenterScreenPosition(Context context) {
        Point p = getScreenSize(context);
        p.x = p.x / 2;
        p.y = p.y / 2;
        return p;
    }

    public static Point getScreenSize(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
}
