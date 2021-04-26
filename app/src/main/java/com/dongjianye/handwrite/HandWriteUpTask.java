package com.dongjianye.handwrite;

import android.view.MotionEvent;


public class HandWriteUpTask extends HandWriteTask {
    private static final HandWriteUpTask iGW = new HandWriteUpTask(null);
    private MotionEvent iGQ;

    public HandWriteUpTask(MotionEvent motionEvent) {
        this.mTaskType = HandWriteTaskType.UP_MOTION_EVENT_TASK;
        this.iGQ = motionEvent;
    }

    public MotionEvent getMotionEvent() {
        return this.iGQ;
    }
}
