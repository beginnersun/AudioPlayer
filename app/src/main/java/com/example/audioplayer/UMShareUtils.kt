package com.example.audioplayer

import android.content.Context

class UMShareUtils private constructor(val context:Context){

    companion object{
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            UMShareUtils(VoiceApplication.instance())
        }
    }


}