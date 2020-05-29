package com.example.audioplayer.scanner

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.io.File


abstract class DiscoverCallback:WeChatScanner.BaseDiscoverCallback, LifecycleObserver {

    companion object{
        const val TAG = "DiscoverCallback"
        const val SUCCESS = 0
        const val FAIL = 1
    }

    private var onDestroy = false
    private var handler:Handler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            when(msg?.what){
                SUCCESS -> {
                    msg?.let {
                        val file = File(it.data.getString("path"))
                        onReceived(file, it.data.getString("code"))
                    }
                }
                FAIL -> {
                    onError("")
                }
            }
        }
    }

    abstract fun onReceived(file:File,userCode:String)

    abstract fun onError(error:String)

    /**
     * 回调有可能是在子线程执行
     */
    @WorkerThread
     override fun received(file: File,userCode: String) {
        if (onDestroy){
            return
        }
        Log.e("暂时寻找","${file.absolutePath}")
        if (file.name.toLowerCase().equals(".amr")){
            val message = handler.obtainMessage()
            message.what = SUCCESS
            message.data = Bundle().apply {
                putString("path",file.absolutePath)
                putString("code",userCode)
            }
            handler.sendMessage(message)
        }else{
            val message = handler.obtainMessage()
            message.what = FAIL
            handler.sendMessage(message)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun onCreated() {
        Log.d(TAG, "onCreated: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onStart() {
        Log.d(TAG, "onStart: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun onResume() {
        Log.d(TAG, "onResume: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    open fun onPause() {
        Log.d(TAG, "onPause: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onStop() {
        Log.d(TAG, "onStop: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun onDestroy() {
        onDestroy = true
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "onDestroy: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun customMethod() {
        Log.d(TAG, "customMethod: ")
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    open fun onAny() { //此方法可以有参数，但类型必须如两者之一(LifecycleOwner owner,Lifecycle.Event event)
        Log.d(TAG, "onAny: ")
    }

}