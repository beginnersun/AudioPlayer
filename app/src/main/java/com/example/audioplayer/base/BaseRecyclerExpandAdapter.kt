package com.example.audioplayer.base

import android.os.Build
import android.view.View
import android.view.ViewGroup

abstract class BaseRecyclerExpandAdapter<T>(override val datas: MutableList<T>) :
    BaseRecyclerViewAdapter<T>(datas) {

    /**
     * 菜单状态  false 表示打开
     *          true  表示关闭
     */
    private val menuState = mutableMapOf<Int, Boolean>()

    /**
     * 保存隐藏的菜单对应的数据
     */
    private val contentData: MutableMap<String, MutableList<T>> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val mHolder = getViewHolder(parent, getLayoutId(viewType), viewType)
        mHolder.setOnViewClickListener(object : BaseViewHolder.OnViewClickListener {
            override fun onViewClick(view: View?, position: Int) {
                if (isMenu(position)) {  //是一级菜单
                    menuState[position] =
                        !(menuState[position] != null && menuState[position]!!)
                    if (!menuState[position]!!) {
                        showMenuData(position, datas[position])
                    } else {
                        collectMenuData(position, datas[position])
                    }
                }
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
     */
    abstract fun inCurrentMenu(item: T, menu: T): Boolean

    /**
     * 将数据收集起来
     */
    private fun collectMenuData(position: Int, data: T) {
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
    private fun showMenuData(position: Int, data: T) {
        datas.addAll(position + 1, getListByType(getMenuContent(data)))
        notifyItemRangeInserted(position + 1, getListByType(getMenuContent(data)).size)
        getListByType(getMenuContent(data)).clear()
    }

}