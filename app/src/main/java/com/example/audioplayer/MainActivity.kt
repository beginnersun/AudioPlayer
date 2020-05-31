package com.example.audioplayer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.scanner.DiscoverAndConvertCallback
import com.example.audioplayer.scanner.WeChatScannerImpl
import com.example.audioplayer.sqlite.Voice
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val voiceList = mutableListOf<Voice>()
    private val voiceAdapter = VoiceAdapter(this,voiceList)
    private var playingPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        discoverAmr()
        refreshLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setOnRefreshListener {
                Toast.makeText(this@MainActivity, "正在刷新", Toast.LENGTH_SHORT).show()
                finishRefresh(2*1000)
            }
            setOnLoadMoreListener {
                Toast.makeText(this@MainActivity, "正在加载", Toast.LENGTH_SHORT).show()
                finishLoadMore(2*1000)
            }
        }

        PlayUtils.instance.setOnPlayChangedListener(object : PlayUtils.OnPlayChangedListener{
            override fun onPrepared() {
                Toast.makeText(this@MainActivity, "准备完成", Toast.LENGTH_SHORT).show()
            }

            override fun onCompleted() {
                Toast.makeText(this@MainActivity, "播放完成", Toast.LENGTH_SHORT).show()
                voiceList[playingPosition].isPlaying = false
                playingPosition = -1
            }

            override fun onError() {
                Toast.makeText(this@MainActivity, "播放失败", Toast.LENGTH_SHORT).show()
                voiceList[playingPosition].isPlaying = false
                playingPosition = -1
            }

        })

        voiceAdapter.setOnItemClickListener(object : BaseRecyclerViewAdapter.OnRecyclerViewItemClickListener<Voice>{
            override fun onItemClick(view: View, viewType: Int, data: Voice, position: Int) {
                when(view.id){
                    R.id.fl_play_voice -> {
                        val isPlay = view.tag as Boolean
                        if (isPlay) {
                            playVoice(position)
                        }else{
                            playingPosition = -1
                            stopPlay()
                        }
                    }
                    R.id.iv_like -> {

                    }
                    R.id.iv_select -> {

                    }
                    R.id.iv_share -> {

                    }
                }
            }
        })

        voice_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = voiceAdapter
        }

    }

    private fun playVoice(position : Int){
        if (playingPosition != -1){
            voiceList[playingPosition].isPlaying = false
            voiceAdapter.notifyItemChanged(playingPosition)
        }
        playingPosition = position
        Log.e("播放路径",voiceList[playingPosition].mp3Path)
        PlayUtils.instance.play(voiceList[playingPosition].mp3Path)
    }

    private fun stopPlay(){
        PlayUtils.instance.stop()
    }

    private val scannerCallback = object : DiscoverAndConvertCallback(){
        override fun onReceived(voice: Voice) {
            voiceList.add(voice)
        }


        override fun onError(error: String) {

        }

        override fun onFinished(num: Int) {
            if(num != 0){
                voiceList.sorted()
                voiceAdapter.notifyDataSetChanged()
            }
        }
    }

    val sorcc = fun (voice: Voice,voice1: Voice):Int{
        return 1
    }

    fun ccdd(voice: Voice,voice1: Voice):Int{
        return 1
    }

    private fun discoverAmr(){
        lifecycleScope.launch(Dispatchers.IO){
            WeChatScannerImpl().apply {
                discoverUsersVoice(this@MainActivity,scannerCallback)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayUtils.instance.onDestroy()
    }

}
