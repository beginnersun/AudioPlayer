package com.example.audioplayer.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.PlayUtils
import com.example.audioplayer.R
import com.example.audioplayer.adapter.VoiceExpandAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.scanner.DiscoverAndConvertCallback
import com.example.audioplayer.scanner.WeChatScannerImpl
import com.example.audioplayer.sqlite.Voice
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMusic
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_voice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Comparator

class VoiceFragment : Fragment() , UMShareListener {

    private val voiceList = mutableListOf<Voice>()
    private lateinit var voiceAdapter:VoiceExpandAdapter
    private var playingPosition = -1
    private val uMShare = ShareAction(activity)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_voice, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voiceAdapter = VoiceExpandAdapter(activity!!, voiceList)

        discoverAmr()

        uMShare.setCallback(this)

        refreshLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setOnRefreshListener {
                voiceList.clear()
                discoverAmr()
                Toast.makeText(context, "正在刷新", Toast.LENGTH_SHORT).show()
                finishRefresh(2*1000)
            }
            setOnLoadMoreListener {
                Toast.makeText(context, "正在加载", Toast.LENGTH_SHORT).show()
                finishLoadMore(2*1000)
            }
        }

        PlayUtils.instance.setOnPlayChangedListener(object : PlayUtils.OnPlayChangedListener{
            override fun onPrepared() {
                Toast.makeText(context, "准备完成", Toast.LENGTH_SHORT).show()
            }

            override fun onCompleted() {
                Toast.makeText(context, "播放完成", Toast.LENGTH_SHORT).show()
                voiceList[playingPosition].isPlaying = false
                playingPosition = -1
            }

            override fun onError() {
                Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show()
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
                        shareMusic(data.mp3Path)
                    }
                }
            }
        })

        voice_recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voiceAdapter
        }
    }

    private fun dealVoicesByName(){
        if(Build.VERSION.SDK_INT >= 24) {
            voiceList.sortWith(Comparator.comparing(Voice::createTime))
            voiceList.sortWith(Comparator.comparing(Voice::targetName))
        }else{
            voiceList.sorted()
            voiceList.sortBy { voice: Voice -> voice.targetName}
        }
        var sum = 0
        var nums = 0
        var oldTag = ""
        var preMenuIndex = -1
        while (nums != voiceList.size){
            if (oldTag != voiceList[nums].targetName){
                oldTag = voiceList[nums].targetName
                if (preMenuIndex!=-1){  //给上一个计数
                    voiceList[preMenuIndex].itemNum = sum
                }
                preMenuIndex = nums
                voiceList.add(nums, Voice.createMenuBean(oldTag,sum))
                sum = 0
            }else{
                voiceList[nums].targetName = oldTag
                sum++
            }
            nums++
        }
        if (preMenuIndex != -1) {
            voiceList[preMenuIndex].itemNum = sum
        }
        voiceList.forEach{
            println("输出   $it")
        }
    }

    fun shareMusic(path:String,title:String = "",description:String ="",toUrl:String = ""){
        val uMusic = UMusic(path)
        if (title.isNotEmpty()){
            uMusic.title = title
        }
        if(description.isNotEmpty()){
            uMusic.description = description
        }
        if (toUrl.isNotEmpty()){
            uMusic.setmTargetUrl(toUrl)
        }
        uMShare.withMedia(uMusic).share()
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
                dealVoicesByName()
                voiceAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun discoverAmr(){
        lifecycleScope.launch(Dispatchers.IO){
            WeChatScannerImpl().apply {
                discoverUsersVoice(this@VoiceFragment,scannerCallback)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayUtils.instance.onDestroy()
    }

    override fun onResult(p0: SHARE_MEDIA?) {
        Toast.makeText(context, "分享成功", Toast.LENGTH_SHORT).show()
    }

    override fun onCancel(p0: SHARE_MEDIA?) {
        Toast.makeText(context, "取消分享", Toast.LENGTH_SHORT).show()
    }

    override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {
        Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show()
    }

    override fun onStart(p0: SHARE_MEDIA?) {
        Toast.makeText(context, "开始分享", Toast.LENGTH_SHORT).show()
    }

}