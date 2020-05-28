package com.example.audioplayer;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String getTimeFormat(long  time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd-hh:mm:ss");
        Date date = new Date();
        date.setTime(time);
        return format.format(date);
    }

}
