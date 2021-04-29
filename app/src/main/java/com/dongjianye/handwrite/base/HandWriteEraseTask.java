package com.dongjianye.handwrite.base;


public class HandWriteEraseTask extends HandWriteTask {
    private static final HandWriteEraseTask iGR = new HandWriteEraseTask();

    private HandWriteEraseTask() {
        this.mTaskType = HandWriteTaskType.ERASE_TASK;
    }

    public static HandWriteEraseTask getEraseTask() {
        return iGR;
    }
}
