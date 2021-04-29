package com.dongjianye.handwrite.base;

/**
 * @author dongjianye on 4/28/21
 */
public class Utils {

    public static int getNextPower2(int value) {
        int leftShift = 1;
        while (true) {
            int result = 1 << leftShift;
            if (result >= value) {
                return result;
            }
            leftShift++;
        }
    }

}