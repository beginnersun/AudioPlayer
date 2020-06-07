package com.example.audioplayer

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.audioplayer.adapter.ShareAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.bean.ShareBean

class SharePopupWindow(
    private val context: Context, private val shareList: MutableList<ShareBean>,
    private val mWidth:Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    private val mHeight:Int = ViewGroup.LayoutParams.WRAP_CONTENT
) : PopupWindow(mWidth,mHeight),
    BaseRecyclerViewAdapter.OnRecyclerViewItemClickListener<ShareBean>,PopupWindow.OnDismissListener {

    enum class Share(val type:String){
        QQ("qq"),WX("wx"),QQZone("qqZone"),WXFriend("wxFriend"),
        DD("dd")
    }

    private val recyclerView: RecyclerView
    private var alpha = 0.2f
    private var adapter: ShareAdapter

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.popup_share, null)
        recyclerView = contentView.findViewById(R.id.share_recyclerView)
        adapter = ShareAdapter(context, shareList)
        Log.e("数据量","${shareList.size}")
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = adapter
        setOnDismissListener(this)
//        animationStyle = R.style.PopupWindowAnimationStyle
        setBackgroundDrawable(context.resources.getDrawable(android.R.color.transparent))
        isOutsideTouchable = true
        isTouchable = true
        adapter.setOnItemClickListener(this)
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

    private fun grayBackground(alpha: Float) {
        if (context is Activity) {
            val layoutParams = context.window.attributes
            layoutParams.alpha = alpha
            context.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            context.window.attributes = layoutParams
        }
    }

    override fun onItemClick(view: View, viewType: Int, data: ShareBean, position: Int) {
        when(data.sharePackageName){
            "微信" -> {

            }
            "QQ" -> {

            }
            "朋友圈" -> {

            }
            "QQ空间" -> {

            }
            "钉钉" -> {

            }
        }
    }

    override fun onDismiss() {
        grayBackground(1f)
//        dismiss()
    }

}