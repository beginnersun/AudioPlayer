package com.example.audioplayer.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.audioplayer.R
import com.example.testapp.Book
import com.example.testapp.IBookManager
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity:AppCompatActivity() {

    var connected = false
    var bookManager:IBookManager? = null

    val bookList = mutableListOf(Book("年后",52),Book("CC",42),Book("AC",43),Book("SSSAC",43))
    var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
//        bindService()
        tv_login.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("androidamap://mylocation")
            )
            startActivity(intent)
//            if (connected){
//                bookManager!!.addBook(bookList[count++])
//            }else{
//                Log.e("不能增加","")
//            }
        }
        tv_wx.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("baidumap://map/geocoder")
            )
            startActivity(intent)
//            if (connected){
//                bookManager!!.bookList.forEach {
//                    Log.e("输出一次",it.name + it.size)
//                }
//            }else{
//                Log.e("不能获取","")
//            }
        }
    }
}