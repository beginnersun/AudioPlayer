package com.example.audioplayer.adapter

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.example.audioplayer.R
import com.example.audioplayer.base.BaseMenuHolder
import com.example.audioplayer.base.BaseRecyclerExpandAdapter
import com.example.audioplayer.base.BaseViewHolder
import com.example.audioplayer.sqlite.Voice
import java.util.*

const val MENU_VIEW_TYPE = 0
const val CONTENT_VIEW_TYPE = 1
class VoiceExpandAdapter(private val voices: MutableList<Voice>) :
    BaseRecyclerExpandAdapter<Voice>(voices) {
    override fun getMenuViewType(position: Int): Int =
        MENU_VIEW_TYPE

    override fun getCustomItemViewType(position: Int): Int =
        CONTENT_VIEW_TYPE

    override fun isMenu(position: Int): Boolean = voices[position].itemNum != 0

    override fun getMenuContent(data: Voice): String = data.typeName

    override fun inCurrentMenu(item: Voice, menu: Voice): Boolean =
        item.typeName == menu.typeName && item.itemNum == 0

    override fun getViewHolder(parent: ViewGroup, layoutId: Int,viewType: Int): BaseViewHolder<Voice> {
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId,parent,false)
        return when(viewType){
            MENU_VIEW_TYPE -> MenuHolder(itemView)
            CONTENT_VIEW_TYPE -> VoiceHolder(itemView)
            else -> MenuHolder(itemView)
        }
    }

    override fun getLayoutId(viewType: Int): Int =
        when(viewType){
            MENU_VIEW_TYPE -> R.layout.item_menu
            CONTENT_VIEW_TYPE -> R.layout.item_voice
            else -> R.layout.item_menu
        }


    class MenuHolder(itemView: View):BaseMenuHolder<Voice>(itemView){
        override fun setData(bean: Voice) {
            getView<AppCompatImageView>(R.id.iv_menu_select).setOnClickListener {
                bean.open = true
                bean.selected = !bean.selected
                if (bean.selected){
                    (it as AppCompatImageView).setImageResource(R.mipmap.selected)
                }else{
                    (it as AppCompatImageView).setImageResource(R.mipmap.un_select)
                }
                onMenuChang?.showMenuData(adapterPosition,bean)
                onClick(it)
            }
            itemView.setOnClickListener {
                bean.open = !bean.open
                if (bean.open){
                    onMenuChang?.showMenuData(adapterPosition,bean)
                } else{
                    onMenuChang?.collectMenuData(adapterPosition,bean)
                }
            }
            getView<AppCompatTextView>(R.id.tv_menu).text = bean.typeName
            getView<AppCompatTextView>(R.id.tv_num).text = "${bean.itemNum} 条"
        }

        override fun onRelease() {

        }

    }

    class VoiceHolder(itemView: View):BaseViewHolder<Voice>(itemView){

        private val timer: Timer = Timer()
        private var animation: AnimationDrawable? = null
        private var timerTask: TimerTask? = null

        override fun setData(bean: Voice) {
            val tvName = getView<AppCompatTextView>(R.id.tv_name,true)
            val tvVoiceMinutes = getView<AppCompatTextView>(R.id.tv_voice_minutes)
            val tvTime = getView<AppCompatTextView>(R.id.tv_time)
            val ivSelect = getView<AppCompatImageView>(R.id.iv_select)
            val flPlayVoice = getView<FrameLayout>(R.id.fl_play_voice)
            val ivVoice = getView<AppCompatImageView>(R.id.iv_voice)
            val ivShare = getView<AppCompatImageView>(R.id.iv_share,true)
            val ivLike = getView<AppCompatImageView>(R.id.iv_like)
            tvName.text = "${bean.targetName}"
            tvVoiceMinutes.text = "${bean.minutes}"
            tvTime.text = "${bean.createTime}"
            if(bean.selected) {
                ivSelect.setImageResource(R.mipmap.selected)
            }else{
                ivSelect.setImageResource(R.mipmap.un_select)
            }
            if (bean.like){
                ivLike.setImageResource(R.mipmap.liked)
            }else{
                ivLike.setImageResource(R.mipmap.un_like)
            }
            flPlayVoice.setOnClickListener {
                ivVoice.setImageResource(
                    R.drawable.voice_bg
                )
                animation = (ivVoice.drawable as AnimationDrawable)
                bean.isPlaying = !bean.isPlaying
                it.tag = bean.isPlaying
                if (bean.isPlaying) playVoice(bean.minutes) else stopVoice()
                onClick(it)
            }
            ivSelect.setOnClickListener {
                bean.selected = !bean.selected
                if (bean.selected){
                    (it as AppCompatImageView).setImageResource(R.mipmap.selected)
                }else{
                    (it as AppCompatImageView).setImageResource(R.mipmap.un_select)
                }
                onClick(it)
            }
            ivLike.setOnClickListener {
                bean.like = !bean.like
                if (bean.like){
                    (it as AppCompatImageView).setImageResource(R.mipmap.liked)
                }else{
                    (it as AppCompatImageView).setImageResource(R.mipmap.un_like)
                }
                onClick(it)
            }

        }

        private fun playVoice(duration:Int){
            timerTask = object : TimerTask(){
                override fun run() {
                    Log.e("停止动画","停止")
                    animation?.stop()
                    getView<AppCompatImageView>(R.id.iv_voice).setImageResource(
                        R.drawable.voice_bg
                    )
                }
            }
            animation?.start()
            timer.schedule(timerTask,duration*1000.toLong())
        }

        private fun stopVoice(){
            timerTask?.cancel()
            animation?.stop()
        }

        override fun onRelease(){
            animation?.stop()
            timerTask?.cancel()
            timer.cancel()
        }
    }

}