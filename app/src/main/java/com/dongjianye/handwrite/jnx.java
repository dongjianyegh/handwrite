package com.dongjianye.handwrite;


public class jnx {
    private static double a(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12, double d13, double d14) {
        double hypot = (Math.hypot(d3 - d, d4 - d2) * 0.3d) + (0.7d * d14);
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

    public static double a(double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        return a(d, d2, d3, d4, 0.05d, 0.5d, 0.5d, 1.0d, 1.0d, 120.0d, 0.25d, d5, d6, d7);
    }
}
