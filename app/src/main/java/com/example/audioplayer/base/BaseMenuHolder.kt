package com.example.audioplayer.base

import android.view.View

abstract class BaseMenuHolder<T>(itemView: View): BaseViewHolder<T>(itemView){

    protected var onMenuChang:OnMenuChang<T>? = null

    fun setOnMenuChangListener(onMenuChang: OnMenuChang<T>){
        this.onMenuChang = onMenuChang
    }

    interface OnMenuChang<T>{
        fun collectMenuData(position: Int, data: T)

        fun showMenuData(position: Int, data: T)
    }

}