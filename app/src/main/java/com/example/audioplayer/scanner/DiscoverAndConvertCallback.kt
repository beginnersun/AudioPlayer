package com.example.audioplayer.scanner

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.example.audioplayer.*
import com.example.audioplayer.sqlite.Voice
import java.io.File

/**
 * 扫描并且直接转为voiceBean,耗时较长并且进行解码操作必须在主线程所以要处理好callBack的生命周期(不过绑定了lifecycleOwner一般不会出问题)
 */
abstract class DiscoverAndConvertCallback : WeChatScanner.BaseDiscoverCallback {

    companion object {
        const val TAG = "DiscoverAndConvertCallback"
        const val SUCCESS = 0
        const val FAIL = 1
        const val FINISHED = 2
        const val NOT_FOUND = 3
        const val ALREADY_EXIST = 4
    }

    override fun registerLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun unregisterLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    private var onDestroy = false
    private var sumVoice = 0
    private var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                ALREADY_EXIST -> {
                    sumVoice++
                    val it = msg.obj as Voice
                    onReceived(it)
                }
                SUCCESS -> {
                    val voiceBean = msg.obj as Voice
                    sumVoice++
                    onReceived(voiceBean)
                }
                FAIL -> {
                    onError(msg?.obj as String)
                }
                FINISHED -> {
                    onFinished(sumVoice)
                    sumVoice = 0
                }
                NOT_FOUND -> {
                    sumVoice = 0
                    onFinished(sumVoice)
                }
            }
        }
    }

    private fun dealMessage(
        pcmPath: String,
        mp3Path: String,
        file: File,
        userCode: String
    ): Voice? {
        val changed = changeAmrToMp3(file.absolutePath, pcmPath, mp3Path)
        var duration = 0
        if (changed) {
            duration = getMediaDuration(mp3Path)
        }
        return if (duration != 0 && changed) {
            Voice.convertToVoiceBean(file).apply {
                this.user = userCode
                this.mp3Path = mp3Path
                this.pcmPath = pcmPath
                this.minutes = duration
            }
        } else {
            null
        }
    }

    abstract fun onReceived(voice: Voice)

    abstract fun onError(error: String)

    abstract fun onFinished(num: Int)

    override fun received(file: File, userCode: String) {
        if (onDestroy) {
            return
        }
        if (file.name.toLowerCase().endsWith(".amr")) {
            val message = handler.obtainMessage()
            var voice = VoiceApplication.instance().getAppDataBase().voiceDao()
                ?.findBySrcPath(file.absolutePath)
            if (voice != null) {  //已存在
                message.what = ALREADY_EXIST
                message.obj = voice
                message.sendToTarget()
                return
            }
            val mp3Path = getExternalPath(AUDIO_MP3_TYPE)
            val pcmPath = getExternalPath(AUDIO_PCM_TYPE)
            voice = dealMessage(pcmPath, mp3Path, file, userCode)
            if (voice != null){
                VoiceApplication.instance().getAppDataBase().voiceDao()?.insert(voice)
            }
            message.run {
                what = SUCCESS
                obj = voice
                sendToTarget()
            }
        } else {
            val message = handler.obtainMessage()
            message.what = DiscoverCallback.FAIL
            message.obj = "文件结尾不正确"
            handler.sendMessage(message)
        }
    }

    override fun onCompleted(error: Boolean) {
        if (error) {
            handler.obtainMessage().apply {
                what = NOT_FOUND
            }.sendToTarget()
        } else {
            handler.obtainMessage(FINISHED).sendToTarget()
        }
    }

    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreated() {
        Log.d(TAG, "onCreated: ")
    }

    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onStart() {
        Log.d(TAG, "onStart: ")
    }

    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onResume() {
        Log.d(TAG, "onResume: ")
    }

    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onPause() {
        Log.d(TAG, "onPause: ")
    }

    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onStop() {
        Log.d(TAG, "onStop: ")
    }

    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        onDestroy = true
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "onDestroy: ")
    }

    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun customMethod() {
        Log.d(TAG, "customMethod: ")
    }


    @SuppressLint("LongLogTag")
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    open fun onAny() { //此方法可以有参数，但类型必须如两者之一(LifecycleOwner owner,Lifecycle.Event event)
        Log.d(TAG, "onAny: ")
    }

}
