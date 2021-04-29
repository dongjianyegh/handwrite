package com.dongjianye.handwrite.doublebuffer;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.dongjianye.handwrite.HandWriteView;

/**
 * @author dongjianye on 4/27/21
 */
public class ModernHandWriteView extends GLSurfaceView {

    private boolean mSurfaceReady = false;
    private ModernRender mHandWriteRender;

    public ModernHandWriteView(Context context) {
        this(context, null);
    }

    public ModernHandWriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mHandWriteRender = new ModernRender(context);

        setEGLContextClientVersion(2);

        setRenderer(mHandWriteRender);

        setRenderMode(0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        super.surfaceChanged(surfaceHolder, i, i2, i3);
        mSurfaceReady = true;
    }

    private static int getActionMasked(MotionEvent motionEvent) {
        return motionEvent.getAction() & 255;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!mSurfaceReady) {
            Log.e(HandWriteView.class.getName(), "onTouchEvent ignore touch event before ready");
            return true;
        }
        int actionMasked = getActionMasked(motionEvent);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mHandWriteRender.appendMotionEvent(motionEvent);
                requestRender();
                break;
            case MotionEvent.ACTION_UP: {
                mHandWriteRender.appendMotionEvent(motionEvent);
                requestRender();
//                if (mFinishListener != null) {
//                    mFinishListener.onMiddleUp();
//                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
                mHandWriteRender.appendMotionEvent(motionEvent);
                requestRender();
                break;
        }
        return true;
    }
}