package com.example.audioplayer.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.audioplayer.R
import com.example.audioplayer.ui.CollectVoiceActivity
import com.example.audioplayer.ui.VoiceSettingActivity
import kotlinx.android.synthetic.main.fragment_my.*

class MyFragment:Fragment() {

    var tagg = "MyFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e(tagg,"")
        return inflater.inflate(R.layout.fragment_my,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_voice_info.setOnClickListener {
            startActivity(Intent(activity!!,VoiceSettingActivity::class.java))
        }

        tv_collect_voice.setOnClickListener {
            startActivity(Intent(activity!!,CollectVoiceActivity::class.java))
        }
    }


}