package com.android.renly.distancemeasure.Utils;

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
}
