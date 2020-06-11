package com.example.audioplayer.sqlite

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.example.audioplayer.Utils
import java.io.File

@Entity
class Voice() :Comparable<Voice>,Parcelable{
    @PrimaryKey(autoGenerate = true) var vid:Long = 0
    var user:String = ""  //当前用户对应的code
    var userCode:String = ""
    var targetUser:String = "" //用户好友对应的code  截取的code串
    var targetName:String = ""//设置的名字 默认是对应code的截取串
    var minutes:Int = 0
    var createTime:String = ""
    var path:String = "" //源文件位置
    var mp3Path:String = ""
    var pcmPath:String = ""
    var like:Boolean = false
    var time:Long = 0
        set(value){
            field = value
            createTime = Utils.getVoiceTime(field)
        }
    var merge:Int = NORMAL_VOICE  //默认
    @Ignore var selected:Boolean = false  //是否被选中
    @Ignore var isPlaying:Boolean = false //是否正在播放

    @Ignore var typeName = ""
    @Ignore var itemNum = 0
    @Ignore var open = true //默认处于打开菜单

    constructor(parcel: Parcel) : this() {
        vid = parcel.readLong()
        user = parcel.readString()
        userCode = parcel.readString()
        targetUser = parcel.readString()
        targetName = parcel.readString()
        minutes = parcel.readInt()
        createTime = parcel.readString()
        path = parcel.readString()
        mp3Path = parcel.readString()
        pcmPath = parcel.readString()
        like = parcel.readByte() != 0.toByte()
//        selected = parcel.readByte() != 0.toByte()
//        isPlaying = parcel.readByte() != 0.toByte()
//        sumSize = parcel.readInt()
//        typeName = parcel.readString()
//        itemNum = parcel.readInt()
//        open = parcel.readByte() != 0.toByte()
    }

//    override fun writeToParcel(dest: Parcel?, flags: Int) {
//
//    }

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

    override fun toString(): String =
        "$targetName  $itemNum  $minutes  $path"

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(vid)
        parcel.writeString(user)
        parcel.writeString(userCode)
        parcel.writeString(targetUser)
        parcel.writeString(targetName)
        parcel.writeInt(minutes)
        parcel.writeString(createTime)
        parcel.writeString(path)
        parcel.writeString(mp3Path)
        parcel.writeString(pcmPath)
        parcel.writeByte(if (like) 1 else 0)
//        parcel.writeByte(if (selected) 1 else 0)
//        parcel.writeByte(if (isPlaying) 1 else 0)
//        parcel.writeInt(sumSize)
//        parcel.writeString(typeName)
//        parcel.writeInt(itemNum)
//        parcel.writeByte(if (open) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Voice> {

        const val MERGE_VOICE = 1
        const val NORMAL_VOICE = 2

        private const val codeLength = 5

        fun convertToVoiceBean(file: File): Voice {
            val voiceBean = Voice()
            voiceBean.time = file.lastModified()
            voiceBean.createTime =
                Utils.getVoiceTime(file.lastModified())
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

        override fun createFromParcel(parcel: Parcel): Voice {
            return Voice(parcel)
        }

        override fun newArray(size: Int): Array<Voice?> {
            return arrayOfNulls(size)
        }
    }


}
