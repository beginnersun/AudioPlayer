package com.example.audioplayer.scanner

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.audioplayer.Utils
import com.example.audioplayer.VoiceBean
import com.example.audioplayer.scanner.WeChatScanner.Companion.userDir
import com.example.audioplayer.scanner.WeChatScanner.Companion.voiceName
import java.io.File

class WeChatScannerImpl : WeChatScanner {

    override fun discoverUserVoice(userCode: String): MutableList<VoiceBean> {
        val voiceDir = getUserVoiceDir(userCode)
        val voiceList = mutableListOf<VoiceBean>()
        discoverAmr(File(voiceDir),voiceList,userCode)
        return voiceList
    }

    override fun discoverUserVoice(LifecycleOwner: LifecycleOwner, userCode: String, callback: DiscoverCallback) {
        LifecycleOwner.lifecycle.addObserver(callback)
        val voiceDir = getUserVoiceDir(userCode)
        discoverAmr(File(voiceDir),userCode,callback)
    }


    override fun discoverUsersVoice(): MutableMap<String, MutableList<VoiceBean>> {
        val dirs = discoverUsersDir()
        val usersVoiceMap = mutableMapOf<String,MutableList<VoiceBean>>()
        if (dirs!=null && dirs.isNotEmpty()) {
            for (dir in dirs) {
                val userCode = File(dir).name
                val voiceDir = getUserVoiceDir(userCode)
                val voiceList = mutableListOf<VoiceBean>()
                discoverAmr(File(voiceDir), voiceList, userCode)
                usersVoiceMap[userCode] = voiceList
            }
        }
        return usersVoiceMap
    }

    override fun discoverUsersVoice(lifecycleOwner: LifecycleOwner, callback: DiscoverCallback) {
        lifecycleOwner.lifecycle.addObserver(callback)
        val dirs = discoverUsersDir()
        if (dirs != null && dirs.isNotEmpty()) {
            for (dir in dirs) {
                val userCode = File(dir).name
                val voiceDir = getUserVoiceDir(userCode)
                discoverAmr(File(voiceDir), userCode, callback)
            }
        }
    }


    private fun discoverUserDir(userCode:String):String{
        return "${userDir}${File.separator}$userCode"
    }

    private fun discoverUsersDir():MutableList<String>{
        val files = mutableListOf<String>()
        val dirs = File(userDir).listFiles()
        Log.e("寻找根目录","${File(userDir).absolutePath}")
        if (dirs!= null){
            Log.e("寻找到的目录11数","${dirs.size}")
        }
        if (dirs!= null && dirs.isNotEmpty()) {
            for (dir in dirs) {
                if (dir.name.length >= 16) {
                    files.add(dir.absolutePath)
                }
            }
        }
        if (files!= null){
            Log.e("寻找到的目录数","${files.size}")
        }
        return files
    }

    private fun getUsersVoiceDir(files:MutableList<String>):MutableList<String>{
        val voiceDirs = mutableListOf<String>()
        if (files!= null && files.isNotEmpty()) {
            for (file in files) {
                val voice = "${file}${File.separator}${voiceName}"
                voiceDirs.add(voice)
            }
        }
        return voiceDirs
    }

    private fun getUserVoiceDir(userCode:String):String{
        return "${discoverUserDir(userCode)}${File.separator}${voiceName}"
    }

    private fun discoverAmr(file:File, userCode:String,callback: DiscoverCallback){
        if (file.isDirectory){
            if (file.listFiles().isNotEmpty()) {
                for (item in file.listFiles()) {
                    discoverAmr(item, userCode, callback)
                }
            }
        }else{
            Log.e("单独文件寻找",file.absolutePath)
            if (file.name.toLowerCase().endsWith(".amr")) {
                callback.received(file,userCode)
            }
        }
    }

    private fun discoverAmr(file:File, voiceList:MutableList<VoiceBean>, userCode:String){
        if (file.isDirectory){
            if (file.listFiles().isNotEmpty()) {
                for (item in file.listFiles()) {
                    discoverAmr(item, voiceList, userCode)
                }
            }
        }else{
            Log.e("单独文件寻找",file.absolutePath)
            if (file.name.toLowerCase().endsWith(".amr")) {
                val voiceBean = VoiceBean()
                voiceBean.createTime =
                    Utils.getTimeFormat(file.lastModified())
                voiceBean.path = file.absolutePath
                voiceBean.userCode = userCode
                voiceList.add(voiceBean)
            }
        }
    }

}