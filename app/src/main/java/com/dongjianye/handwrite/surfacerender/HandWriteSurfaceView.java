package com.dongjianye.handwrite.surfacerender;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.dongjianye.handwrite.HandWriteView;
import com.dongjianye.handwrite.base.ThreadSurfaceView;

import static androidx.core.view.MotionEventCompat.getActionMasked;

/**
 * @author dongjianye on 4/30/21
 */
public class HandWriteSurfaceView extends ThreadSurfaceView {

    private HandWriteRender mHandWriteRender;

    public HandWriteSurfaceView(Context context) {
        this(context, null);
    }

    public HandWriteSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHandWriteRender = new HandWriteRender(this);
        setRenderer(mHandWriteRender);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = getActionMasked(motionEvent);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mHandWriteRender.appendMotionEvent(motionEvent);
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

    public Bitmap getCacheBitmap() {
        return mHandWriteRender.getCacheBitmap(1, Bitmap.Config.ARGB_8888, 10);
    }
}