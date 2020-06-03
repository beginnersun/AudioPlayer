package com.example.audioplayer.scanner

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.example.audioplayer.R

class PopupListWindow<T>(private val context:Context, private val beans:MutableList<T>, private val mWidth:Int = ViewGroup.LayoutParams.WRAP_CONTENT,
                         private val mHeight:Int = ViewGroup.LayoutParams.WRAP_CONTENT):PopupWindow(mWidth,mHeight),AdapterView.OnItemClickListener,PopupWindow.OnDismissListener {

    private var adapter:ListAdapter? = null
    private var onItemClickListener:OnItemClickListener<T>? = null
    private var onDismissListener:OnDismissListener? = null
    var alpha = 0.2f
    var noBackgroundGray = true

    fun setOnDismissListener(onDismissListener:OnDismissListener){
        this.onDismissListener = onDismissListener
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>){
        this.onItemClickListener = onItemClickListener
    }

    fun setAdapter(adapter: ListAdapter){
        this.adapter = adapter
        this.listView.adapter = adapter
    }

    private val listView:ListView

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.popup_list,null)
        listView = contentView.findViewById(R.id.listView)
        listView.onItemClickListener = this
//        animationStyle = R.style.PopupWindowAnimationStyle
        setBackgroundDrawable(context.resources.getDrawable(android.R.color.transparent))
        isOutsideTouchable = true
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onItemClickListener?.onItemClick(view!!,position,beans[position])
    }

    override fun showAsDropDown(anchor: View?) {
        grayBackground(this.alpha)
        super.showAsDropDown(anchor)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        grayBackground(this.alpha)
        super.showAsDropDown(anchor, xoff, yoff)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        grayBackground(this.alpha)
        super.showAsDropDown(anchor, xoff, yoff, gravity)
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        grayBackground(this.alpha)
        super.showAtLocation(parent, gravity, x, y)
    }

    private fun grayBackground(alpha:Float){
        if (context is Activity && !noBackgroundGray){
            val layoutParams = context.window.attributes
            layoutParams.alpha = alpha
            context.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            context.window.attributes = layoutParams
        }
    }

    override fun onDismiss() {
        grayBackground(1f)
        this.onDismissListener?.onDismiss()
    }

    interface OnItemClickListener<T>{
        fun onItemClick(view:View,position:Int,data:T)
    }

    interface OnDismissListener{
        fun onDismiss()
    }
}