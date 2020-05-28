package com.example.audioplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.CompositePageTransformer
import com.example.audioplayer.scanner.DiscoverCallback
import com.example.audioplayer.scanner.WeChatScannerImpl
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        FragmentStateAdapter

    }

    private val scannerCallback = object : DiscoverCallback(){
        override fun onReceived(file: File,userCode:String) {
            Log.e("找到amr","${file.absolutePath}")
        }

        override fun onError(error: String) {

        }
    }

    private fun discoverAmr(){
        lifecycleScope.launch(Dispatchers.IO){
            WeChatScannerImpl().discoverUsersVoice(this@MainActivity,scannerCallback)
        }
    }

}
