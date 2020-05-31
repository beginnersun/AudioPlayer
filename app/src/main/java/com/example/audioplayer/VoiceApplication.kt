package com.example.audioplayer

import android.app.Application
import androidx.room.Room
import com.example.audioplayer.sqlite.AppDataBase
import com.example.audioplayer.sqlite.Voice
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig

//import com.umeng.commonsdk.UMConfigure

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
        PlatformConfig.setQQZone("1108208362","Dp7nyNV9Qm78AmcB")

        UMConfigure.init(this,null,"huawei", UMConfigure.DEVICE_TYPE_PHONE,"")
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