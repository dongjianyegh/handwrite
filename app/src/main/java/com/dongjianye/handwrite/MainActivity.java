package com.dongjianye.handwrite;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private HandWriteView mHandWriteView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandWriteView = findViewById(R.id.hand_write_view);

        Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.brush3));

        mHandWriteView.setBrushBitmap(bitmap);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

        mHandWriteView.init(displayMetrics.density, 1);
    }
}