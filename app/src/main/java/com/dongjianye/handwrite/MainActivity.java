package com.dongjianye.handwrite;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.dongjianye.handwrite.airhockey.AirHockeyRender;
import com.dongjianye.handwrite.myrender.MyHandWriteView;

public class MainActivity extends AppCompatActivity {

    private HandWriteView mHandWriteView;
    private AirHockeyRender mAirRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int type = 0;

        if (type == 0) {
            setContentView(R.layout.activity_main);

            mHandWriteView = findViewById(R.id.hand_write_view);

//        mAirRender = new AirHockeyRender(this);

            Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.brush3));

            mHandWriteView.setBrushBitmap(bitmap);

            ActivityManager activityManager =
                    (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ConfigurationInfo configurationInfo = activityManager
                    .getDeviceConfigurationInfo();
            final boolean supportsEs2 =
                    configurationInfo.reqGlEsVersion >= 0x20000
                            || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                            && (Build.FINGERPRINT.startsWith("generic")
                            || Build.FINGERPRINT.startsWith("unknown")
                            || Build.MODEL.contains("google_sdk")
                            || Build.MODEL.contains("Emulator")
                            || Build.MODEL.contains("Android SDK built for x86")));

            if (supportsEs2) {
                // Request an OpenGL ES 2.0 compatible context.
                //mHandWriteView.setEGLContextClientVersion(2);
                //mHandWriteView.setRenderer(mAirRender);
            } else {

            }

            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

            mHandWriteView.init(displayMetrics.density, 1);
        } else if (type == 1) {
            setContentView(R.layout.activity_main_2);

            MyHandWriteView handWriteView = findViewById(R.id.hand_write_view);

            Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.brush3));

            handWriteView.setBitmap(bitmap);
        } else if (type == 2) {
            setContentView(R.layout.activity_main_3);
        } else if (type == 3) {
            setContentView(R.layout.activity_main_4);
        }
    }
}