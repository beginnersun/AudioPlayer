package com.example.audioplayer.scanner

import android.os.Environment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.audioplayer.VoiceBean
import java.io.File

interface WeChatScanner {

    fun discoverUserVoice(userCode:String):MutableList<VoiceBean>


    fun discoverUsersVoice():MutableMap<String,MutableList<VoiceBean>>


    fun discoverUserVoice(lifecycleOwner: LifecycleOwner,userCode:String,callback: WeChatScanner.BaseDiscoverCallback)


    fun discoverUsersVoice(lifecycleOwner: LifecycleOwner,callback: WeChatScanner.BaseDiscoverCallback)


    companion object{
        val userDir = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}" +
                "tencent${File.separator}" +
                "MicroMsg${File.separator}"

        val voiceName = "voice2"

        const val defaultSpaceTime:Long = (1 * 30 * 24 * 60 * 60 * 1000.toLong())
    }

    interface BaseDiscoverCallback:LifecycleObserver{
        fun received(file: File,userCode: String)
    }

}