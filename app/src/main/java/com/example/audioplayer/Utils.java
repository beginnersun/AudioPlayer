package com.example.audioplayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Utils {


    public static String getTimeFormatNoDay(long  time){
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
        Date date = new Date();
        date.setTime(time);
        ArrayList<String> list = new ArrayList<>();

        return format.format(date);
    }

    public static String getTimeFormatNoMinute(long  time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh点");
        Date date = new Date();
        date.setTime(time);
        ArrayList<String> list = new ArrayList<>();

        return format.format(date);
    }

    public static String getTimeFormatNoYear(long  time){
        SimpleDateFormat format = new SimpleDateFormat("MM-dd hh点");
        Date date = new Date();
        date.setTime(time);
        ArrayList<String> list = new ArrayList<>();

        return format.format(date);
    }


    public static String getVoiceTime(long  time){
//        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日-hh:mm:ss");
        Date date = new Date();
        date.setTime(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar currentCalendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        int cYear = currentCalendar.get(Calendar.YEAR);
        int cMonth = currentCalendar.get(Calendar.MONTH);
        int cDay = currentCalendar.get(Calendar.DAY_OF_YEAR);
        if (year == cYear){
            if (month == cMonth){
                if (day == cDay){
                    return "今天 "+calendar.get(Calendar.HOUR_OF_DAY)+"点";
                }else if (cDay - day == 1){
                    return "昨天 "+calendar.get(Calendar.HOUR_OF_DAY)+"点";
                }else if (cDay - day == 2){
                    return "前天 "+calendar.get(Calendar.HOUR_OF_DAY)+"点";
                }else {
                    return getTimeFormatNoYear(time);
                }
            }else {
                return getTimeFormatNoYear(time);
            }
        }else {
            return getTimeFormatNoMinute(time);
        }
    }

    public static int dayDValue(long time1,long time2){
        long dValue = Math.max(time1,time2) - Math.min(time1,time2);
        return (int) (dValue/1000/60/60/24);
    }

}
