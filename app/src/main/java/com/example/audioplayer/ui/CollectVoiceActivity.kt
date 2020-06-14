package com.example.audioplayer.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.BuildConfig
import com.example.audioplayer.R
import com.example.audioplayer.VoiceApplication
import com.example.audioplayer.adapter.VoiceAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.dialog.OnContentDialog
import com.example.audioplayer.sqlite.Voice
import com.example.audioplayer.util.PlayUtils
import com.example.audioplayer.util.VoiceUtil.checkHaveSelected
import com.example.audioplayer.util.VoiceUtil.shareMusic
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_collect_voice.*
import kotlinx.android.synthetic.main.fragment_voice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CollectVoiceActivity : AppCompatActivity() ,BaseRecyclerViewAdapter.OnRecyclerViewItemClickListener<Voice>{

    private val voiceList: MutableList<Voice> = mutableListOf()
    private val voiceMergeList: MutableList<Voice> = mutableListOf()
    private var currentPosition = 0
    private var editDialog: OnContentDialog = OnContentDialog.newInstance()
    private var playingPosition = -1
    private val voiceAdapter: VoiceAdapter = VoiceAdapter(voiceList).apply {
        showSelected(false)
    }
    private val voiceMergeAdapter: VoiceAdapter = VoiceAdapter(voiceMergeList).apply {
        showSelected(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_voice)

        collect_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CollectVoiceActivity)
            adapter = voiceAdapter
        }
        collect_merge_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CollectVoiceActivity)
            adapter = voiceMergeAdapter
        }
        initListener()
        discoverCollectVoice()
    }

    private fun initListener(){
        voiceAdapter.setOnItemClickListener(this)
        voiceMergeAdapter.setOnItemClickListener(this)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                releaseMediaPlayer()
                when (currentPosition) {
                    1 -> {
                        collect_merge_recyclerView.visibility = View.GONE
                        collect_recyclerView.visibility = View.VISIBLE
                        currentPosition = 0
                    }
                    else -> {
                        collect_merge_recyclerView.visibility = View.VISIBLE
                        collect_recyclerView.visibility = View.GONE
                        currentPosition = 1
                    }
                }
            }

        })
    }

    private fun releaseMediaPlayer(){
        changeOldPlayStation()  //修改之前adapter的状态
        playingPosition = -1
        PlayUtils.instance.stop()
    }

    /**
     * 修改上一个状态
     */
    private fun changeOldPlayStation(){
        if (playingPosition != -1){
            when(currentPosition){
                0 -> {
                    voiceList[playingPosition].isPlaying = false
                    voiceAdapter.notifyItemChanged(playingPosition)
                }
                else -> {
                    voiceMergeList[playingPosition].isPlaying = false
                    voiceMergeAdapter.notifyItemChanged(playingPosition)
                }
            }
        }
    }

    /**
     * 播放语音
     */
    private fun playVoice(position: Int) {
        changeOldPlayStation()
        playingPosition = position
        val voice = when(currentPosition){
            0 -> voiceList[playingPosition]
            else -> voiceMergeList[playingPosition]
        }
        Log.e("播放路径", voice.mp3Path)
        PlayUtils.instance.play(voice.mp3Path)
    }

    private fun stopPlay() {
        PlayUtils.instance.stop()
    }

    /**
     * 扫描收藏语音
     */
    private fun discoverCollectVoice() {
        lifecycleScope.launch(Dispatchers.IO) {
            val collectVoices =
                VoiceApplication.instance().getAppDataBase().voiceDao()?.findCollectVoice(true)
            if (!collectVoices.isNullOrEmpty()) {
                Log.e("收藏语音结果","${collectVoices.size}")
                collectVoices.forEach {
                    Log.e("收藏的结果","$it")
                }
                voiceList.addAll(collectVoices!!.filter { it.merge != Voice.MERGE_VOICE })
                voiceMergeList.addAll(collectVoices!!.filter { it.merge == Voice.MERGE_VOICE })
                withContext(Dispatchers.Main) {
                    voiceAdapter.notifyDataSetChanged()
                    voiceMergeAdapter.notifyDataSetChanged()
                }
            }else{
                Log.e("收藏语音结果为0","好处")
            }
            cancel()
        }
    }

    private fun updateVoice(voice:Voice,position: Int){
        lifecycleScope.launch(Dispatchers.IO){
            VoiceApplication.instance().getAppDataBase().voiceDao()?.update(voice)
            withContext(Dispatchers.Main){
                if (currentPosition == 0){
                    voiceAdapter.notifyItemChanged(position)
                }else{
                    voiceMergeAdapter.notifyItemChanged(position)
                }
            }
            cancel()
        }
    }

    override fun onItemClick(view: View, viewType: Int, data: Voice, position: Int) {
        when (view.id) {
            R.id.fl_play_voice -> {
                val isPlay = view.tag as Boolean
                if (isPlay) {
                    playVoice(position)
                } else {
                    playingPosition = -1
                    stopPlay()
                }
            }
            R.id.iv_like -> {
                updateVoice(data,position)
            }
            R.id.iv_share -> {
                shareMusic(this,data.mp3Path)
            }
            R.id.tv_name -> { //需要做区分  分开merge与target
                editDialog.initValue = data.targetName
                supportFragmentManager?.let { editDialog.show(it, "collect") }
            }
        }
    }

}