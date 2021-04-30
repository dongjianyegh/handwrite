package com.dongjianye.handwrite.surfacerender;

import android.annotation.AnyThread;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.dongjianye.handwrite.HandWriteView;
import com.dongjianye.handwrite.R;
import com.dongjianye.handwrite.base.HandWriteMotionTask;
import com.dongjianye.handwrite.base.HandWriteTask;
import com.dongjianye.handwrite.base.HandWriteTaskType;
import com.dongjianye.handwrite.base.HandWriteUpTask;

import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.NonNull;

/**
 * @author dongjianye on 4/30/21
 */
public class HandWriteRender extends CacheSurfaceRender<HandWriteSurfaceView> {

    private Bitmap mBitmap;

    private final ConcurrentLinkedQueue<HandWriteTask> mHandWriteTasks;

    public HandWriteRender(HandWriteSurfaceView host) {
        super(host);
        mHandWriteTasks = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        mBitmap = BitmapFactory.decodeResource(mSurfaceView.getContext().getResources(), R.drawable.sky_brush, options);
    }

    @Override
    protected void drawFrameCache(Canvas canvas) {
        final long currentTimeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - currentTimeMillis <= 16) {
            HandWriteTask peek = mHandWriteTasks.peek();
            if (peek == null) {
                break;
            }

            switch (peek.getTaskType()) {
                case DRAW_MOTION_EVENT_TASK: {
                    HandWriteTask poll = mHandWriteTasks.poll();
                    while (poll != null) {
                        Log.d("HandWriteRender", "task size is " + mHandWriteTasks.size());
                        MotionEvent motionEvent = ((HandWriteMotionTask) poll).getMotionEvent();
                        if (motionEvent != null) {
                            canvas.drawBitmap(mBitmap, motionEvent.getX(), motionEvent.getY(), null);
                        }
                        HandWriteTask top = mHandWriteTasks.peek();
                        poll = (top == null || top.getTaskType() != HandWriteTaskType.DRAW_MOTION_EVENT_TASK) ? null : mHandWriteTasks.poll();
                    }
                    break;
                }
            }
        }
    }

    @AnyThread
    public void appendMotionEvent(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//            mHandWriteTasks.add(new HandWriteUpTask(obtain));
        } else {
            mHandWriteTasks.add(new HandWriteMotionTask(obtain));
        }
    }
}