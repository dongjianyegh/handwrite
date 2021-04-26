package com.dongjianye.handwrite;


public class SizeUtils {
    private static double getSize(double oldX, double oldY, double newX, double newY,
                                  double d5, double d6, double d7, double d8, double d9, double d10, double d11,
                                  double d12, double d13, double distance) {
        double hypot = (Math.hypot(newX - oldX, newY - oldY) * 0.3d) + (0.7d * distance);
        double d15 = d5 * hypot;
        if (d6 < d15) {
            d15 = d6;
        }
        double log = Math.log(d9 / d11) / (d10 - d8);
        double exp = Math.exp(((-log) * hypot) + Math.log(d9) + (log * d8)) * d12;
        double d16 = exp - d12;
        float f = 1.0f;
        if (Math.abs(d16) / d12 > d15) {
            double d17 = (double) (d16 > 0.0d ? 1.0f : -1.0f);
            Double.isNaN(d17);
            exp = d12 * ((d15 * d17) + 1.0d);
        }
        double d18 = exp - d13;
        if (Math.abs(d18) / d13 <= d7) {
            return exp;
        }
        if (d18 <= 0.0d) {
            f = -1.0f;
        }
        double d19 = (double) f;
        Double.isNaN(d19);
        return ((d19 * d7) + 1.0d) * d13;
    }

    public static double getSize(double oldX, double oldY, double newX, double newY, double d5, double d6, double distance) {
        return getSize(oldX, oldY, newX, newY, 0.05d, 0.5d, 0.5d, 1.0d, 1.0d, 120.0d, 0.25d, d5, d6, distance);
    }
}
