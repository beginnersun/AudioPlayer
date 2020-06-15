package com.example.audioplayer.ui.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
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
import com.example.audioplayer.dialog.OnContentDialog
import com.example.audioplayer.dialog.PopupListWindow
import com.example.audioplayer.dialog.ScanDialog
import com.example.audioplayer.scanner.DiscoverAndConvertCallback
import com.example.audioplayer.scanner.WeChatScanner.Companion.defaultSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.fiveMonthSpaceTime
import com.example.audioplayer.scanner.WeChatScanner.Companion.threeMonthSpaceTime
import com.example.audioplayer.scanner.WeChatScannerImpl
import com.example.audioplayer.scanner.strategy.TimeSpaceStrategy
import com.example.audioplayer.sqlite.Voice
import com.example.audioplayer.ui.MainActivity
import com.example.audioplayer.ui.MainActivity.Companion.TAG_MERGE_FRAGMENT
import com.example.audioplayer.ui.TargetUserActivity
import com.example.audioplayer.util.PlayUtils
import com.example.audioplayer.util.VoiceUtil
import com.example.audioplayer.util.rotate180
import com.scwang.smart.refresh.footer.BallPulseFooter
import com.scwang.smart.refresh.header.MaterialHeader
import kotlinx.android.synthetic.main.fragment_voice.*
import kotlinx.coroutines.*
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.mutableListOf
import kotlin.collections.removeAll
import kotlin.collections.sortBy
import kotlin.collections.sortWith
import kotlin.collections.sorted


class VoiceFragment : Fragment(), PopupListWindow.OnItemClickListener<String>,
    OnContentDialog.OnDialogClickListener {

    private val voiceList = mutableListOf<Voice>()
    private lateinit var voiceAdapter: VoiceExpandAdapter
    private var playingPosition = -1
    private var popupListWindow: PopupListWindow<String>? = null
    private var timePopupListWindow: PopupListWindow<String>? = null
    private val groupList = mutableListOf("用户名", "时间")
    private lateinit var timeList: MutableList<String>
    private var groupTag = "用户名"
    private var timeTag = "一个月"
    private val weChatScannerImpl = WeChatScannerImpl(TimeSpaceStrategy())
    private var editDialog: OnContentDialog = OnContentDialog.newInstance()
    private val scanDialog: ScanDialog = ScanDialog.newInstance()
    private var isDegreeScan = false
    private val voice: String = "Voice"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_voice, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeList = mutableListOf<String>(
            getString(R.string.one_month),
            getString(R.string.three_month),
            getString(R.string.five_month)
        )
        voiceAdapter = VoiceExpandAdapter(voiceList)
        scannerCallback.registerLifecycle(this)
        discoverAmr()

        refreshLayout.setRefreshHeader(MaterialHeader(context!!))
        refreshLayout.setRefreshFooter(BallPulseFooter(context!!))
        refreshLayout.apply {
            setEnableRefresh(true)
            setEnableLoadMore(false)
            setOnRefreshListener {
                refresh()
            }
        }
        voice_recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voiceAdapter
        }
        popupListWindow = PopupListWindow(
            activity!!,
            groupList,
            dp2px(context!!, 90f)
        )
        timePopupListWindow = PopupListWindow(
            activity!!,
            timeList,
            dp2px(context!!, 90f)
        )
        popupListWindow?.setAdapter(MyListAdapter(context!!, groupList))
        initListener()
    }

    private fun initListener(){
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
                        collectVoice(data)
                    }
                    R.id.iv_menu_select -> {  //默认选择这个都会展开
                        selectTag(data)
                        if (VoiceUtil.checkHaveSelected(voiceList)) {
                            option_group.visibility = View.VISIBLE
                        } else {
                            option_group.visibility = View.GONE
                        }
                    }
                    R.id.iv_select -> {
                        if (VoiceUtil.checkHaveSelected(voiceList)) {
                            option_group.visibility = View.VISIBLE
                        } else {
                            option_group.visibility = View.GONE
                        }
                    }
                    R.id.iv_share -> {
                        VoiceUtil.shareMusic(context!!,data.mp3Path)
                    }
                    R.id.tv_name -> {
                        editDialog.initValue = data.targetName
                        fragmentManager?.let { editDialog.show(it, voice) }
                    }
                    else -> {
                        Log.e("跳转","携带参数为 ${data.targetUser}")
                        TargetUserActivity.intoActivity(context!!, data.targetUser)
                    }
                }
            }
        })

        editDialog.onDialogClickListener = this

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
            (activity!! as MainActivity).switchFragment(TAG_MERGE_FRAGMENT,voiceList.filter { it.selected } as ArrayList<Voice>)
