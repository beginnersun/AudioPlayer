package com.example.audioplayer.scanner

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.audioplayer.Utils
import com.example.audioplayer.sqlite.Voice
import com.example.audioplayer.scanner.WeChatScanner.Companion.defaultSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.userDir
import com.example.audioplayer.scanner.WeChatScanner.Companion.voiceName
import java.io.File

class WeChatScannerImpl(var spaceTime: Long = defaultSpaceTime,var enoughTime:Boolean = false) : WeChatScanner {

    override fun discoverUserVoice(userCode: String): MutableList<Voice> {
        val voiceDir = getUserVoiceDir(userCode)
        val voiceList = mutableListOf<Voice>()
        discoverAmr(File(voiceDir), voiceList, userCode)
        return voiceList
    }

    override fun discoverUserVoice(
        LifecycleOwner: LifecycleOwner,
        userCode: String,
        callback: WeChatScanner.BaseDiscoverCallback
    ) {
        LifecycleOwner.lifecycle.addObserver(callback)
        val voiceDir = getUserVoiceDir(userCode)
        discoverAmr(File(voiceDir), userCode, callback)
    }


    override fun discoverUsersVoice(): MutableMap<String, MutableList<Voice>> {
        val dirs = discoverUsersDir()
        val usersVoiceMap = mutableMapOf<String, MutableList<Voice>>()
        if (dirs.isNotEmpty()) {
            for (dir in dirs) {
                val userCode = File(dir).name
                val voiceDir = getUserVoiceDir(userCode)
                val voiceList = mutableListOf<Voice>()
                discoverAmr(File(voiceDir), voiceList, userCode)
                usersVoiceMap[userCode] = voiceList
            }
        }
        return usersVoiceMap
    }

    /**
     * 扫描音频并回调 （游客模式)
     */
    override fun discoverUsersVoice(
        lifecycleOwner: LifecycleOwner,
        callback: WeChatScanner.BaseDiscoverCallback
    ) {
        lifecycleOwner.lifecycle.addObserver(callback)
        val dirs = discoverUsersDir()
        if (dirs.isNotEmpty()) {
            for (dir in dirs) {
                val userCode = File(dir).name
                val voiceDir = getUserVoiceDir(userCode)
                discoverAmr(File(voiceDir), userCode, callback)
            }
            callback.onCompleted(false)
        }else{
            callback.onCompleted(true)
        }
    }


    private fun discoverUserDir(userCode: String): String {
        return "${userDir}${File.separator}$userCode"
    }

    private fun discoverUsersDir(): MutableList<String> {
        val files = mutableListOf<String>()
        val dirs = File(userDir).listFiles()
        if (dirs != null && dirs.isNotEmpty()) {
            for (dir in dirs) {
                if (dir.name.length >= 16) {
                    files.add(dir.absolutePath)
                }
            }
        }
        return files
    }

    private fun getUsersVoiceDir(files: MutableList<String>): MutableList<String> {
        val voiceDirs = mutableListOf<String>()
        if (files.isNotEmpty()) {
            for (file in files) {
                val voice = "${file}${File.separator}${voiceName}"
                voiceDirs.add(voice)
            }
        }
        return voiceDirs
    }

    private fun getUserVoiceDir(userCode: String): String {
        return "${discoverUserDir(userCode)}${File.separator}${voiceName}"
    }

    private fun discoverAmr(
        file: File,
        userCode: String,
        callback: WeChatScanner.BaseDiscoverCallback
    ) {
        if (file.isDirectory) {
            if (file.listFiles().isNotEmpty()) {
                for (item in file.listFiles()) {
                    discoverAmr(item, userCode, callback)
                }
            }
        } else {
            if (file.name.toLowerCase().endsWith(".amr")) {
                Log.e("扫描信息${file.absolutePath}","${spaceTime}")
                if (inSpaceTime(file) || enoughTime) {
                    callback.received(file, userCode)
                }
            }
        }
    }

    private fun discoverAmr(file: File, voiceList: MutableList<Voice>, userCode: String) {
        if (file.isDirectory) {
            if (file.listFiles().isNotEmpty()) {
                for (item in file.listFiles()) {
                    discoverAmr(item, voiceList, userCode)
                }
            }
        } else {
            if (file.name.toLowerCase().endsWith(".amr")) {
                val voiceBean = Voice()
                voiceBean.createTime =
                    Utils.getTimeFormat(file.lastModified())
                voiceBean.path = file.absolutePath
                voiceBean.userCode = userCode
                voiceList.add(voiceBean)
            }
        }
    }

    /**
     * 处于间隔时间内  默认为一个月
     */
    private fun inSpaceTime(file:File) = System.currentTimeMillis() - file.lastModified() < spaceTime

}