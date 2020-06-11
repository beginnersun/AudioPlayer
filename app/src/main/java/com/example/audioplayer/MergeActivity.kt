package com.example.audioplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audioplayer.adapter.VoiceAdapter
import com.example.audioplayer.base.BaseRecyclerViewAdapter
import com.example.audioplayer.sqlite.Voice
import kotlinx.android.synthetic.main.activity_merge.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MergeActivity:AppCompatActivity() {

    private lateinit var voiceList: MutableList<Voice>
    private var voiceAdapter:VoiceAdapter = VoiceAdapter(voiceList)
    private var playingPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge)

        discoverMergeList()
        initListener()

        merge_recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MergeActivity)
            adapter = voiceAdapter
        }
    }

    private fun initListener(){
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

        voiceAdapter.setOnItemClickListener(object :BaseRecyclerViewAdapter.OnRecyclerViewItemClickListener<Voice>{
            override fun onItemClick(view: View, viewType: Int, data: Voice, position: Int) {
                when(view.id){

                }
            }
        })
    }

    /**
     * 获取已有的合成列表
     */
    private fun discoverMergeList(){
        lifecycleScope.launch(Dispatchers.IO){
            val newList = VoiceApplication.instance().getAppDataBase().voiceDao()?.findMergeVoice(Voice.MERGE_VOICE)
            if (newList!=null && newList.size != 0){
                voiceList.addAll(newList)
            }
            withContext(Dispatchers.Main){
                voiceAdapter.notifyDataSetChanged()
                intent!!.getParcelableArrayListExtra<Voice>(KEY_MERGER_VOICE)?.let {
                    mergeVoice(it)
                }
            }
        }
    }

    private fun mergeVoice(mergeList:MutableList<Voice>){
        lifecycleScope.launch(Dispatchers.IO){
            val pcmList:MutableList<String> = mergeList.map { it.pcmPath }.toMutableList()
            val mp3Path = getExternalPath(AUDIO_MP3_TYPE)
            val pcmPath = getExternalPath(AUDIO_PCM_TYPE)
            val success = mergePcmToMp3(pcmList,pcmPath, mp3Path)
            if (success) {
                val voice = Voice().apply {
                    this.mp3Path = mp3Path
                    this.pcmPath = pcmPath
                    this.path = mergeList.joinToString(";") { it.path }
                    this.targetUser = mergeList.joinToString("+") { it.targetUser }
                    this.targetName = this.targetUser
                    this.minutes = getMediaDuration(mp3Path)
                    this.time = System.currentTimeMillis()
                    this.merge = Voice.MERGE_VOICE
                }
                voiceList.add(voice)
                VoiceApplication.instance().getAppDataBase().voiceDao()
                    ?.insert(voiceList[voiceList.size - 1])
                withContext(Dispatchers.Main) {
                    voiceAdapter.notifyItemChanged(voiceList.size - 1)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlayUtils.instance.onDestroy()
    }

    companion object{
        private const val KEY_MERGER_VOICE = "key_merge_voice"
        /**
         * list代表要合并的语音文件  合并目录自定义
         */
        fun intoMergeActivity(context:Context,list:ArrayList<Voice>){
            context.startActivity(Intent(context,MergeActivity::class.java)
                .putParcelableArrayListExtra(KEY_MERGER_VOICE,list))
        }
    }

}