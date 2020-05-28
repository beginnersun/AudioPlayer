package com.example.audioplayer.scanner

import android.os.Environment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.audioplayer.VoiceBean
import java.io.File

interface WeChatScanner {

    fun discoverUserVoice(userCode:String):MutableList<VoiceBean>


    fun discoverUsersVoice():MutableMap<String,MutableList<VoiceBean>>


    fun discoverUserVoice(lifecycleOwner: LifecycleOwner,userCode:String,callback: DiscoverCallback)


    fun discoverUsersVoice(lifecycleOwner: LifecycleOwner,callback: DiscoverCallback)


    companion object{
        val userDir = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}" +
                "tencent${File.separator}" +
                "MicroMsg${File.separator}"

        val voiceName = "voice2"
    }

    interface BaseDiscoverCallback{
        fun received(file: File,userCode: String)
    }

}