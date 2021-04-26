package com.dongjianye.handwrite;



public class jny {
    private long aeL;
    private long iGX = Long.MIN_VALUE;
    private long iGY = Long.MAX_VALUE;
    private long iGZ;
    private long lastTime = -1;
    private long start;

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public void end() {
        this.iGZ += System.currentTimeMillis() - this.start;
        this.aeL++;
    }

    public synchronized void reset() {
        this.lastTime = -1;
    }
}
