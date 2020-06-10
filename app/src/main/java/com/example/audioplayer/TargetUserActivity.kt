package com.example.audioplayer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.adapter.VoiceAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.scanner.DiscoverAndConvertCallback
import com.example.audioplayer.scanner.WeChatScanner
import com.example.audioplayer.scanner.WeChatScannerImpl
import com.example.audioplayer.sqlite.Voice
import com.scwang.smart.refresh.footer.BallPulseFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.umeng.socialize.utils.DeviceConfigInternal.context
import kotlinx.android.synthetic.main.activity_target_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File

class TargetUserActivity:AppCompatActivity() {

    private var playingPosition = -1
    private lateinit var targetUserCode:String
    private val weChatScannerImpl: WeChatScannerImpl = WeChatScannerImpl()
    private val voiceList:MutableList<Voice> = mutableListOf()
    private var voiceAdapter:VoiceAdapter = VoiceAdapter(voiceList)
    private var editDialog: OnContentDialog = OnContentDialog.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target_user)
        discoverCallback.registerLifecycle(this)
        targetUserCode = intent!!.getStringExtra(KEY_USER_CODE)

        smart_refreshLayout.apply {
            setRefreshHeader(MaterialHeader(this@TargetUserActivity))
            setRefreshFooter(BallPulseFooter(this@TargetUserActivity))
            setEnableLoadMore(true)
            setEnableRefresh(true)
            setOnRefreshListener {
                refresh()
            }
            setOnLoadMoreListener {
                loadMore()
            }
        }

        target_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TargetUserActivity)
            adapter = voiceAdapter
        }

        initListener()

        discoverVoice()
    }

    private fun initListener(){
        editDialog.onDialogClickListener = object :OnContentDialog.OnDialogClickListener{
            override fun onOkClick(view: View, message: String) {
                when(message){
                    getString(R.string.delete_info) -> {

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

        voiceAdapter.setOnItemClickListener(object : BaseRecyclerViewAdapter.OnRecyclerViewItemClickListener<Voice>{
            override fun onItemClick(view: View, viewType: Int, data: Voice, position: Int) {
                when(view.id){
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
                        supportFragmentManager?.let { editDialog.show(it, showTag) }
                    }
                }
            }

        })
    }

    /**
     * 分享语音
     */
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
        intent.putExtra(Intent.EXTRA_STREAM,uri)
        startActivity(Intent.createChooser(intent,"分享"))
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

    /**
     * 检查是否有选中项
     */
    private fun checkHaveSelected(): Boolean {
        voiceList.forEach {
            if (it.selected) {
                return it.selected
            }
        }
        return false
    }

    private fun stopPlay() {
        PlayUtils.instance.stop()
    }

    private fun discoverVoice(){
        lifecycleScope.launch(Dispatchers.IO){
            weChatScannerImpl.discoverUsersVoiceByTargetName(targetUserCode,discoverCallback)
        }
    }

    private val discoverCallback: WeChatScanner.BaseDiscoverCallback = object :
        DiscoverAndConvertCallback(){
        override fun onReceived(voice: Voice) {
            voiceList.add(voice)
            Log.e("扫描结果","增加")
        }

        override fun onError(error: String) {

        }

        override fun onFinished(num: Int) {
            smart_refreshLayout.finishLoadMore()
            smart_refreshLayout.finishRefresh()
            voiceAdapter.notifyDataSetChanged()
        }
    }

    private fun refresh(){
        weChatScannerImpl.count = 0
        voiceAdapter.clearData()
    }

    private fun loadMore(){
        weChatScannerImpl.count++
        discoverVoice()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (editDialog.isAdded){
            editDialog.dismiss()
        }
        discoverCallback.unregisterLifecycle(this)
        PlayUtils.instance.onDestroy()
    }

    companion object{
        const val KEY_USER_CODE = "KeyOfUserCode"
        const val showTag = "targetUser"

        fun intoActivity(context: Context,userCode:String){
            context.startActivity(Intent(context,TargetUserActivity::class.java).putExtra(
                KEY_USER_CODE,userCode))
        }
    }
}