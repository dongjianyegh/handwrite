package com.dongjianye.handwrite;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;


public class HandWriteHandler {
    private long mDelayTime = 500;
    private Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message message) {
            if (message.what == 1) {
                onFinishWrite();
            }
        }
    };
    private WriteListener mWriteListener = null;
    private boolean mStartWrite = false;

    public HandWriteHandler(WriteListener listener) {
        mWriteListener = listener;
    }
    
    private void onFinishWrite() {
        mWriteListener.onFinishWrite();
        mStartWrite = false;
    }

    public void setDelayTime(long j) {
        mDelayTime = j;
    }

    public void action(int i) {
        if (!mStartWrite) {
            mStartWrite = true;
            mWriteListener.onStartWrite();
        }
        if (i == MotionEvent.ACTION_UP) {
            handler.removeMessages(1);
            handler.sendMessageDelayed(handler.obtainMessage(1), getDelayTime());
        } else if (i == MotionEvent.ACTION_DOWN) {
            handler.removeMessages(1);
        }
    }

    private long getDelayTime() {
        return mDelayTime;
    }
}
