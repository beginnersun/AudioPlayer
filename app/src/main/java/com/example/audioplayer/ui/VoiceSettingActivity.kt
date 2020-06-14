package com.example.audioplayer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.audioplayer.R
import com.example.audioplayer.VoiceConfig
import kotlinx.android.synthetic.main.activity_voice_setting.*


class VoiceSettingActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_setting)

        tv_desc_voice_content.text = VoiceConfig.instance.voiceSavePath
        tv_src_voice_content.text = VoiceConfig.instance.voiceSrcPath

        ll_voice_down_dir.setOnClickListener {
            TODO("跳转到文件夹选择页面")
        }
//
//        tv_add_src.setOnClickListener {
//            TODO("跳转到文件夹选择页面")
//        }
    }


    private fun selectVoiceDir(){

    }

    private fun getVersionName(): String? {
        // 获取packagemanager的实例
        val packageManager = packageManager
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        val packInfo = packageManager.getPackageInfo(packageName, 0)
        return packInfo.versionName
    }
}