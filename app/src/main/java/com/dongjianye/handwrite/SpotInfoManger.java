package com.dongjianye.handwrite;


public class SpotInfoManger {
    private SpotInfo mOldSpot = new SpotInfo(); // 旧点信息
    private SpotInfo mCenterCenterSpot = new SpotInfo(); // 中间点和新点之间的中间点
    private SpotInfo mCenterSpot = new SpotInfo(); // 新点和旧点之间的点
    private SpotInfo mNewSpot = new SpotInfo(); // 新点信息

    /**
     * 插值器：贝塞尔曲线
     * @param from
     * @param center
     * @param to
     * @param ratio
     * @return
     */
    private double a(double from, double center, double to, double ratio) {
        double add = center * 2.0d;
        return ((add - (2.0d * from)) * ratio) + from + (((from - add) + to) * ratio * ratio);
    }


    public static class SpotInfo {
        public double size;
        public double x;
        public double y;

        public void init(double x, double y, double size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }

    public void init(double oldX, double oldY, double oldSize, double newX, double newY, double newSize) {
        this.mOldSpot.init(oldX, oldY, oldSize);
        double centerX = (oldX + newX) / 2.0d;
        double centerY = (oldY + newY) / 2.0d;
        double size = (oldSize + newSize) / 2.0d;
        this.mCenterSpot.init(centerX, centerY, size);
        this.mCenterCenterSpot.init((centerX + oldX) / 2.0d, (centerY + oldY) / 2.0d, (size + oldSize) / 2.0d);
        this.mNewSpot.init(newX, newY, newSize);
    }

    public void append(double x, double y, double size) {
        this.mOldSpot.init(this.mCenterSpot.x, this.mCenterSpot.y, this.mCenterSpot.size);
        this.mCenterCenterSpot.init(this.mNewSpot.x, this.mNewSpot.y, this.mNewSpot.size);
        this.mCenterSpot.init((this.mNewSpot.x + x) / 2.0d, (this.mNewSpot.y + y) / 2.0d, (this.mNewSpot.size + size) / 2.0d);
        this.mNewSpot.init(x, y, size);
    }

    public void end() {
        this.mOldSpot.init(this.mCenterSpot.x, this.mCenterSpot.y, this.mCenterSpot.size);
        this.mCenterCenterSpot.init((this.mNewSpot.x + this.mOldSpot.x) / 2.0d, (this.mNewSpot.y + this.mOldSpot.y) / 2.0d, (this.mNewSpot.size + this.mOldSpot.size) / 2.0d);
        this.mCenterSpot.init(this.mNewSpot.x, this.mNewSpot.y, this.mNewSpot.size);
    }

    public void getRatioSpot(SpotInfo aVar, double ratio) {
        aVar.init(m(ratio), n(ratio), o(ratio));
    }

    private double m(double d) {
        return a(this.mOldSpot.x, this.mCenterCenterSpot.x, this.mCenterSpot.x, d);
    }

    private double n(double d) {
        return a(this.mOldSpot.y, this.mCenterCenterSpot.y, this.mCenterSpot.y, d);
    }

    private double o(double d) {
        return a(this.mOldSpot.size, this.mCenterCenterSpot.size, this.mCenterSpot.size, d);
    }
}
