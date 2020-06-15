package com.example.audioplayer.scanner

import android.util.Log
import com.example.audioplayer.scanner.WeChatScanner.*
import com.example.audioplayer.sqlite.Voice
import com.example.audioplayer.scanner.WeChatScanner.Companion.defaultSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.userDir
import com.example.audioplayer.scanner.WeChatScanner.Companion.voiceName
import java.io.File

class WeChatScannerImpl(var filterStrategy:FilterStrategy):WeChatScanner(filterStrategy){

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

//    /**
//     * 扫描指定好友语音文件
//     */
//    override fun discoverUsersVoiceByTargetName(
//        callback: WeChatScanner.BaseDiscoverCallback
//    ) {
//        val dirs = discoverUsersDir()
//        if (dirs.isNotEmpty()) {
//            for (dir in dirs) {
//                val userCode = File(dir).name
//                val voiceDir = getUserVoiceDir(userCode)
//                discoverAmr(targetUser,File(voiceDir), userCode, callback)
//            }
//            callback.onCompleted(false)
//        } else {
//            callback.onCompleted(true)
//        }
//    }


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

    /**
     * 获取当前用户的语音文件存放根目录
     */
    private fun getUserVoiceDir(userCode: String): String {
        return "${discoverUserDir(userCode)}${File.separator}${voiceName}"
    }

    /**
     * 扫描当前用户所有语音
     */
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
                if (filterStrategy.predicate(file)){
                    callback.received(file, userCode)
                }
//                Log.e("${file.name}", "")
//                if ((inSpaceTime(file)) || enoughTime) {
//                }
            }
        }
    }

//    /**
//     * 扫描某个好友的语音文件
//     */
//    private fun discoverAmr(
//        targetUser: String,
//        file: File,
//        userCode: String,
//        callback: WeChatScanner.BaseDiscoverCallback
//    ) {
//        if (file.isDirectory) {
//            if (file.listFiles().isNotEmpty()) {
//                for (item in file.listFiles()) {
//                    discoverAmr(targetUser,item, userCode, callback)
//                }
//            }
//        } else {
//            if (file.name.toLowerCase().endsWith(".amr")) {
//                Log.e("${file.name}", "")
//                val targetName = convertPathToUserCode(file.absolutePath)
//                Log.e("扫描结果","$targetName    $targetUser")
//
//                if ( ((inSpaceTime(file)) || enoughTime) && targetName == targetUser) {
//                    callback.received(file, userCode)
//                }
//            }
//        }
//    }

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


//    /**
//     * 根据语音文件名 寻找对应用户名Code
//     */
//    private fun convertPathToUserCode(path:String):String{
//        val start = path.length -11 - 5 -1   //-11 代表去掉随机生成的7位字符+后缀 -5代表不变的code  -1是因为下标从0开始
//        if (start > 0 && path.length > start && path.length > start + 5) {
//            return path.substring(start, start + 5)
//        }
//        return ""
//    }
//
//    /**
//     * 处于间隔时间内  (默认为间隔时间为一个月)
//     */
//    private fun inSpaceTime(file: File) = System.currentTimeMillis() - file.lastModified() >= (count - 1) * spaceTime
//            && System.currentTimeMillis() - file.lastModified() < count * spaceTime

}