package com.example.audioplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.umeng.socialize.UMAuthListener
import com.umeng.socialize.UMShareAPI
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tv_wx.setOnClickListener {
            UMShareAPI.get(this).getPlatformInfo(this,SHARE_MEDIA.QQ,authListener)
        }
        tv_login.setOnClickListener {
            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
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
}