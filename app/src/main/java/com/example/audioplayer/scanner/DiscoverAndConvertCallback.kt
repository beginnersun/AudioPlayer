package com.example.audioplayer.scanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.audioplayer.*
import java.io.File

/**
 * 扫描并且直接转为voiceBean,耗时较长并且进行解码操作必须在主线程所以要处理好callBack的生命周期(不过绑定了lifecycleOwner一般不会出问题)
 */
abstract class DiscoverAndConvertCallback : WeChatScanner.BaseDiscoverCallback {

    companion object {
        const val TAG = "DiscoverAndConvertCallback"
        const val SUCCESS = 0
        const val FAIL = 1
    }

    private var onDestroy = false
    private var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                SUCCESS -> {
                    val data = msg?.data
                    val voiceBean = dealMessage(data)
                    if (voiceBean == null) {
                        onError("格式转换失败")
                    } else {
                        onReceived(voiceBean)
                    }
                }
                FAIL -> {
                    onError(msg?.obj as String)
                }
            }
        }
    }

    private fun dealMessage(data: Bundle): VoiceBean? {
        val file = File(data.getString("path"))
        val pcmPath = data.getString("pcm")
        val mp3Path = data.getString("mp3")
        Log.e("开始处理","1")
        val changed = changeAmrToMp3(file.absolutePath, pcmPath, mp3Path)
        Log.e("开始处理","转换成功")
        var duration = 0
        if (changed) {
            duration = getMediaDuration(mp3Path)
            Log.e("开始处理","音频获取成功")
        }
        return if (duration != 0 && changed) {
            VoiceBean.convertToVoiceBean(file).apply {
                this.user = data.getString("code")
                this.mp3Path = mp3Path
                this.pcmPath = pcmPath
                this.minutes = duration
            }
        } else {
            null
        }
    }

    abstract fun onReceived(voiceBean: VoiceBean)

    abstract fun onError(error: String)

    override fun received(file: File, userCode: String) {
        if (onDestroy) {
            return
        }
        if (file.name.toLowerCase().endsWith(".amr")) {
            val message = handler.obtainMessage()
            val mp3Path = getExternalPath(AUDIO_MP3_TYPE)
            val pcmPath = getExternalPath(AUDIO_PCM_TYPE)
            message.what = SUCCESS
            message.data = Bundle().apply {
                putString("path", file.absolutePath)
                putString("code", userCode)
                putString("mp3", mp3Path)
                putString("pcm", pcmPath)
            }
            handler.sendMessage(message)
        } else {
            val message = handler.obtainMessage()
            message.what = DiscoverCallback.FAIL
            message.obj = "文件结尾不正确"
            handler.sendMessage(message)
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