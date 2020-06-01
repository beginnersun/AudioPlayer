package com.example.audioplayer.base

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import androidx.core.util.contains
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(itemView: View): RecyclerView.ViewHolder(itemView),View.OnClickListener {
    private val sparseViews:SparseArray<View> = SparseArray<View>()
    private var onViewClickListener:OnViewClickListener? = null

    abstract fun setData(bean:T)

    abstract fun onRelease()

    fun <T:View> getView(@IdRes id:Int,enableClick:Boolean = false) :T{
        if (!sparseViews.contains(id)){
            val view = itemView.findViewById<View>(id)
            if (enableClick){
                view.setOnClickListener(this)
            }
            sparseViews.put(id,view)
        }
        return sparseViews[id] as T
    }

    override fun onClick(v: View?) {
        onViewClickListener?.let {
            it.onViewClick(v,this.adapterPosition)
        }
    }

    fun setOnViewClickListener(onViewClickListener:OnViewClickListener){
        this.onViewClickListener = onViewClickListener
    }

    /**
     * item 点击事件
     */
    interface OnViewClickListener {
        /**
         * item 被点击
         *
         * @param view     被点击的 [View]
         * @param position 在 RecyclerView 中的位置
         */
        fun onViewClick(view: View?, position: Int)
    }

}