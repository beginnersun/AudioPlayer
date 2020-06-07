package com.example.audioplayer.bean

import android.graphics.drawable.Drawable

data class ShareBean(val sharePackageName:String,val shareId:String,val drawable:Drawable){

    override fun toString(): String {
        return "ShareBean(sharePackageName='$sharePackageName', shareId='$shareId', drawable=$drawable)"
    }
}