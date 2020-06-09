package com.example.audioplayer

import android.content.Intent
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.reinhard.wcvcodec.WcvCodec
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*


class LoginActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val nihaoya = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}ccc555.mp3"
        val targetPath = File(this.externalCacheDir,"tmp.mp3").absolutePath
        val sourPath = copyToCache()!!.absolutePath
//        val pcmPath = File(cacheDir, "pcm444.pcm").absolutePath
//        Log.e("源文件位置",sourPath + "\n" + nihaoya )
//        File(pcmPath).apply {
//            if (exists()){
//                delete()
//            }
//        }
//        File(targetPath).apply {
//            if (exists()){
//                delete()
//            }
//        }

//        val pcmIOPath1 = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}IOpcm.pcm"
//        val targetIOPath1 = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}IOccc.mp3"
//                val successIO1 = changeAmrToMp3(sourPath,pcmIOPath1,targetIOPath1)
//        val success = Silk.convertSilkToMp3(sourPath,nihaoya,24000)
////        val success = changeAmrToMp3(sourPath,pcmPath,targetPath)
//        Log.e("主线程","$success")

//        play(nihaoya)


//        val sourPath = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}ffgg.amr"
//        val inputStream = FileInputStream(File(sourPath))
//        val outPutStream = FileOutputStream(File(outPath),true)
//        File(outPath).createNewFile()
//        inputStream.skip(1)
//        val bytes = ByteArray(1024)
//        var len = 0
//        len = inputStream.read(bytes)
//        while (len != -1){
//            outPutStream.write(bytes,0,len)
//            len = inputStream.read(bytes)
//        }
//        inputStream.close()
//        outPutStream.close()

        GlobalScope.launch {
            withContext(Dispatchers.IO){

//                val success = Silk.convertSilkToMp3(sourPath,nihaoya,32000)
//                Log.e("转换结果","$success")

                val outPath = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}ffgg.amr"
                val pcmPath1 = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}pcpcpcpcpc.pcm"
                val targetPath1 = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}mpmpmp.mp3"
                File(targetPath1).apply {
                    if (exists()){
                        delete()
                    }
                }
                File(pcmPath1).apply {
                    if (exists()){
                        delete()
                    }
                }
                val success1 = WcvCodec.decode(outPath,pcmPath1,targetPath1)
                Log.e("子线程Unconfined","${success1 == 0}")






//                Log.e("子线程IO","${Thread.currentThread().name}")
            }
        }
//
//        lifecycleScope.launch(Dispatchers.Unconfined){
//
//            Log.e("子线程Unconfined","${Thread.currentThread().name}")
//        }
//
//        Thread{

//        }.start()
//        tv_wx.setOnClickListener {
//            UMShareAPI.get(this).getPlatformInfo(this,SHARE_MEDIA.QQ,authListener)
//        }
        tv_login.setOnClickListener {
            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
        }

    }

    fun play(src: String?) {
        val mex = MediaExtractor()
        try {
            mex.setDataSource(src)
            val mf = mex.getTrackFormat(0)
            val bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE)
            val sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            Log.d("bitRate", "" + bitRate)
            Log.d("sampleRate", "" + sampleRate)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(src)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mediaPlayer -> mediaPlayer.start() }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private var authListener: UMAuthListener = object : UMAuthListener {
        /**
         * @desc 授权开始的回调
         * @param platform 平台名称
         */
        override fun onStart(platform: SHARE_MEDIA) {
            Log.e("OnStart","回调")
            Toast.makeText(this@LoginActivity, "开始", Toast.LENGTH_LONG).show()
        }

        /**
         * @desc 授权成功的回调
         * @param platform 平台名称
         * @param action 行为序号，开发者用不上
         * @param data 用户资料返回
         */
        override fun onComplete(
            platform: SHARE_MEDIA,
            action: Int,
            data: Map<String, String>
        ) {
            Toast.makeText(this@LoginActivity, "成功了", Toast.LENGTH_LONG).show()
            Log.e("回调信息",data.toString())
            startActivity(Intent(this@LoginActivity,GuideActivity::class.java))
        }

        /**
         * @desc 授权失败的回调
         * @param platform 平台名称
         * @param action 行为序号，开发者用不上
         * @param t 错误原因
         */
        override fun onError(platform: SHARE_MEDIA, action: Int, t: Throwable) {
            Log.e("OnStart","失败")
            Toast.makeText(this@LoginActivity, "失败：" + t.message, Toast.LENGTH_LONG).show()
        }

        /**
         * @desc 授权取消的回调
         * @param platform 平台名称
         * @param action 行为序号，开发者用不上
         */
        override fun onCancel(platform: SHARE_MEDIA, action: Int) {
            Log.e("OnStart","取消")
            Toast.makeText(this@LoginActivity, "取消了", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
    }


    fun copyToCache(): File? {
        try {
            val srcFile = File(cacheDir, "hello555.amr")
            if (!srcFile.exists()) {
                val outPath = "${Environment.getExternalStorageDirectory()}${File.separator}mediaCode${File.separator}ffggss.amr"
                val `is`: InputStream = FileInputStream(File(outPath))
                //srcFile = new File(context.getCacheDir(),"hello.silk");
                val fos = FileOutputStream(srcFile)
                var byteCount: Int
                val buffer = ByteArray(1024 * 4)
                while (`is`.read(buffer).also { byteCount = it } != -1) {
                    fos.write(buffer, 0, byteCount)
                }
                fos.flush()
                `is`.close()
                fos.close()
            }
            return srcFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}