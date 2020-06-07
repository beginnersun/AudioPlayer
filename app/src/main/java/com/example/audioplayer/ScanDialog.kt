package com.example.audioplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_scan.*

class ScanDialog private constructor():DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_scan,container,false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        Glide.with(context!!).asGif().load(R.mipmap.scan).into(iv_scan)
        tv_scan_num?.let{
            it.text = "正在扫描文件，已扫描到0个文件"
        }
    }

    fun updateScanner(size:Int){
        tv_scan_num?.let{
            it.text = "正在扫描文件，已扫描到${size}个文件"
        }
    }

    companion object{
        fun newInstance():ScanDialog{
            return ScanDialog()
        }
    }
}