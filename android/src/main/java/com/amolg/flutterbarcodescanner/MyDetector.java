package com.amolg.flutterbarcodescanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import android.util.SparseArray;

import com.amolg.flutterbarcodescanner.utils.AppUtil;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.ByteArrayOutputStream;

class MyDetector extends Detector<Barcode> {
    private Detector mDelegate;
    private int mBoxWidth, mBoxHeight;

    public MyDetector(Detector delegate, int boxWidth, int boxHeight) {
        mDelegate = delegate;
        mBoxWidth = boxWidth;
        mBoxHeight = boxHeight;
    }
  
    public SparseArray<Barcode> detect(Frame frame) {
      // *** crop the frame here
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();
        int right = (width / 2) + (mBoxHeight / 2);
        int left = (width / 2) - (mBoxHeight / 2);
        int bottom = (height / 2) + (mBoxWidth / 2);
        int top = (height / 2) - (mBoxWidth / 2);
        Log.i("barcode.getDPI", left + " " +  top + " " + right  +  " " + bottom);

        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(left, top, right, bottom), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

        Frame croppedFrame =
                new Frame.Builder()
                        .setBitmap(bitmap)
                        .setRotation(frame.getMetadata().getRotation())
                        .build();

        return mDelegate.detect(croppedFrame);

     }
  
    public boolean isOperational() {
      return mDelegate.isOperational();
    }
  
    public boolean setFocus(int id) {
      return mDelegate.setFocus(id);
    }
  }