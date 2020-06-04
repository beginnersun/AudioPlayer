package com.example.audioplayer.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.*
import com.example.audioplayer.adapter.MyListAdapter
import com.example.audioplayer.adapter.VoiceExpandAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.scanner.DiscoverAndConvertCallback
import com.example.audioplayer.scanner.WeChatScanner.Companion.defaultSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.fiveMonthSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.threeMonthSpaceTime
import com.example.audioplayer.scanner.WeChatScannerImpl
import com.example.audioplayer.sqlite.Voice
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMusic
import kotlinx.android.synthetic.main.fragment_voice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.util.Comparator

class VoiceFragment : Fragment() , UMShareListener,PopupListWindow.OnItemClickListener<String>{

    private val voiceList = mutableListOf<Voice>()
    private lateinit var voiceAdapter:VoiceExpandAdapter
    private var playingPosition = -1
    private val uMShare = ShareAction(activity)
    private var popupListWindow:PopupListWindow<String>? = null
    private var timePopupListWindow:PopupListWindow<String>? = null
    private val groupList = mutableListOf<String>("按用户","按时间")
    private lateinit var timeList:MutableList<String>
    private var groupTag = "按用户"
    private var timeTag = "一个月"
    private val weChatScannerImpl = WeChatScannerImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_voice, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeList = mutableListOf<String>(getString(R.string.one_month),getString(R.string.three_month),getString(R.string.five_month))

        voiceAdapter = VoiceExpandAdapter(activity!!, voiceList)
        scannerCallback.registerLifecycle(this)
        discoverAmr()

        uMShare.setCallback(this)

        refreshLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setOnRefreshListener {
                refresh()
            }
            setOnLoadMoreListener {
                loadMore()
            }
        }

        PlayUtils.instance.setOnPlayChangedListener(object : PlayUtils.OnPlayChangedListener{
            override fun onPrepared() {
            }

            override fun onCompleted() {
                voiceList[playingPosition].isPlaying = false
                playingPosition = -1
            }

            override fun onError() {
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
                    R.id.tv_name -> {

                        context!!.defaultSharedPreferences.edit {
                            putString(data.targetName,"输入框")
                        }
                    }
                }
            }
        })

        voice_recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voiceAdapter
        }

        popupListWindow = PopupListWindow(activity!!,groupList, dp2px(context!!,90f))

        timePopupListWindow = PopupListWindow(activity!!,timeList, dp2px(context!!,90f))

        popupListWindow!!.setOnDismissListener(object : PopupListWindow.OnDismissListener{
            override fun onDismiss() {
                lifecycleScope.launch {
                    rotate180(250,iv_group)
                    delay(50)
                    iv_group.isClickable = true
                    tv_group.isClickable = true
                }
            }
        })

        timePopupListWindow!!.setOnDismissListener {
            lifecycleScope.launch {
                rotate180(250,iv_time_type)
                delay(50)
                iv_time_type.isClickable = true
                tv_time_type.isClickable = true
            }
        }

        popupListWindow?.setOnItemClickListener(this)
        popupListWindow?.setAdapter(MyListAdapter(context!!,groupList))

        iv_group.setOnClickListener {
            rotate180(250,it)
            popupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
        tv_group.setOnClickListener {
            rotate180(250,iv_group)
            popupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
        iv_time_type.setOnClickListener {
            rotate180(250,it)
            timePopupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
        tv_time_type.setOnClickListener {
            rotate180(250,iv_time_type)
            timePopupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
    }

    private fun dealVoicesByName(){
        removeTag()
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
                voiceList[nums].typeName = oldTag
                sum++
            }
            nums++
        }
        if (preMenuIndex != -1) {
            voiceList[preMenuIndex].itemNum = sum
        }
        voiceAdapter.notifyDataSetChanged()
    }

    private fun dealVoicesByTime(){
        removeTag()
        if(Build.VERSION.SDK_INT >= 24) {
            voiceList.sortWith(Comparator.comparing(Voice::createTime))
        }else{
            voiceList.sorted()
        }
        var sum = 0
        var nums = 0
        var oldTag = ""
        var preMenuIndex = -1
        while (nums != voiceList.size){
            if (oldTag != voiceList[nums].createTime){
                oldTag = voiceList[nums].createTime
                if (preMenuIndex!=-1){  //给上一个计数
                    voiceList[preMenuIndex].itemNum = sum
                }
                preMenuIndex = nums
                voiceList.add(nums, Voice.createMenuBean(oldTag,sum))
                sum = 0
            }else{
                voiceList[nums].typeName = oldTag
                sum++
            }
            nums++
        }
        if (preMenuIndex != -1) {
            voiceList[preMenuIndex].itemNum = sum
        }
        voiceAdapter.notifyDataSetChanged()
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
            voice.targetName = context!!.defaultSharedPreferences.getString(voice.targetUser,voice.targetUser) //如果没有则取targetUser也就是code
            voiceList.add(voice)
        }
        override fun onError(error: String) {
        }
        override fun onFinished(num: Int) {
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()
            if(num != 0){
                voiceAdapter.releaseAllMenuData()
                when(groupTag){
                    "按时间" -> dealVoicesByTime()
                    "按用户" -> dealVoicesByName()
                }
            }
        }
    }

    private fun discoverAmr(){
        lifecycleScope.launch(Dispatchers.IO){
            weChatScannerImpl.discoverUsersVoice(scannerCallback)
        }
    }

    private fun loadMore(){
        lifecycleScope.launch(Dispatchers.IO){
            weChatScannerImpl.count++
            weChatScannerImpl.discoverUsersVoice(scannerCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        popupListWindow?.let {
            if (it.isShowing){
                it.dismiss()
            }
        }
        timePopupListWindow?.let {
            if (it.isShowing){
                it.dismiss()
            }
        }
        popupListWindow = null
        timePopupListWindow = null
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

    override fun onItemClick(view: View, position: Int, data: String) {
        when(data){
            "按用户" -> {
                if(groupTag != data) {
                    groupTag = data
                    dealVoicesByName()
                }
            }
            "按时间" -> {
                if(groupTag != data) {
                    groupTag = data
                    dealVoicesByTime()
                }
            }
            "一个月" -> {
                if (timeTag != data){
                    timeTag = data
                    weChatScannerImpl.spaceTime = defaultSpaceTime
                    refresh()
                }
            }
            "三个月" -> {
                if (timeTag != data){
                    timeTag = data
                    weChatScannerImpl.spaceTime = threeMonthSpaceTime
                    refresh()
                }
            }
            "五个月" -> {
                if (timeTag != data){
                    timeTag = data
                    weChatScannerImpl.spaceTime = fiveMonthSpaceTime
                    refresh()
                }
            }
        }
    }

    private fun removeTag(){
        voiceList.removeAll { it.itemNum != 0 }
    }

    private fun refresh(){
        voiceList.clear()
        discoverAmr()
    }

}