package com.example.audioplayer.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.example.audioplayer.BuildConfig
import com.example.audioplayer.sqlite.Voice
import java.io.File

object VoiceUtil {

    fun shareMusic(context:Context,path: String) {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context!!,
                BuildConfig.APPLICATION_ID + ".fileprovider", File(path)
            )
        } else {
            Uri.parse(path)
        }
        val intent = Intent(Intent.ACTION_SEND, null)
        intent.type = "*/*"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Log.e("分享文件位置", "$path")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, "分享"))
    }

    /**
     * 检查是否有选中项
     */
    fun checkHaveSelected(voiceList:MutableList<Voice>): Boolean {
        voiceList.forEach {
            if (it.selected) {
                return it.selected
            }
        }
        return false
    }
}