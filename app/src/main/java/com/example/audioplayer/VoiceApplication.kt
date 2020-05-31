package com.example.audioplayer

import android.app.Application
import androidx.room.Room
import com.example.audioplayer.sqlite.AppDataBase
import com.example.audioplayer.sqlite.Voice
import com.umeng.commonsdk.UMConfigure

class VoiceApplication:Application() {

    private lateinit var appDataBase:AppDataBase

    companion object {
        private var instance: VoiceApplication? = null
        fun instance() = instance!!
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appDataBase = Room.databaseBuilder(this,AppDataBase::class.java,"voice").allowMainThreadQueries().build()

    }

    fun getAppDataBase():AppDataBase{
        return if (appDataBase != null){
            appDataBase
        }else{
            appDataBase = Room.databaseBuilder(this,AppDataBase::class.java,"voice").allowMainThreadQueries()
                .build()
            appDataBase
        }
    }

}