package com.dongjianye.handwrite;


public class jnz {
    private a mCurPosition = new a();
    private a iHb = new a();
    private a mCenterPosition = new a();
    private a mOldPosition = new a();

    private double a(double d, double d2, double d3, double d4) {
        double d5 = d2 * 2.0d;
        return ((d5 - (2.0d * d)) * d4) + d + (((d - d5) + d3) * d4 * d4);
    }


    public static class a {
        public double gi;
        public double x;
        public double y;

        public void init(double x, double y, double size) {
            this.x = x;
            this.y = y;
            this.gi = size;
        }
    }

    public void a(double newX, double newY, double newSize, double oldX, double oldY, double oldSize) {
        this.mCurPosition.init(newX, newY, newSize);
        double centerX = (newX + oldX) / 2.0d;
        double centerY = (newY + oldY) / 2.0d;
        double size = (newSize + oldSize) / 2.0d;
        this.mCenterPosition.init(centerX, centerY, size);
        this.iHb.init((centerX + newX) / 2.0d, (centerY + newY) / 2.0d, (size + newSize) / 2.0d);
        this.mOldPosition.init(oldX, oldY, oldSize);
    }

    public void b(double d, double d2, double d3) {
        this.mCurPosition.init(this.mCenterPosition.x, this.mCenterPosition.y, this.mCenterPosition.gi);
        this.iHb.init(this.mOldPosition.x, this.mOldPosition.y, this.mOldPosition.gi);
        this.mCenterPosition.init((this.mOldPosition.x + d) / 2.0d, (this.mOldPosition.y + d2) / 2.0d, (this.mOldPosition.gi + d3) / 2.0d);
        this.mOldPosition.init(d, d2, d3);
    }

    public void end() {
        this.mCurPosition.init(this.mCenterPosition.x, this.mCenterPosition.y, this.mCenterPosition.gi);
        this.iHb.init((this.mOldPosition.x + this.mCurPosition.x) / 2.0d, (this.mOldPosition.y + this.mCurPosition.y) / 2.0d, (this.mOldPosition.gi + this.mCurPosition.gi) / 2.0d);
        this.mCenterPosition.init(this.mOldPosition.x, this.mOldPosition.y, this.mOldPosition.gi);
    }

    public void a(a aVar, double d) {
        aVar.init(m(d), n(d), o(d));
    }

    private double m(double d) {
        return a(this.mCurPosition.x, this.iHb.x, this.mCenterPosition.x, d);
    }

    private double n(double d) {
        return a(this.mCurPosition.y, this.iHb.y, this.mCenterPosition.y, d);
    }

    private double o(double d) {
        return a(this.mCurPosition.gi, this.iHb.gi, this.mCenterPosition.gi, d);
    }
}
