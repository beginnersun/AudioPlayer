package com.example.audioplayer.scanner

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.audioplayer.Utils
import com.example.audioplayer.sqlite.Voice
import com.example.audioplayer.scanner.WeChatScanner.Companion.defaultSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.userDir
import com.example.audioplayer.scanner.WeChatScanner.Companion.voiceName
import java.io.File

/**
 * @param enoughTime true 代表有足够的时间去扫描
 * count 代表当前加载的次数(count == 1)代表第一次加载
 */
class WeChatScannerImpl(
    var count: Int = 1,
    var spaceTime: Long = defaultSpaceTime,
    var enoughTime: Boolean = false
) : WeChatScanner {

    private val sizeMap:MutableMap<String,Int> = mutableMapOf()

    override fun discoverUserVoice(userCode: String): MutableList<Voice> {
        val voiceDir = getUserVoiceDir(userCode)
        val voiceList = mutableListOf<Voice>()
        discoverAmr(File(voiceDir), voiceList, userCode)
        return voiceList
    }

    override fun discoverUserVoice(
        userCode: String,
        callback: WeChatScanner.BaseDiscoverCallback
    ) {
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
        callback: WeChatScanner.BaseDiscoverCallback
    ) {
        val dirs = discoverUsersDir()
        if (dirs.isNotEmpty()) {
            for (dir in dirs) {
                val userCode = File(dir).name
                val voiceDir = getUserVoiceDir(userCode)
                discoverAmr(File(voiceDir), userCode, callback)
            }
            callback.onCompleted(false)
        } else {
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
                Log.e("${file.name}", "")
                val targetName = convertPathToUserCode(file.absolutePath)
                var nameSize = if (sizeMap[targetName]!=null) {
                    sizeMap[targetName]
                }else {
                    0
                }
                if ((inSpaceTime(file)) || enoughTime) {
                    sizeMap[targetName] = (nameSize!!+1)
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
                val voiceBean = Voice.convertToVoiceBean(file)
                voiceList.add(voiceBean)
            }
        }
    }


    private fun convertPathToUserCode(path:String):String{
        val start = path.length -11 - 5 -1   //-11 代表去掉随机生成的7位字符+后缀 -5代表不变的code  -1是因为下标从0开始
        if (start > 0 && path.length > start && path.length > start + 5) {
            return path.substring(start, start + 5)
        }
        return ""
    }

    /**
     * 处于间隔时间内  (默认为间隔时间为一个月)
     */
    private fun inSpaceTime(file: File) = System.currentTimeMillis() - file.lastModified() >= (count - 1) * spaceTime
            && System.currentTimeMillis() - file.lastModified() < count * spaceTime

}