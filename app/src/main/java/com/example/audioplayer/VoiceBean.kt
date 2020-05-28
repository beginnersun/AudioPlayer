package com.example.audioplayer

import java.io.File

class VoiceBean {
    var user:String = ""
    var userCode:String = ""
    var targetUser:String = ""
    var targetName:String = ""
    var minutes:Int = 0
    var createTime:String = ""
    var path:String = ""

    constructor(){

    }

    companion object{
        private const val codeLength = 5

        fun convertToVoiceBean(file: File):VoiceBean{
            val voiceBean = VoiceBean()
            voiceBean.createTime =
                Utils.getTimeFormat(file.lastModified())
            voiceBean.path = file.absolutePath
            val start = voiceBean.path.length -11 - codeLength -1   //-11 代表去掉随机生成的7位字符+后缀 -5代表不变的code  -1是因为下标从0开始
            voiceBean.targetUser = voiceBean.path.substring(start,start+ codeLength)
            return voiceBean
        }
    }

}