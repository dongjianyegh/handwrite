package com.dongjianye.handwrite.base;

import android.annotation.AnyThread;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.WorkerThread;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dongjianye.handwrite.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.graphics.PixelFormat.RGBA_8888;
import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;
import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

/**
 * @author dongjianye on 4/29/21
 */
public class ThreadSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private final static String TAG = "ThreadSurfaceView";

    private final static boolean LOG_THREADS = BuildConfig.DEBUG;
    private static final boolean LOG_ATTACH_DETACH = BuildConfig.DEBUG;

    private static final ThreadSurfaceView.CustomThreadManager CUSTOM_THREAD_MANAGER_INSTANCE =
            new ThreadSurfaceView.CustomThreadManager();

    private final WeakReference<ThreadSurfaceView> mThisWeakRef = new WeakReference<ThreadSurfaceView>(this);

    private CustomThread mThread;

    private Callback mRenderer;

    private boolean mDetached;

    public interface Callback {
        @WorkerThread
        void surfaceCreated(@NonNull SurfaceHolder holder);

        @WorkerThread
        void surfaceChanged(@NonNull SurfaceHolder holder, @PixelFormat.Format int format,
                            @IntRange(from = 0) int width, @IntRange(from = 0) int height);

        @WorkerThread
        void onDrawFrame(Canvas canvas);

        @WorkerThread
        void surfaceDestroyed();

        @AnyThread
        Bitmap getCacheBitmap(float ratio, Bitmap.Config config, long timeOut);
    }

    public ThreadSurfaceView(Context context) {
        this(context, null);
    }

    public ThreadSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSPARENT);
    }

    public void setRenderer(Callback renderer) {
        checkRenderThreadState();
        mRenderer = renderer;
        mThread = new ThreadSurfaceView.CustomThread(mThisWeakRef);
        mThread.start();
    }

    public void requestRender() {
        mThread.requestRender();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "View: surfaceCreated start");
        }

        mThread.surfaceCreated();

        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "View: surfaceCreated end");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "View: surfaceChanged start");
        }

        mThread.onWindowResize(width, height);

        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "View: surfaceChanged start");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "View: surfaceDestroyed start");
        }

        mThread.surfaceDestroyed();

        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "View: surfaceDestroyed end");
        }
    }

    private void checkRenderThreadState() {
        if (mThread != null) {
            throw new IllegalStateException(
                    "setRenderer has already been called for this instance.");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow start reattach =" + mDetached);
        }
        if (mDetached && (mRenderer != null)) {
            int renderMode = RENDERMODE_CONTINUOUSLY;
            if (mThread != null) {
                renderMode = mThread.getRenderMode();
            }
            mThread = new ThreadSurfaceView.CustomThread(mThisWeakRef);
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mThread.setRenderMode(renderMode);
            }
            mThread.start();
        }
        mDetached = false;

        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow start end");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow start");
        }
        if (mThread != null) {
            mThread.requestExitAndWait();
        }
        mDetached = true;
        super.onDetachedFromWindow();

        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow end");
        }
    }

    public void queueEvent(Runnable r) {
        if (r == null) {
            return;
        }

        if (mThread == null) {
            r.run();
        } else {
            mThread.queueEvent(r);
        }
    }

    private static class CustomThread extends Thread {

        private static final boolean LOG_SURFACE = BuildConfig.DEBUG;
        private static final boolean LOG_RENDERER = BuildConfig.DEBUG;
        private static final boolean LOG_RENDERER_DRAW_FRAME = BuildConfig.DEBUG;

        private WeakReference<ThreadSurfaceView> mSurfaceViewWeakRef;

        private int mWidth;     // surfaceview 宽度
        private int mHeight;    // surfaceView 高度
        private int mRenderMode; // 类型

        private boolean mExited;

        private boolean mHasSurface;
        private boolean mWaitingForSurface;

        private boolean mSizeChanged;
        private boolean mRequestRender;
//        private boolean mHaveEglSurface;
        private boolean mShouldExit;

        private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();

        public CustomThread(WeakReference<ThreadSurfaceView> surfaceViewWeakRef) {
            mWidth = 0;
            mHeight = 0;
            mSurfaceViewWeakRef = surfaceViewWeakRef;

        }

        @Override
        public void run() {
            setName("ThreadSurfaceView:CustomThread:" + getId());

            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                // 基本不会走到这里,做个
                final ArrayList<Runnable> backup = new ArrayList<>(mEventQueue);
                for (Runnable r : backup) {
                    r.run();
                }
                CUSTOM_THREAD_MANAGER_INSTANCE.threadExiting(this);
            }
        }

        private void guardedRun() throws InterruptedException {

            boolean createEglContext = false;
            boolean sizeChanged = false;
            boolean surfaceDestroy = false;
            boolean haveSurfacedCreated = false;
            Runnable event = null;

            int w = 0;
            int h = 0;

            while (true) {
                synchronized (CUSTOM_THREAD_MANAGER_INSTANCE) {
                    while (true) {
                        if (mShouldExit) {
                            return;
                        }

                        if (!mEventQueue.isEmpty()) {
                            event = mEventQueue.remove(0);
                            break;
                        }

                        if (haveSurfacedCreated && !mHasSurface) {
                            if (LOG_SURFACE) {
                                Log.i(TAG, "noticed surfaceView surface destroyed tid=" + getId());
                            }

                            surfaceDestroy = true;
                            CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
                            break;
                        }

                        // Have we lost the SurfaceView surface?
                        if (!mHasSurface && !mWaitingForSurface) {
                            if (LOG_SURFACE) {
                                Log.i(TAG, "noticed surfaceView surface lost tid=" + getId());
                            }
                            mWaitingForSurface = true;
                            CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
                        }

                        // Have we acquired the surface view surface?
                        if (mHasSurface && mWaitingForSurface) {
                            if (LOG_SURFACE) {
                                Log.i(TAG, "noticed surfaceView surface acquired tid=" + getId());
                            }
                            createEglContext = true;
                            mWaitingForSurface = false;
                            CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
                        }

                        // Ready to draw?
                        if (readyToDraw()) {

                            if (mSizeChanged) {
                                sizeChanged = true;
                                w = mWidth;
                                h = mHeight;
                                mSizeChanged = false;
                            }
                            mRequestRender = false;
                            CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();

                            // 直到可以绘制的时候，才能退出循环
                            break;
                        }

                        if (LOG_THREADS) {
                            Log.i(TAG, "waiting tid=" + getId()
                                    + " mHasSurface: " + mHasSurface
                                    + " mWaitingForSurface: " + mWaitingForSurface
                                    + " mWidth: " + mWidth
                                    + " mHeight: " + mHeight
                                    + " mRequestRender: " + mRequestRender
                                    + " mRenderMode: " + mRenderMode);
                        }

                        CUSTOM_THREAD_MANAGER_INSTANCE.wait();
                    }
                }
                if (event != null) {
                    event.run();
                    event = null;
                    continue;
                }

                if (surfaceDestroy) {
                    if (LOG_RENDERER) {
                        Log.w(TAG, "onSurfaceDestroy");
                    }
                    ThreadSurfaceView view = mSurfaceViewWeakRef.get();
                    if (view != null) {
                        try {
                            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "onSurfaceDestroy");
                            view.mRenderer.surfaceDestroyed();
                        } finally {
                            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
                        }
                    }
                    surfaceDestroy = false;
                    haveSurfacedCreated = false;
                    continue;
                }

                if (createEglContext) {
                    if (LOG_RENDERER) {
                        Log.w(TAG, "Thread: onSurfaceCreated");
                    }
                    ThreadSurfaceView view = mSurfaceViewWeakRef.get();
                    if (view != null) {
                        try {
                            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "onSurfaceCreated");
                            view.mRenderer.surfaceCreated(view.getHolder());
                            haveSurfacedCreated = true;
                        } finally {
                            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
                        }
                    }
                    createEglContext = false;
                }

                if (sizeChanged) {
                    if (LOG_RENDERER) {
                        Log.w(TAG, "Thread: onSurfaceChanged(" + w + ", " + h + ")");
                    }
                    ThreadSurfaceView view = mSurfaceViewWeakRef.get();
                    if (view != null) {
                        try {
                            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "onSurfaceChanged");
                            view.mRenderer.surfaceChanged(view.getHolder(), RGBA_8888, w, h);
                        } finally {
                            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
                        }
                    }
                    sizeChanged = false;
                }

                if (LOG_RENDERER_DRAW_FRAME) {
                    Log.w(TAG, "Thread: onDrawFrame tid=" + getId());
                }

                {
                    ThreadSurfaceView view = mSurfaceViewWeakRef.get();
                    if (view != null) {
                        try {
                            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "onDrawFrame");

                            final SurfaceHolder holder = view.getHolder();
                            if (holder != null) {
                                final Canvas canvas = holder.lockCanvas();
                                if (canvas != null) {
                                    try {
                                        view.mRenderer.onDrawFrame(canvas);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        holder.unlockCanvasAndPost(canvas);
                                    }
                                }

                            }
                        } finally {
                            Trace.traceEnd(Trace.TRACE_TAG_VIEW);
                        }
                    }
                }
            }
        }

        public void surfaceCreated() {
            synchronized(CUSTOM_THREAD_MANAGER_INSTANCE) {
                if (LOG_THREADS) {
                    Log.i(TAG, "main : surfaceCreated tid=" + getId());
                }
                mHasSurface = true;
                CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
                while (mWaitingForSurface && !mExited) {
                    try {
                        CUSTOM_THREAD_MANAGER_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized(CUSTOM_THREAD_MANAGER_INSTANCE) {
                if (LOG_THREADS) {
                    Log.i(TAG, "main: surfaceDestroyed tid=" + getId());
                }
                mHasSurface = false;
                CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
                while(!mWaitingForSurface && !mExited) {
                    try {
                        CUSTOM_THREAD_MANAGER_INSTANCE.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (CUSTOM_THREAD_MANAGER_INSTANCE) {
                mWidth = w;
                mHeight = h;
                mSizeChanged = true;
                mRequestRender = true;

                // If we are already on the GL thread, this means a client callback
                // has caused reentrancy, for example via updating the SurfaceView parameters.
                // We need to process the size change eventually though and update our EGLSurface.
                // So we set the parameters and return so they can be processed on our
                // next iteration.
                if (Thread.currentThread() == this) {
                    return;
                }

                CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();

                // Wait for thread to react to resize and render a frame
                while (!mExited && ableToDraw()) {
                    if (LOG_SURFACE) {
                        Log.i(TAG, "main : onWindowResize waiting for render complete from tid=" + getId());
                    }
                    try {
                        CUSTOM_THREAD_MANAGER_INSTANCE.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        private boolean ableToDraw() {
            return readyToDraw();
        }

        private boolean readyToDraw() {
            return mHasSurface
                    && (mWidth > 0) && (mHeight > 0)
                    && (mRequestRender || (mRenderMode == RENDERMODE_CONTINUOUSLY));
        }

        public int getRenderMode() {
            synchronized(CUSTOM_THREAD_MANAGER_INSTANCE) {
                return mRenderMode;
            }
        }

        public void requestRender() {
            synchronized(CUSTOM_THREAD_MANAGER_INSTANCE) {
                mRequestRender = true;
                CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
            }
        }

        public void requestExitAndWait() {
            // don't call this from GLThread thread or it is a guaranteed
            // deadlock!
            synchronized(CUSTOM_THREAD_MANAGER_INSTANCE) {
                mShouldExit = true;
                CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
                while (!mExited) {
                    try {
                        CUSTOM_THREAD_MANAGER_INSTANCE.wait();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void setRenderMode(int renderMode) {
            if ( !((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY)) ) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized(CUSTOM_THREAD_MANAGER_INSTANCE) {
                mRenderMode = renderMode;
                CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
            }
        }

        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized(CUSTOM_THREAD_MANAGER_INSTANCE) {
                mEventQueue.add(r);
                CUSTOM_THREAD_MANAGER_INSTANCE.notifyAll();
            }
        }
    }

    private static class CustomThreadManager {

        public synchronized void threadExiting(CustomThread thread) {
            if (LOG_THREADS) {
                Log.i(TAG, "exiting tid=" +  thread.getId());
            }
            thread.mExited = true;
            notifyAll();
        }
    }
}