package com.example.audioplayer

import android.media.AudioManager
import android.media.MediaPlayer

class PlayUtils:MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var mediaPlayer = MediaPlayer()
    private var playListener:OnPlayChangedListener? = null

    private constructor()

    companion object{
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED){
            PlayUtils()
        }
    }

    fun play(path:String){
        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }
        mediaPlayer.setOnCompletionListener (this )
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnErrorListener(this)
        mediaPlayer.setOnPreparedListener ( this )
        try {
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepareAsync()
        }catch (t:Throwable){

        }

    }

    fun stop(){
        mediaPlayer?.stop()
        mediaPlayer?.reset()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        playListener?.onPrepared()
        mediaPlayer.start()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        this.mediaPlayer.reset()
        playListener?.onCompleted()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        playListener?.onError()
        return true
    }

    fun setOnPlayChangedListener(onPlayChangedListener: OnPlayChangedListener){
        this.playListener = onPlayChangedListener
    }

    interface OnPlayChangedListener{
        fun onPrepared()

        fun onCompleted()

        fun onError()
    }

    fun onDestroy(){
        this.mediaPlayer.stop()
        this.mediaPlayer.release()
    }

}