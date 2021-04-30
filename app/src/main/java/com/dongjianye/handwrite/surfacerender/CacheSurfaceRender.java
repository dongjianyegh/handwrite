package com.dongjianye.handwrite.surfacerender;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;

import com.dongjianye.handwrite.base.ThreadSurfaceView;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * @author dongjianye on 4/30/21
 */
public abstract class CacheSurfaceRender<T extends ThreadSurfaceView> implements ThreadSurfaceView.Callback {

    protected Canvas mCacheCanvas;
    protected Bitmap mCacheBitmap;

    protected final T mSurfaceView;

    public CacheSurfaceRender(T surfaceView) {
        mSurfaceView = surfaceView;
    }

    @Override
    @WorkerThread
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        updateCache(width, height);
    }

    @Override
    @WorkerThread
    public void onDrawFrame(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        drawFrameCache(mCacheCanvas);

        if (mCacheBitmap != null) {
            canvas.drawBitmap(mCacheBitmap, 0, 0, null);
        }
    }

    protected abstract void drawFrameCache(Canvas canvas);

    @Override
    @WorkerThread
    public void surfaceDestroyed() {
        recycle();
    }

    @WorkerThread
    private void updateCache(int width, int height) {
        final boolean recreate = mCacheBitmap == null || mCacheBitmap.getWidth() != width ||
                mCacheBitmap.getHeight() != height;

        if (recreate) {
            recycle();

            mCacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCacheCanvas = new Canvas(mCacheBitmap);
        }
    }

    @WorkerThread
    private void recycle() {
        if (mCacheBitmap != null) {
            mCacheBitmap.recycle();
            mCacheBitmap = null;
            mCacheCanvas = null;
        }
    }

    /**
     * 获取当前图片的copy。这个是阻塞的
     * @return
     */
    @AnyThread
    public Bitmap getCacheBitmap(final float rate, final Bitmap.Config config, long timeOut) {
        final FutureTask<Bitmap> task = new FutureTask<Bitmap>(new Callable<Bitmap>() {
            @Override
            @WorkerThread
            public Bitmap call() throws Exception {
                if (mCacheBitmap == null) {
                    return null;
                }
                final int width = mCacheBitmap.getWidth();
                final int height = mCacheBitmap.getHeight();
                final Bitmap result = Bitmap.createScaledBitmap(mCacheBitmap ,  (int) (width * rate), (int) (height * rate), true);
                if (result == mCacheBitmap) {
                    return mCacheBitmap.copy(config, true);
                } else {
                    return result;
                }
            }
        });
        mSurfaceView.queueEvent(task);

        try {
            return task.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return null;
        }
    }
}