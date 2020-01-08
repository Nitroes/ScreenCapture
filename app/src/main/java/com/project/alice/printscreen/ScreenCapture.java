package com.project.alice.printscreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class ScreenCapture {

    private MediaProjectionManager mPM;
    public static int Width,Height;
    private Activity activity;
    public static int REQUEST_CODE=212;
    private Handler backgroundHandler;
    public static AtomicInteger oneScreenshot = new AtomicInteger(0);

    public ScreenCapture(Activity activity){
        this.activity=activity;
        mPM= (MediaProjectionManager) activity.getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Display display=activity.getWindowManager().getDefaultDisplay();
        Point point=new Point();
        display.getSize(point);
        Width=point.x;
        Height=point.y;
    }

    public interface CallBack{
        void Back(Bitmap bitmap);
    }

    public void startCatch(){
        if (Build.VERSION.SDK_INT >= 21) {
            activity.startActivityForResult(mPM.createScreenCaptureIntent(),REQUEST_CODE);
        } else {
            Log.e("TAG", "版本过低,无法截屏");
        }
    }

    public void ParseData( Intent data,CallBack callBack){
        onScreenshotTaskBegan();

        MediaProjection mP=mPM.getMediaProjection(Activity.RESULT_OK,data);
        ImageReader imageReader=ImageReader.newInstance(Width,Height,PixelFormat.RGBA_8888,2);
        VirtualDisplay vDisplay=mP.createVirtualDisplay("capture_screen",Width,Height,
                Resources.getSystem().getDisplayMetrics().densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),null,null);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader mImageReader) {

                Image image = null;
                try {
                    image = mImageReader.acquireLatestImage();
                    if (image != null) {
                        final Image.Plane[] planes = image.getPlanes();
                        if (planes.length > 0) {
                            final ByteBuffer buffer = planes[0].getBuffer();
                            int pixelStride = planes[0].getPixelStride();
                            int rowStride = planes[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * Width;
                            // create bitmap
                            Bitmap bmp = Bitmap.createBitmap(Width + rowPadding / pixelStride,
                                    Height, Bitmap.Config.ARGB_8888);
                            bmp.copyPixelsFromBuffer(buffer);

                            Bitmap croppedBitmap = Bitmap.createBitmap(bmp, 0, 0, Width, Height);

                            callBack.Back(croppedBitmap);

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (image != null) {
                        image.close();
                    }
                    if (mImageReader != null) {
                        mImageReader.close();
                    }
                    if (vDisplay != null) {
                        vDisplay.release();
                    }

                    mImageReader.setOnImageAvailableListener(null, null);
                    mP.stop();

                    onScreenshotTaskOver();
                }

            }
        }, getBackgroundHandler());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Image image=imageReader.acquireLatestImage();
//                imageView.setImageBitmap(ImageToBitmap(image));
//            }
//        },1000);
//        vDisplay.release();
//        vDisplay=null;
    }

    private Handler getBackgroundHandler() {
        if (backgroundHandler == null) {
            HandlerThread backgroundThread =
                    new HandlerThread("catwindow", android.os.Process
                            .THREAD_PRIORITY_BACKGROUND);
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }
        return backgroundHandler;
    }

    private synchronized void onScreenshotTaskBegan() {
        oneScreenshot.set(1);
    }

    private synchronized void onScreenshotTaskOver() {
        oneScreenshot.set(0);
    }

    public void close(){
        mPM=null;
    }


}



