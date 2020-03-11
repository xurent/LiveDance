package com.xurent.myplayer.util;

public class WlTimeUtil {

    public static String secdsToDateFormat(int sedc, int total) {

        long hours = sedc / (60 * 60);
        long minutes = (sedc % (60 * 60)) / 60;
        long seconds = sedc % 60;
        String sh = "00";
        if (hours > 0) {
            if (hours < 10) {
                sh = "0" + hours;

            } else {
                sh = hours + "0";
            }

        }
        String sm = "00";
        if (minutes > 0) {
            if (minutes < 10) {
                sm = "0" + minutes;
            } else {
                sm = minutes + "";
            }
        }

        String ss = "00";
        if (seconds > 0) {
            if (seconds < 10) {
                ss = "0" + seconds;
            } else {
                ss = seconds + "";
            }
        }
        if (total >= 3600) {
            return sh + ":" + sm + ":" + ss;
        }
        return sm + ":" + ss;
    }

}
