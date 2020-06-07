package com.example.audioplayer.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.*
import com.example.audioplayer.adapter.MyListAdapter
import com.example.audioplayer.adapter.VoiceExpandAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.bean.ShareBean
import com.example.audioplayer.scanner.DiscoverAndConvertCallback
import com.example.audioplayer.scanner.WeChatScanner.Companion.defaultSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.fiveMonthSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.threeMonthSpaceTime
import com.example.audioplayer.scanner.WeChatScannerImpl
import com.example.audioplayer.sqlite.Voice
import com.umeng.socialize.ShareAction
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.fragment_voice.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.collections.mutableListOf
import kotlin.collections.removeAll
import kotlin.collections.sortBy
import kotlin.collections.sortWith
import kotlin.collections.sorted
import kotlin.collections.toMutableList


class VoiceFragment : Fragment(), UMShareListener, PopupListWindow.OnItemClickListener<String>,
    OnContentDialog.OnDialogClickListener {

    private val voiceList = mutableListOf<Voice>()
    private lateinit var voiceAdapter: VoiceExpandAdapter
    private var playingPosition = -1
    private var uMShare = ShareAction(activity)
    private var popupListWindow: PopupListWindow<String>? = null
    private var timePopupListWindow: PopupListWindow<String>? = null
    private val groupList = mutableListOf<String>("用户名", "时间")
    private lateinit var timeList: MutableList<String>
    private var groupTag = "用户名"
    private var timeTag = "一个月"
    private val weChatScannerImpl = WeChatScannerImpl()
    private var editDialog: OnContentDialog = OnContentDialog.newInstance()
    private val scanDialog:ScanDialog = ScanDialog.newInstance()
    private var isDegreeScan = false
    private var isShowScanning = false
    private var sharePopupWindow:SharePopupWindow?= null

    private val voice: String = "Voice"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_voice, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = getShareList()
        sharePopupWindow = SharePopupWindow(context!!,list,ViewGroup.LayoutParams.MATCH_PARENT,
            dp2px(context!!,300f))
        sharePopupWindow?.showAtLocation(rootView,Gravity.BOTTOM,0,0)

        timeList = mutableListOf<String>(
            getString(R.string.one_month),
            getString(R.string.three_month),
            getString(R.string.five_month)
        )

        voiceAdapter = VoiceExpandAdapter(activity!!, voiceList)
        scannerCallback.registerLifecycle(this)
        discoverAmr()

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

        PlayUtils.instance.setOnPlayChangedListener(object : PlayUtils.OnPlayChangedListener {
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

        voiceAdapter.setOnItemClickListener(object :
            BaseRecyclerViewAdapter.OnRecyclerViewItemClickListener<Voice> {
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
                        VoiceApplication.instance().getAppDataBase().voiceDao()?.update(data)
                    }
                    R.id.iv_menu_select -> {  //默认选择这个都会展开
                        selectTag(data)
                        if (checkHaveSelected()) {
                            option_group.visibility = View.VISIBLE
                        } else {
                            option_group.visibility = View.GONE
                        }
                    }
                    R.id.iv_select -> {
                        if (checkHaveSelected()) {
                            option_group.visibility = View.VISIBLE
                        } else {
                            option_group.visibility = View.GONE
                        }
                    }
                    R.id.iv_share -> {
                        shareMusic(data.mp3Path)
                    }
                    R.id.tv_name -> {
                        editDialog.initValue = data.targetName
                        fragmentManager?.let { editDialog.show(it, voice) }
                    }
                }
            }
        })

        voice_recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voiceAdapter
        }

        popupListWindow = PopupListWindow(activity!!, groupList, dp2px(context!!, 90f))

        timePopupListWindow = PopupListWindow(activity!!, timeList, dp2px(context!!, 90f))

        popupListWindow!!.setOnDismissListener(object : PopupListWindow.OnDismissListener {
            override fun onDismiss() {
                lifecycleScope.launch {
                    rotate180(250, iv_group)
                    delay(50)
                    iv_group.isClickable = true
                    tv_group.isClickable = true
                }
            }
        })

        timePopupListWindow!!.setOnDismissListener {
            lifecycleScope.launch {
                rotate180(250, iv_time_type)
                delay(50)
                iv_time_type.isClickable = true
                tv_time_type.isClickable = true
            }
        }

        popupListWindow?.setOnItemClickListener(this)
        popupListWindow?.setAdapter(MyListAdapter(context!!, groupList))

        iv_group.setOnClickListener {
            rotate180(250, it)
            popupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
        tv_group.setOnClickListener {
            rotate180(250, iv_group)
            popupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
        iv_time_type.setOnClickListener {
            rotate180(250, it)
            timePopupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
        tv_time_type.setOnClickListener {
            rotate180(250, iv_time_type)
            timePopupListWindow?.showAsDropDown(it)
            it.isClickable = false
        }
        tv_merge.setOnClickListener {
            mergeVoice()
        }
        tv_delete.setOnClickListener {
            editDialog.run {
                this.initValue = ""
                this.showNotification = getString(R.string.delete_info)
                fragmentManager?.let { it1 -> show(it1, voice) }
            }
        }
        editDialog.onDialogClickListener = this

        switch_degree.setOnCheckedChangeListener { _, isChecked ->
            isDegreeScan = isChecked
            when (isDegreeScan) {
                true -> {
                    editDialog.showNotification = getString(R.string.open_degree)
                    fragmentManager?.let { editDialog.show(it, voice) }
                }
                false -> {
                    tv_time_type.visibility = View.GONE
                    tv_time_type.visibility = View.GONE
                }
            }
        }
    }

    private fun getShareList():MutableList<ShareBean>{
        val packageManager = context!!.packageManager
        val intent = Intent(Intent.ACTION_SEND)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.type = "audio/*"
        val results =  packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY)
        val shareBeanList: MutableList<ShareBean> = mutableListOf()
        if (!results.isNullOrEmpty()){
            for (result in results){
                val shareBean = ShareBean(result.loadLabel(packageManager).toString(),
                    result.activityInfo.packageName,result.loadIcon(packageManager))
                shareBeanList.add(shareBean)
            }
        }
        return shareBeanList
    }

    private fun selectTag(voice: Voice) {
        for (index in voiceList.indices) {
            val bean = voiceList[index]
            if (bean.typeName == voice.typeName && bean.itemNum == 0) {
                bean.selected = voice.selected
                voiceAdapter.notifyItemChanged(index)
            }
        }
    }

    private fun mergeVoice() {
        Toast.makeText(context, "开始合成", Toast.LENGTH_SHORT).show()
        val pcmPaths: MutableList<String> =
            voiceList.filter { it.selected }.map { it.pcmPath }.toMutableList()
        val job = lifecycleScope.launch(Dispatchers.Main) {
            val mp3Path = getExternalPath(AUDIO_MP3_TYPE)
            val success = mergePcmToMp3(pcmPaths, mp3Path)
            if (success) {
                startActivity(
                    Intent(activity!!, MergeActivity::class.java).putExtra(
                        "path",
                        mp3Path
                    )
                )
            } else {
//                Toast.makeText(this@MainActivity, "失败", Toast.LENGTH_LONG).show()
            }
            cancel()
        }
    }

    private fun checkHaveSelected(): Boolean {
        voiceList.forEach {
            if (it.selected) {
                return it.selected
            }
        }
        return false
    }

    private fun dealVoicesByName() {
        removeTag()
        if (Build.VERSION.SDK_INT >= 24) {
            voiceList.sortWith(Comparator.comparing(Voice::createTime))
            voiceList.sortWith(Comparator.comparing(Voice::targetName))
        } else {
            voiceList.sorted()
            voiceList.sortBy { voice: Voice -> voice.targetName }
        }
        var sum = 0
        var nums = 0
        var oldTag = ""
        var preMenuIndex = -1
        while (nums != voiceList.size) {
            if (oldTag != voiceList[nums].targetName) {
                oldTag = voiceList[nums].targetName
                if (preMenuIndex != -1) {  //给上一个计数
                    voiceList[preMenuIndex].itemNum = sum
                }
                preMenuIndex = nums
                voiceList.add(nums, Voice.createMenuBean(oldTag, sum))
                sum = 0
            } else {
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

    private fun dealVoicesByTime() {
        removeTag()
        if (Build.VERSION.SDK_INT >= 24) {
            voiceList.sortWith(Comparator.comparing(Voice::createTime))
        } else {
            voiceList.sorted()
        }
        var sum = 0
        var nums = 0
        var oldTag = ""
        var preMenuIndex = -1
        while (nums != voiceList.size) {
            if (oldTag != voiceList[nums].createTime) {
                oldTag = voiceList[nums].createTime
                if (preMenuIndex != -1) {  //给上一个计数
                    voiceList[preMenuIndex].itemNum = sum
                }
                preMenuIndex = nums
                voiceList.add(nums, Voice.createMenuBean(oldTag, sum))
                sum = 0
            } else {
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

    fun shareMusic(path: String) {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            FileProvider.getUriForFile(context!!,
                BuildConfig.APPLICATION_ID+".fileprovider", File(path)
            )
        }else{
            Uri.parse(path)
        }
        val intent = Intent(Intent.ACTION_SEND,null)
        intent.type = "*/*"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Log.e("分享文件位置","$path")
        intent.putExtra(Intent.EXTRA_STREAM,uri)
        startActivity(Intent.createChooser(intent,"分享"))
    }

    private fun playVoice(position: Int) {
        if (playingPosition != -1) {
            voiceList[playingPosition].isPlaying = false
            voiceAdapter.notifyItemChanged(playingPosition)
        }
        playingPosition = position
        Log.e("播放路径", voiceList[playingPosition].mp3Path)
        PlayUtils.instance.play(voiceList[playingPosition].mp3Path)
    }

    private fun stopPlay() {
        PlayUtils.instance.stop()
    }

    private val scannerCallback = object : DiscoverAndConvertCallback() {
        override fun onReceived(voice: Voice) {
            voice.targetName = context!!.defaultSharedPreferences.getString(
                voice.targetUser,
                voice.targetUser
            ) //如果没有则取targetUser也就是code
            if (scanDialog.isAdded){
            }
            voiceList.add(voice)
        }

        override fun onError(error: String) {
        }

        override fun onFinished(num: Int) {
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()
            if (num != 0) {
                voiceAdapter.releaseAllMenuData()
                when (groupTag) {
                    "时间" -> dealVoicesByTime()
                    "用户名" -> dealVoicesByName()
                }
            }
        }
    }

    private fun discoverAmr() { //开始扫描 判断是否是深度扫描 如果是开启弹窗
        if (isDegreeScan) {
            fragmentManager?.let { scanDialog.show(it, "扫描") }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            weChatScannerImpl.discoverUsersVoice(scannerCallback)
        }
    }

    private fun loadMore() {
        lifecycleScope.launch(Dispatchers.IO) {
            weChatScannerImpl.count++
            weChatScannerImpl.discoverUsersVoice(scannerCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        popupListWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        timePopupListWindow?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
        sharePopupWindow?.let {
            if (it.isShowing){
                it.dismiss()
            }
        }
        if (editDialog.isAdded) {
            editDialog.dismiss()
        }
        popupListWindow = null
        timePopupListWindow = null
        sharePopupWindow = null
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
        p1?.printStackTrace()
    }

    override fun onStart(p0: SHARE_MEDIA?) {
        Toast.makeText(context, "开始分享", Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(view: View, position: Int, data: String) {
        when (data) {
            "用户名" -> {
                if (groupTag != data) {
                    groupTag = data
                    dealVoicesByName()
                }
            }
            "时间" -> {
                if (groupTag != data) {
                    groupTag = data
                    dealVoicesByTime()
                }
            }
            "一个月" -> {
                if (timeTag != data) {
                    timeTag = data
                    weChatScannerImpl.spaceTime = defaultSpaceTime
                    refresh()
                }
            }
            "三个月" -> {
                if (timeTag != data) {
                    timeTag = data
                    weChatScannerImpl.spaceTime = threeMonthSpaceTime
                    refresh()
                }
            }
            "五个月" -> {
                if (timeTag != data) {
                    timeTag = data
                    weChatScannerImpl.spaceTime = fiveMonthSpaceTime
                    refresh()
                }
            }
        }
    }

    private fun removeTag() {
        voiceList.removeAll { it.itemNum != 0 }
    }

    private fun refresh() {
        voiceList.clear()
        discoverAmr()
    }

    override fun onOkClick(view: View, message: String) {
        when (message) {
            getString(R.string.delete_info) -> {
            }
            getString(R.string.open_degree) -> {
                iv_time_type.visibility = View.VISIBLE
                tv_time_type.visibility = View.VISIBLE
            }
            else -> {
                context?.defaultSharedPreferences?.edit {
                    putString(editDialog.initValue, message)
                }
            }
        }
    }

    override fun onCancel(view: View) {
    }

}