//            MergeActivity.intoMergeActivity(context!!,
//                )
        }
        tv_delete.setOnClickListener {
            editDialog.run {
                this.initValue = ""
                this.showNotification = getString(R.string.delete_info)
                fragmentManager?.let { it1 -> show(it1, voice) }
            }
        }

        switch_degree.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    tv_time_type.visibility = View.VISIBLE
                    iv_time_type.visibility = View.VISIBLE
                    editDialog.showNotification = getString(R.string.open_degree)
                    fragmentManager?.let { editDialog.show(it, voice) }
                }
                false -> {
                    tv_time_type.visibility = View.GONE
                    iv_time_type.visibility = View.GONE
                }
            }
        }
        popupListWindow!!.setOnDismissListener(object : PopupListWindow.OnDismissListener {
            override fun onDismiss() {
                lifecycleScope.launch {
                    rotate180(250, iv_group)
                    delay(50)
                    iv_group.isClickable = true
                    tv_group.isClickable = true
                    cancel()
                }
            }
        })

        timePopupListWindow!!.setOnDismissListener {
            lifecycleScope.launch {
                rotate180(250, iv_time_type)
                delay(50)
                iv_time_type.isClickable = true
                tv_time_type.isClickable = true
                cancel()
            }
        }
        popupListWindow?.setOnItemClickListener(this)


        DatePickerDialog.newInstance(object :DatePickerDialog.OnSelectedDateListener{
            override fun onSelectedDate(
                view: DatePicker,
                year: Int,
                monthOfYear: Int,
                dayOfMonth: Int
            ) {

            }

        })

        tv_date_pick.setOnClickListener {
        }
    }

