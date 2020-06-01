package com.example.audioplayer.sqlite

import androidx.room.*
import com.example.audioplayer.Utils
import java.io.File

@Fts4
@Entity
class Voice:Comparable<Voice>{
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") @Ignore var vid:Long = 0
    var user:String = ""
    var userCode:String = ""
    var targetUser:String = ""
    var targetName:String = ""//设置的名字 默认是对应code的截取串
    var minutes:Int = 0
    var createTime:String = ""
    var path:String = "" //源文件位置
    var mp3Path:String = ""
    var pcmPath:String = ""
    var like:Boolean = false
    @Ignore var selected:Boolean = false  //是否被选中
    @Ignore var isPlaying:Boolean = false //是否正在播放
    @Ignore var sumSize:Int = 0

    @Ignore var typeName = ""
    @Ignore var itemNum = 0
    @Ignore var open = true //默认处于打开菜单
    constructor(){

    }

    companion object{
        private const val codeLength = 5

        fun convertToVoiceBean(file: File): Voice {
            val voiceBean = Voice()
            voiceBean.createTime =
                Utils.getTimeFormat(file.lastModified())
            voiceBean.path = file.absolutePath
            val start = voiceBean.path.length -11 - codeLength -1   //-11 代表去掉随机生成的7位字符+后缀 -5代表不变的code  -1是因为下标从0开始
            if (start > 0 && voiceBean.path.length > start && voiceBean.path.length > start + codeLength) {
                voiceBean.targetUser = voiceBean.path.substring(start, start + codeLength)
                voiceBean.targetName = voiceBean.targetUser
            }
            return voiceBean
        }

        fun createMenuBean(typeName:String,size:Int):Voice{
            val voiceBean = Voice()
            voiceBean.itemNum = size
            voiceBean.typeName = typeName
            return voiceBean
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Voice) {
            (path == other.path && mp3Path == other.mp3Path)
        }else{
            false
        }
    }

    override fun compareTo(other: Voice): Int =
        when {
            this.createTime > other.createTime -> {
                1
            }
            this.createTime == other.createTime -> {
                0
            }
            else -> {
                -1
            }
        }


}
