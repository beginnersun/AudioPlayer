package com.example.audioplayer

import java.io.File

class VoiceBean {
    var user:String = ""
    var userCode:String = ""
    var targetUser:String = ""
    var minutes:Int = 0
    var createTime:String = ""
    var path:String = ""

    constructor(){

    }

    companion object{
        fun convertToVoiceBean(file: File):VoiceBean{
            val voiceBean = VoiceBean()
            voiceBean.createTime =
                Utils.getTimeFormat(file.lastModified())
            voiceBean.path = file.absolutePath
            return voiceBean
        }
    }

}