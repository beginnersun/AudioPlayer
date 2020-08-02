package com.example.audioplayer

class StringUtil {


    external fun stringFromJNI():String

    companion object{
        init{
            System.loadLibrary("native-lib")
        }
    }

}