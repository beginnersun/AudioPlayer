package com.example.audioplayer;


import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

public class Utils {


    public static String getTimeFormat(long  time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日-hh:mm:ss");
        Date date = new Date();
        date.setTime(time);
        ArrayList<String> list = new ArrayList<>();

        return format.format(date);
    }

}
