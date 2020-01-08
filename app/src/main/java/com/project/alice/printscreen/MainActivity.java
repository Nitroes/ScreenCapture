package com.project.alice.printscreen;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

public class MainActivity extends AppCompatActivity {

    int Width,Height;
    ImageView imageView;
    Button button;
    ScreenCapture capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView=findViewById(R.id.imageView);
        button=findViewById(R.id.button);

        capture=new ScreenCapture(this);


        button.setOnClickListener(v->{
            capture.startCatch();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==ScreenCapture.REQUEST_CODE&&data!=null) {
            capture.ParseData(data, bitmap -> {
                imageView.setImageBitmap(bitmap);
            });
        }
    }

    private void saveBitmap(Bitmap bmp) throws IOException {
        File childFolder = Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES);
        File imageFile = new File(childFolder.getAbsolutePath() + "/" + System.currentTimeMillis
                () + ".jpg");
        OutputStream fOut = new FileOutputStream(imageFile);
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, fOut);//将bg输出至文件
        fOut.flush();
        fOut.close(); // do not forget to close the stream
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile
                (imageFile)));
    }


}
