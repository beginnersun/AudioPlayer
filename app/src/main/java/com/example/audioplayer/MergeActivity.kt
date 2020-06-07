package com.example.audioplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MergeActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge)
        Log.e("合成位置",intent.getStringExtra("path"))
    }

}