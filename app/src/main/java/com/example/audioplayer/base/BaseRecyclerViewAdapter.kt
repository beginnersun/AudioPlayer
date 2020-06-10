package com.example.audioplayer.base

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView


abstract class BaseRecyclerViewAdapter<T>(protected open val datas: MutableList<T>) :
    RecyclerView.Adapter<BaseViewHolder<T>>() {

    protected var onClickListener: OnRecyclerViewItemClickListener<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T>{
        val mHolder = getViewHolder(parent, getLayoutId(viewType),viewType)
        onClickListener?.let {
            mHolder.setOnViewClickListener(object :BaseViewHolder.OnViewClickListener{
                override fun onViewClick(view: View?, position: Int) {
                    it.onItemClick(view!!,viewType,datas[position],position)
                }
            })
        }
        return mHolder
    }

    override fun getItemCount(): Int = datas.size

    open fun clearData(){
        this.datas.clear()
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        Log.e("更新导致重新Bangding","")
        holder.setData(datas[position])

    }

    abstract fun getViewHolder(
        parent: ViewGroup,
        @LayoutRes layoutId: Int,
        viewType: Int
    ): BaseViewHolder<T>

    @LayoutRes
    abstract fun getLayoutId(viewType: Int): Int

    fun setOnItemClickListener(onItemClickListener: OnRecyclerViewItemClickListener<T>) {
        this.onClickListener = onItemClickListener
    }

    /**
     * item 点击事件
     *
     * @param <T>
    </T> */
    interface OnRecyclerViewItemClickListener<T> {
        /**
         * item 被点击
         *
         * @param view     被点击的 [View]
         * @param viewType 布局类型
         * @param data     数据
         * @param position 在 RecyclerView 中的位置
         */
        fun onItemClick(view: View, viewType: Int, data: T, position: Int)
    }

    open class Menu(var type:String,var size:Int)
}