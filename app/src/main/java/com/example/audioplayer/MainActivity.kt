package com.example.audioplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.CompositePageTransformer
import com.example.audioplayer.scanner.DiscoverAndConvertCallback
import com.example.audioplayer.scanner.DiscoverCallback
import com.example.audioplayer.scanner.WeChatScanner.Companion.spaceTimes
import com.example.audioplayer.scanner.WeChatScannerImpl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        discoverAmr()
    }

    private val scannerCallback = object : DiscoverAndConvertCallback(){
        override fun onReceived(voiceBean: VoiceBean) {
            Log.e("名字${voiceBean.targetUser}","时长${voiceBean.minutes}")
        }


        override fun onError(error: String) {

        }
    }

    private fun discoverAmr(){
        lifecycleScope.launch(Dispatchers.IO){
            WeChatScannerImpl().apply {
                discoverUsersVoice(this@MainActivity,scannerCallback)
            }
        }
    }

}
