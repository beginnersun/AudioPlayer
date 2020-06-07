package com.example.audioplayer.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.example.audioplayer.R
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.base.BaseViewHolder
import com.example.audioplayer.bean.ShareBean

class ShareAdapter(val context: Context, private val shareList:MutableList<ShareBean>):BaseRecyclerViewAdapter<ShareBean>(shareList) {
    override fun getViewHolder(
        parent: ViewGroup,
        layoutId: Int,
        viewType: Int
    ): BaseViewHolder<ShareBean> {
        Log.e("创建ViewHolder","")
        return ShareHolder(LayoutInflater.from(context).inflate(layoutId,parent,false))
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.item_share

    class ShareHolder(itemView: View):BaseViewHolder<ShareBean>(itemView){
        override fun setData(bean: ShareBean) {
            Log.e("设置数据","${bean}")
            getView<AppCompatImageView>(R.id.iv_share_icon).setImageDrawable(bean.drawable)
            getView<AppCompatTextView>(R.id.tv_share_content).text = bean.sharePackageName
        }

        override fun onRelease() {

        }

    }

}