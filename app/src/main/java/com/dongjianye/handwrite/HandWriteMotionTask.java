package com.dongjianye.handwrite;

import android.view.MotionEvent;


public class HandWriteMotionTask extends HandWriteTask {
    private static final HandWriteMotionTask iGP = new HandWriteMotionTask(null);
    private MotionEvent iGQ;

    public HandWriteMotionTask(MotionEvent motionEvent) {
        this.mTaskType = HandWriteTaskType.DRAW_MOTION_EVENT_TASK;
        this.iGQ = motionEvent;
    }

    public MotionEvent getMotionEvent() {
        return this.iGQ;
    }
}
