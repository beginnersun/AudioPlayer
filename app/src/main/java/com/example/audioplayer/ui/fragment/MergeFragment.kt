package com.example.audioplayer.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.R
import com.example.audioplayer.VoiceApplication
import com.example.audioplayer.adapter.VoiceAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.dialog.OnContentDialog
import com.example.audioplayer.sqlite.Voice
import com.example.audioplayer.util.*
import kotlinx.android.synthetic.main.activity_merge.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MergeFragment : Fragment(), OnContentDialog.OnDialogClickListener {

    private var needMergeAndRefresh = false
    private var voiceList: MutableList<Voice> = mutableListOf()
    private var mergeList: MutableList<Voice> = mutableListOf()
    private var editDialog: OnContentDialog = OnContentDialog.newInstance()
    private var currentPosition = -1
    private var voiceAdapter: VoiceAdapter = VoiceAdapter(voiceList).apply {
        showSelected(false)
    }

    private var playingPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_merge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverMergeList()
        initListener()

        merge_recyclerView.apply {
            layoutManager = LinearLayoutManager(context!!)
            adapter = voiceAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (needMergeAndRefresh) {
            lifecycleScope.launch(Dispatchers.IO){
                mergeVoice(mergeList)
                cancel()
            }
        }
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

    private fun initListener() {
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

        editDialog.onDialogClickListener = this

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
                        updateVoice(position)
                    }
                    R.id.iv_share -> {
                        VoiceUtil.shareMusic(context!!, data.mp3Path)
                    }
                    R.id.tv_name -> {
                        editDialog.initValue = data.targetName
                        fragmentManager?.let { editDialog.show(it, "mergeFragment") }
                    }
                }
            }
        })
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

    private fun updateVoice(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            VoiceApplication.instance().getAppDataBase().voiceDao()?.update(voiceList[position])
            voiceAdapter.notifyItemChanged(position)
            cancel()
        }
    }

    fun setMergeVoiceList(mergeList: MutableList<Voice>) {
        this.mergeList = mergeList
        needMergeAndRefresh = true
    }

    /**
     * 获取已有的合成列表
     */
    private fun discoverMergeList() {
        lifecycleScope.launch(Dispatchers.IO) {
            val newList = VoiceApplication.instance()
                .getAppDataBase().voiceDao()?.findMergeVoice(Voice.MERGE_VOICE)
            if (newList != null && newList.size != 0) {
                voiceList.addAll(newList)
            }
            withContext(Dispatchers.Main) {
                voiceAdapter.notifyDataSetChanged()
                if (!mergeList.isNullOrEmpty()) {
                    mergeVoice(mergeList)
                }
            }
        }
    }

    /**
     * 合并选中的并且添加到List中
     */
    private suspend fun mergeVoice(mergeList: MutableList<Voice>) {
        val pcmList: MutableList<String> = mergeList.map { it.pcmPath }.toMutableList()
        val mp3Path =
            getExternalPath(AUDIO_MP3_TYPE)
        val pcmPath =
            getExternalPath(AUDIO_PCM_TYPE)
        val success = mergePcmToMp3(
            pcmList,
            pcmPath,
            mp3Path
        )
        if (success) {
            needMergeAndRefresh = false
            val voice = Voice().apply {
                this.mp3Path = mp3Path
                this.pcmPath = pcmPath
                this.path = mergeList.joinToString(";") { it.path }
                this.targetUser = mergeList.joinToString("+") { it.targetUser }
                this.targetName = this.targetUser
                this.minutes =
                    getMediaDuration(mp3Path)
                this.time = System.currentTimeMillis()
                this.merge = Voice.MERGE_VOICE
            }
            mergeList.clear()
            voiceList.add(voice)
            VoiceApplication.instance().getAppDataBase().voiceDao()
                ?.insert(voiceList[voiceList.size - 1])
            withContext(Dispatchers.Main) {
                voiceAdapter.notifyItemChanged(voiceList.size - 1)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        PlayUtils.instance.onDestroy()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        PlayUtils.instance.onDestroy()
    }

    override fun onOkClick(view: View, message: String) {
        voiceList[currentPosition].targetName = message
        updateVoice(currentPosition)
    }

    override fun onCancel(view: View) {

    }

//    companion object{
//        private const val KEY_MERGER_VOICE = "key_merge_voice"
//        /**
//         * list代表要合并的语音文件  合并目录自定义
//         */
//        fun intoMergeActivity(context:Context,list:ArrayList<Voice>){
//            context.startActivity(Intent(context,
//                MergeActivity::class.java)
//                .putParcelableArrayListExtra(KEY_MERGER_VOICE,list))
//        }
//    }

}