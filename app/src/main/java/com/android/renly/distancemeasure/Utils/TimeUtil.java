package com.android.renly.distancemeasure.Utils;

import android.os.SystemClock;

public class TimeUtil {
    /**
     * 根据毫秒返回时分秒
     * @param time
     * @return
     */
    public static String getFormatHMS(long time){
        time/=10;
        int mm = (int) (time%100);
        time/=100;//总秒数
        int s = (int) (time%60);//秒
        int m = (int) (time/60);//分
        return String.format("%02d:%02d.%02d",m,s,mm);
    }

    /**
     * 将String类型的时间转换成long,如：12:01:08
     *
     * @param strTime String类型的时间
     * @return long类型的时间
     */
    public static long convertStrTimeToLong(String strTime) {
        String[] timeArry = strTime.split(":");
        long longTime = 0;
        if (timeArry.length == 2) {//如果时间是MM:SS格式
            longTime = Integer.parseInt(timeArry[0]) * 1000 * 60 + Integer.parseInt(timeArry[1]) * 1000;
        } else if (timeArry.length == 3) {//如果时间是HH:MM:SS格式
            longTime = Integer.parseInt(timeArry[0]) * 1000 * 60 * 60 + Integer.parseInt(timeArry[1])
                    * 1000 * 60 + Integer.parseInt(timeArry[2]) * 1000;
        }
        return SystemClock.elapsedRealtime() - longTime;
    }
}
