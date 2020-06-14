package com.example.audioplayer

import android.content.Context
import android.os.Environment
import android.preference.PreferenceManager
import androidx.core.content.edit
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File

class VoiceConfig private constructor(private val context:Context) {

    var voiceSavePath = ""
        private set
    var voiceSrcPath = ""
        private set

    fun install(){
        voiceSavePath =  context.defaultSharedPreferences.getString(KEY_VOICE_SAVE_DIR,"${Environment.getExternalStorageDirectory()}${File.separator}audioPlayer${File.separator}")
        voiceSrcPath = context.defaultSharedPreferences.getString(
            KEY_VOICE_Src_DIR,"${Environment.getExternalStorageDirectory()}${File.separator}tencent${File.separator}MicroMsg")
    }

    fun getNameByCode(code:String):String =
        context!!.defaultSharedPreferences.getString(
            code,
            code
        )


    fun putNameInCode(code:String,name:String){
        context?.defaultSharedPreferences?.edit {
            putString(code,name)
        }
    }


    fun putVoiceSaveDir(savePath:String){
        this.voiceSavePath = savePath
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply{
            putString(KEY_VOICE_SAVE_DIR,voiceSavePath)
            apply()
        }
    }

    companion object{

        const val KEY_VOICE_SAVE_DIR = "Key_Voice_Save_Dir"

        const val KEY_VOICE_Src_DIR = "Key_Voice_Src_Dir"

        val instance:VoiceConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED){
            VoiceConfig(VoiceApplication.instance())
        }
    }

}