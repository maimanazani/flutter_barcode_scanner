/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amolg.flutterbarcodescanner.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.amolg.flutterbarcodescanner.BarcodeCaptureActivity;
import com.amolg.flutterbarcodescanner.FlutterBarcodeScannerPlugin;
import com.amolg.flutterbarcodescanner.constants.AppConstants;
import com.amolg.flutterbarcodescanner.utils.AppUtil;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class GraphicOverlay<T extends GraphicOverlay.Graphic> extends View {
    private final Object mLock = new Object();
    private float mWidthScaleFactor = 1.0f, mHeightScaleFactor = 1.0f;

    private int mFacing = CameraSource.CAMERA_FACING_BACK;
    private Set<T> mGraphics = new HashSet<>();

    /**
     * Custom added values for overlay
     */
    private float left, top, endY;
    private int rectWidth, rectHeight, frames, lineColor, lineWidth;
    private boolean revAnimation;
    private Barcode barcode;
    private long timerCountdown;


    public static abstract class Graphic {
        private GraphicOverlay mOverlay;

        public Graphic(GraphicOverlay overlay) {
            mOverlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public float scaleX(float horizontal) {
            return horizontal * mOverlay.mWidthScaleFactor;
        }

        public float scaleY(float vertical) {
            return vertical * mOverlay.mHeightScaleFactor;
        }

        public float translateX(float x) {
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        public float translateY(float y) {
            return scaleY(y);
        }

        public void postInvalidate() {
            mOverlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        rectWidth = AppConstants.BARCODE_RECT_WIDTH;
        rectHeight = BarcodeCaptureActivity.SCAN_MODE == BarcodeCaptureActivity.SCAN_MODE_ENUM.QR.ordinal()
                ? AppConstants.BARCODE_RECT_HEIGHT : (int) (AppConstants.BARCODE_RECT_HEIGHT / 1.5);

        lineColor = Color.parseColor(FlutterBarcodeScannerPlugin.lineColor);

        lineWidth = AppConstants.BARCODE_LINE_WIDTH;
        frames = AppConstants.BARCODE_FRAMES;
    }


    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
        }
        postInvalidate();
    }


    public void add(T graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
        }
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        left = (w - AppUtil.dpToPx(getContext(), rectWidth)) / 2;
        top = (h - AppUtil.dpToPx(getContext(), rectHeight)) / 2;
        endY = top;
        super.onSizeChanged(w, h, oldw, oldh);
    }


    public void remove(T graphic) {
        synchronized (mLock) {
            mGraphics.remove(graphic);
        }
        postInvalidate();
    }

    public List<T> getGraphics() {
        synchronized (mLock) {
            return new Vector(mGraphics);
        }
    }

    public float getWidthScaleFactor() {
        return mWidthScaleFactor;
    }

    public float getHeightScaleFactor() {
        return mHeightScaleFactor;
    }

    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (mLock) {
            mFacing = facing;
        }
        postInvalidate();
    }

    public void setBarcode(Barcode barcodeItem) {
        barcode = barcodeItem;
    }

    public void setTimer(long second) {
        timerCountdown = second;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw transparent rect
        int cornerRadius = 0;
        Paint eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        float center = AppUtil.getHeight(getContext()) / 2;

        RectF rect = new RectF(left, top, AppUtil.dpToPx(getContext(), rectWidth) + left, AppUtil.dpToPx(getContext(), rectHeight) + top);
        canvas.drawRoundRect(rect, (float) cornerRadius, (float) cornerRadius, eraser);

        // draw horizontal line
        Paint line = new Paint();
        line.setColor(lineColor);
        line.setStrokeWidth(Float.valueOf(lineWidth));

        float middleHeight = (AppUtil.getHeight(getContext()) - AppUtil.dpToPx(getContext(), 60)) / 2;

        canvas.drawLine(left,middleHeight , left + AppUtil.dpToPx(getContext(), rectWidth), middleHeight , line);

        // draw horizontal line
//        Paint calibrationLine = new Paint();
//        calibrationLine.setColor(Color.GREEN);
//        calibrationLine.setStrokeWidth(12F);
//
//        float centerHeight = (AppUtil.getHeight(getContext()) / AppUtil.getDPI(getContext()))  ;
//        canvas.drawLine(left, centerHeight, left + AppUtil.dpToPx(getContext(), rectWidth), centerHeight , calibrationLine);


        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        paint.setTextSize(20 * AppUtil.getDPI(getContext()));

//        if(null != barcode){
//            RectF barcodeGraphic = new RectF(barcode.getBoundingBox());
//            Paint mRectPaint   = new Paint();
//            mRectPaint.setColor(Color.GREEN);
//            mRectPaint.setStyle(Paint.Style.STROKE);
//            mRectPaint.setStrokeWidth(4.0f);
//
//            canvas.drawRect(barcodeGraphic, mRectPaint);
//        }

        if (null != barcode) {
            canvas.drawText("Barcode detected: " + barcode.displayValue, 10, left + AppUtil.dpToPx(getContext(), rectWidth) + top / 2, paint);
            canvas.drawText("Press checkbox to continue or wait " + timerCountdown + " s...", 10, left + AppUtil.dpToPx(getContext(), rectWidth) + top / 2 + (40 * AppUtil.getDPI(getContext())), paint);

        } else {
            canvas.drawText("Detecting barcode...", 10, left + AppUtil.dpToPx(getContext(), rectWidth) + top / 2, paint);
        }


        invalidate();
    }
}