//    private fun getShareList(): MutableList<ShareBean> {
//        val packageManager = context!!.packageManager
//        val intent = Intent(Intent.ACTION_SEND)
//        intent.addCategory(Intent.CATEGORY_DEFAULT)
//        intent.type = "audio/*"
//        val results =
//            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
//        val shareBeanList: MutableList<ShareBean> = mutableListOf()
//        if (!results.isNullOrEmpty()) {
//            for (result in results) {
//                val shareBean = ShareBean(
//                    result.loadLabel(packageManager).toString(),
//                    result.activityInfo.packageName, result.loadIcon(packageManager)
//                )
//                shareBeanList.add(shareBean)
//            }
//        }
//        return shareBeanList
//    }

    private fun collectVoice(voice:Voice){
        lifecycleScope.launch(Dispatchers.IO){
            val result = VoiceApplication.instance().getAppDataBase().voiceDao()?.update(voice)
            Log.e("结果","$result")
            cancel()
        }
    }

    /**
     * 设置被选中tag
     */
    private fun selectTag(voice: Voice) {
        for (index in voiceList.indices) {
            val bean = voiceList[index]
            if (bean.typeName == voice.typeName && bean.itemNum == 0) {
                bean.selected = voice.selected
                voiceAdapter.notifyItemChanged(index)
            }
        }
    }

    /**
     * 根据用户名排序
     */
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

    /**
     * 根据时间排序
     */
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

    /**
     * 播放语音
     */
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

    /**
     * 扫描回调接口
     */
    private val scannerCallback = object : DiscoverAndConvertCallback() {
        override fun onReceived(voice: Voice) {

            Log.e("查看Voice信息", "${voice.vid}")

            voice.targetName = context!!.defaultSharedPreferences.getString(
                voice.targetUser,
                voice.targetUser
            ) //如果没有则取targetUser也就是code
            voiceList.add(voice)
            if (scanDialog.isAdded) {
                scanDialog.updateScanner(voiceList.size)
            }
        }

        override fun onError(error: String) {
        }

        override fun onFinished(num: Int) {
            refreshLayout.finishRefresh()
            if (isDegreeScan && scanDialog.isAdded) {
                scanDialog.dismiss()
            }
            if (num != 0) {
                voiceAdapter.releaseAllMenuData()
                when (groupTag) {
                    "时间" -> dealVoicesByTime()
                    "用户名" -> dealVoicesByName()
                }
            } else {
                voiceAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 开始扫描
     */
    private fun discoverAmr() { //开始扫描 判断是否是深度扫描 如果是开启弹窗
        if (isDegreeScan) { //深度扫描开启弹窗
            fragmentManager?.let { scanDialog.show(it, "扫描") }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            weChatScannerImpl.discoverUsersVoice(scannerCallback)
            cancel()
        }
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
        }
    }

    /**
     * 移除voiceList中分组对应的实例
     */
    private fun removeTag() {
        voiceList.removeAll { it.itemNum != 0 }
    }

    /**
     * 刷新
     */
    private fun refresh() {
        voiceAdapter.clearData()
        discoverAmr()
    }

    override fun onOkClick(view: View, message: String) {
        when (message) {
            getString(R.string.delete_info) -> {
            }
            getString(R.string.open_degree) -> {
                isDegreeScan = true
                refresh()
            }
            else -> {
                updateVoiceName(editDialog.initValue, message)
                context?.defaultSharedPreferences?.edit {
                    putString(editDialog.initValue, message)
                }
            }
        }
    }

    private fun updateVoiceName(oldName: String, newName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.e("更新比较","$oldName   $newName")
            if (groupTag == "按用户") {
                var first = false
                var startIndex: Int = 0
                val newList = mutableListOf<Voice>()
                for (i in voiceList.indices) {
                    if (voiceList[i].targetName == oldName) {
                        voiceList[i].targetName = newName
                        newList.add(voiceList[i])
                        if (!first) {
                            first = true
                            startIndex = i
                        }
                    } else if (first) {
                        break
                    }
                }
                voiceList[startIndex-1].typeName = newName
                VoiceApplication.instance().getAppDataBase().voiceDao()
                    ?.updateAll(*newList.toTypedArray())
                withContext(Dispatchers.Main){
                    voiceAdapter.notifyItemRangeChanged(startIndex,startIndex+newList.size)
                }

            } else {  //按时间分组  过滤然后更新 最后全部替换
                val newList = voiceList.filter { it.targetName == oldName }
                newList.forEach {
                    it.targetName = newName
                }
                VoiceApplication.instance().getAppDataBase().voiceDao()
                    ?.updateAll(*newList.toTypedArray())
                withContext(Dispatchers.Main) {
                    voiceAdapter.notifyDataSetChanged()
                }
            }
            cancel()
        }
    }

    override fun onCancel(view: View) {
    }

    override fun onResume() {
        super.onResume()
        initMediaListener()
    }

    private fun initMediaListener(){
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
    }

    override fun onPause() {
        super.onPause()
        PlayUtils.instance.onDestroy()
    }

    /**
     * 停止任务
     */
    override fun onDestroyView() {
        super.onDestroyView()
        voiceList.clear()
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
        if (editDialog.isAdded) {
            editDialog.dismiss()
        }
        popupListWindow = null
        timePopupListWindow = null
        scannerCallback.unregisterLifecycle(this)
    }
}