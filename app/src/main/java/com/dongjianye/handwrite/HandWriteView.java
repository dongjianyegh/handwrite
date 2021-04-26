package com.dongjianye.handwrite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class HandWriteView extends GLSurfaceView implements WriteListener {
    
    private final HandWriteRender mHandWriteRender;
    private final HandWriteHandler mHandWriteHandler;
    private boolean mSurfaceReady = false;
    private FinishListener mFinishListener;

    @Override
    public void onStartWrite() {
    }

    public HandWriteView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mHandWriteHandler = new HandWriteHandler(this);
        mHandWriteRender = new HandWriteRender(this);
        setRenderer(mHandWriteRender);
        setRenderMode(0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
    }

    /**
     * 设置笔刷大小
     * @param density
     * @param scale
     */
    public void init(float density, float scale) {
        float size = 10.0f * density;
        if (scale > 0.0f) {
            size *= scale;
        }
        mHandWriteRender.setSize((double) size);
        mHandWriteRender.setDensity((double) density);
        mHandWriteRender.clearTasks();
        requestRender();
    }

    public void setBrushResource(int i) {
        mHandWriteRender.setResource(i);
    }

    public void setBrushBitmap(Bitmap bitmap) {
        mHandWriteRender.setBitmap(bitmap);
    }

    public void setStrokeAlpha(float f) {
        mHandWriteRender.setStrokeAlpha(f);
    }

    public void setWordFinishDelayTime(long j) {
        mHandWriteHandler.setDelayTime(j);
    }

    public void setActiveRect(int i, int i2, int i3, int i4) {
        mHandWriteRender.A(new Rect(i, i2, i3, i4));
    }

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
        mHandWriteHandler.action(actionMasked);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mHandWriteRender.appendMotionEvent(motionEvent);
                break;
            case MotionEvent.ACTION_UP: {
                mHandWriteRender.appendMotionEvent(motionEvent);
                requestRender();
                if (mFinishListener != null) {
                    mFinishListener.onMiddleUp();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
                mHandWriteRender.appendMotionEvent(motionEvent);
                requestRender();
                break;
        }
        return true;
    }

    public void setHandWriteListener(FinishListener jno) {
        mFinishListener = jno;
    }

    @Override
    public void onFinishWrite() {
        mHandWriteRender.clear();
        requestRender();

        if (mFinishListener != null) {
            mFinishListener.onFinishWrite();
        }
    }

    public void resetWrite() {
        mHandWriteRender.clear();
    }

    public void skipPointRate(int i) {
        mHandWriteRender.setSkipPointRate(i);
    }
}
