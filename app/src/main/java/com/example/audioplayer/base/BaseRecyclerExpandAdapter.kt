package com.example.audioplayer.base

import android.os.Build
import android.view.View
import android.view.ViewGroup

abstract class BaseRecyclerExpandAdapter<T>(override val datas: MutableList<T>) :
    BaseRecyclerViewAdapter<T>(datas), BaseMenuHolder.OnMenuChang<T> {

    /**
     * 保存隐藏的菜单对应的数据
     */
    private val contentData: MutableMap<String, MutableList<T>> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val mHolder = getViewHolder(parent, getLayoutId(viewType), viewType)
        mHolder.itemView.setOnClickListener(mHolder)
        if (mHolder is BaseMenuHolder) {
            mHolder.setOnMenuChangListener(this)
        }
        mHolder.setOnViewClickListener(object : BaseViewHolder.OnViewClickListener {
            override fun onViewClick(view: View?, position: Int) {
                onClickListener?.onItemClick(view!!, viewType, datas[position], position)
            }
        })
        return mHolder
    }

    final override fun getItemViewType(position: Int): Int =
        when (isMenu(position)) {
            true -> getMenuViewType(position)
            false -> getCustomItemViewType(position)
        }

    /**
     * 菜单对应的menuViewTye
     */
    abstract fun getMenuViewType(position: Int): Int

    /**
     * 内容对应的viewType
     */
    abstract fun getCustomItemViewType(position: Int): Int

    private fun getListByType(type: String): MutableList<T> {
        if (contentData[type].isNullOrEmpty()) {
            contentData[type] = mutableListOf()
        }
        return contentData[type]!!
    }

    /**
     * 当前position是否是menu
     */
    abstract fun isMenu(position: Int): Boolean

    /**
     * 每个菜单项 对应的type不允许重复 并且同意菜单每次的调用结果必须相同
     */
    abstract fun getMenuContent(data: T): String

    /**
     * 传入的item属于当前操作的menu(写入自己的判断逻辑)
     * 判断条件时记得将菜单项排除在外
     */
    abstract fun inCurrentMenu(item: T, menu: T): Boolean

    /**
     * 将数据收集起来
     */
    override fun collectMenuData(position: Int, data: T) {
        var removed = false
        if (Build.VERSION.SDK_INT >= 24) {
            removed = datas.removeIf { t ->
                if (inCurrentMenu(t, data)) {
                    getListByType(getMenuContent(data)).add(t)
                }
                inCurrentMenu(t, data)
            }
        }
        if (removed) {
            notifyItemRangeRemoved(position + 1, getListByType(getMenuContent(data)).size)
        }
    }

    /**
     * 显示数据
     */
    override fun showMenuData(position: Int, data: T) {
        val list = getListByType(getMenuContent(data))
        if (!list.isNullOrEmpty()) {
            datas.addAll(position + 1, list)
            notifyItemRangeInserted(position + 1, getListByType(getMenuContent(data)).size)
            getListByType(getMenuContent(data)).clear()
        }
    }

    fun releaseAllMenuData() {
        var index = 0
        while (index != datas.size && index < datas.size) {
            if (isMenu(index)) {
                val list = getListByType(getMenuContent(datas[index]))
                if (!list.isNullOrEmpty()) {
                    datas.addAll(index + 1, list)
                    index += list.size
                    list.clear()
                }else{
                    index ++
                }
            } else {
                index++
            }
        }
    }
}