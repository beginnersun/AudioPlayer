package com.example.audioplayer.scanner

import android.os.Environment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.audioplayer.sqlite.Voice
import java.io.File

interface WeChatScanner {

    fun discoverUserVoice(userCode:String):MutableList<Voice>


    fun discoverUsersVoice():MutableMap<String,MutableList<Voice>>


    fun discoverUserVoice(lifecycleOwner: LifecycleOwner,userCode:String,callback: WeChatScanner.BaseDiscoverCallback)


    fun discoverUsersVoice(lifecycleOwner: LifecycleOwner,callback: WeChatScanner.BaseDiscoverCallback)


    companion object{
        val userDir = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}" +
                "tencent${File.separator}" +
                "MicroMsg${File.separator}"

        val voiceName = "voice2"

        val spaceTimes = arrayOf(1 * 30 * 24 * 60 * 60 * 1000.toLong(),3 * 30 * 24 * 60 * 60 * 1000.toLong(),5 * 30 * 24 * 60 * 60 * 1000.toLong())

        const val defaultSpaceTime:Long = (1 * 30 * 24 * 60 * 60 * 1000.toLong())
    }

    interface BaseDiscoverCallback:LifecycleObserver{
        
        fun registerLifecycle(lifecycleOwner: LifecycleOwner)
        
        fun received(file: File,userCode: String)

        fun onCompleted(error:Boolean)
    }

}
